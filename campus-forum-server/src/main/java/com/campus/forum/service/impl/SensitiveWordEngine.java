package com.campus.forum.service.impl;

import com.campus.forum.entity.AuditSensitiveWord;
import com.campus.forum.mapper.AuditSensitiveWordMapper;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词DFA引擎组件
 * <p>
 * 双层词库架构：
 * 1. 基础层：sensitive-word 内置词库（5000+ 中文敏感词）
 * 2. 自定义层：数据库 audit_sensitive_word 表（管理员动态维护）
 * <p>
 * 使用 SensitiveWordBs 实例支持运行时动态增删，DFA 算法匹配复杂度 O(n)
 */
@Component
public class SensitiveWordEngine {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordEngine.class);

    @Autowired
    private AuditSensitiveWordMapper sensitiveWordMapper;

    /** DFA引擎实例（支持动态增删词） */
    private SensitiveWordBs wordBs;

    /** DB自定义敏感词 → level 映射（运行时可刷新） */
    private Map<String, Integer> dbWordLevelMap = new ConcurrentHashMap<>();

    /** 上次DB缓存刷新时间 */
    private volatile long lastDbRefreshTime = 0;
    private static final long DB_CACHE_TTL_MS = 5 * 60 * 1000;

    /**
     * 初始化DFA引擎：内置默认词库 + 加载DB自定义词库
     */
    @PostConstruct
    public void init() {
        // 从DB加载初始自定义词列表
        List<String> initialDbWords = loadDbWords();
        buildEngine(initialDbWords);
        log.info("敏感词DFA引擎初始化完成，DB自定义词库数量={}", dbWordLevelMap.size());
    }

    // ==================== 公开API ====================

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 匹配结果，包含命中的敏感词列表、最高级别、替换后内容
     */
    public MatchResult findSensitive(String text) {
        if (text == null || text.trim().isEmpty()) {
            return MatchResult.empty();
        }
        ensureDbCacheFresh();

        List<String> dfaMatches = wordBs.findAll(text);
        Set<String> allMatchedWords = new LinkedHashSet<>(dfaMatches);

        int maxLevel = 0;
        List<WordWithLevel> wordDetails = new ArrayList<>();
        for (String word : allMatchedWords) {
            int level = resolveWordLevel(word.toLowerCase());
            wordDetails.add(new WordWithLevel(word, level));
            if (level > maxLevel) {
                maxLevel = level;
            }
        }

        String filteredText = wordBs.replace(text);
        return new MatchResult(wordDetails, maxLevel, filteredText, !allMatchedWords.isEmpty());
    }

    public boolean containsSensitive(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        ensureDbCacheFresh();
        return wordBs.contains(text);
    }

    public String replaceSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        ensureDbCacheFresh();
        return wordBs.replace(text);
    }

    /**
     * 强制刷新DB词库缓存（管理员增删敏感词时调用）
     */
    public void refreshDbCache() {
        refreshDbWordCache();
    }

    // ==================== 内部方法 ====================

    /**
     * 构建或重建 DFA 引擎实例
     */
    private void buildEngine(List<String> dbCustomWords) {
        // 先基于内置默认词库初始化引擎
        IWordAllow allowSource = WordAllows.empty();

        this.wordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.defaults())
                .wordAllow(allowSource)
                .init();

        // 再逐个添加DB自定义敏感词到运行时引擎
        if (dbCustomWords != null && !dbCustomWords.isEmpty()) {
            for (String word : dbCustomWords) {
                try {
                    wordBs.addWord(word);
                } catch (Exception e) {
                    log.debug("初始化DB自定义词失败: {}", word);
                }
            }
        }

        log.info("DFA引擎重建完成，内置+DB词库已加载, DB自定义词数量={}",
                dbCustomWords != null ? dbCustomWords.size() : 0);
    }

    /**
     * 从数据库加载自定义敏感词列表
     */
    private List<String> loadDbWords() {
        Map<String, Integer> newMap = new ConcurrentHashMap<>();
        List<String> words = new ArrayList<>();
        try {
            List<AuditSensitiveWord> dbWords = sensitiveWordMapper.selectAllEnabled();
            for (AuditSensitiveWord sw : dbWords) {
                if (sw.getWord() != null && !sw.getWord().trim().isEmpty()) {
                    words.add(sw.getWord());
                    newMap.put(sw.getWord().toLowerCase(),
                            sw.getLevel() != null ? sw.getLevel() : 1);
                }
            }
        } catch (Exception e) {
            log.error("从DB加载敏感词失败", e);
        }
        this.dbWordLevelMap = newMap;
        return words;
    }

    /**
     * 刷新DB词库：增量同步到DFA引擎
     */
    private void refreshDbWordCache() {
        try {
            Map<String, Integer> oldMap = this.dbWordLevelMap;
            List<AuditSensitiveWord> dbWords = sensitiveWordMapper.selectAllEnabled();

            Map<String, Integer> newMap = new ConcurrentHashMap<>();
            for (AuditSensitiveWord sw : dbWords) {
                if (sw.getWord() != null && !sw.getWord().trim().isEmpty()) {
                    newMap.put(sw.getWord().toLowerCase(),
                            sw.getLevel() != null ? sw.getLevel() : 1);
                }
            }

            // 增量同步到DFA引擎
            syncDfaIncremental(oldMap.keySet(), newMap.keySet());

            this.dbWordLevelMap = newMap;
            this.lastDbRefreshTime = System.currentTimeMillis();
            log.info("DB敏感词库刷新完成，共 {} 个自定义敏感词", newMap.size());
        } catch (Exception e) {
            log.error("刷新DB敏感词库失败", e);
        }
    }

    /**
     * 增量同步：用 SensitiveWordBs 实例的 addWord/removeWord 方法
     */
    private void syncDfaIncremental(Set<String> oldKeys, Set<String> newKeys) {
        // 移除已删除的词
        for (String word : oldKeys) {
            if (!newKeys.contains(word)) {
                try {
                    wordBs.removeWord(word);
                } catch (Exception e) {
                    log.debug("移除DFA词失败: {}", word);
                }
            }
        }
        // 新增的词
        for (String word : newKeys) {
            if (!oldKeys.contains(word)) {
                try {
                    wordBs.addWord(word);
                } catch (Exception e) {
                    log.debug("添加DFA词失败: {}", word);
                }
            }
        }
    }

    /**
     * 解析命中词级别：
     * - DB自定义词优先使用DB配置的level
     * - 内置词默认为 level=3（BLOCKED，直接拦截）
     */
    private int resolveWordLevel(String lowerWord) {
        Integer dbLevel = dbWordLevelMap.get(lowerWord);
        if (dbLevel != null) {
            return dbLevel;
        }
        // 内置词库 → 强级拦截
        return 3;
    }

    private void ensureDbCacheFresh() {
        if (System.currentTimeMillis() - lastDbRefreshTime > DB_CACHE_TTL_MS) {
            refreshDbWordCache();
        }
    }

    // ==================== 数据类 ====================

    public static class MatchResult {
        private final List<WordWithLevel> matchedWords;
        private final int maxLevel;
        private final String filteredContent;
        private final boolean hasMatch;

        public MatchResult(List<WordWithLevel> matchedWords, int maxLevel,
                           String filteredContent, boolean hasMatch) {
            this.matchedWords = matchedWords;
            this.maxLevel = maxLevel;
            this.filteredContent = filteredContent;
            this.hasMatch = hasMatch;
        }

        public static MatchResult empty() {
            return new MatchResult(Collections.emptyList(), 0, null, false);
        }

        public List<WordWithLevel> getMatchedWords() { return matchedWords; }
        public int getMaxLevel() { return maxLevel; }
        public String getFilteredContent() { return filteredContent; }
        public boolean hasMatch() { return hasMatch; }
        public List<String> getKeywordList() {
            return matchedWords.stream().map(WordWithLevel::getWord).toList();
        }
    }

    public static class WordWithLevel {
        private final String word;
        private final int level;

        public WordWithLevel(String word, int level) {
            this.word = word;
            this.level = level;
        }

        public String getWord() { return word; }
        public int getLevel() { return level; }
    }
}
