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
import org.springframework.context.annotation.DependsOn;
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
@DependsOn("sensitiveWordSchemaInitializer")
public class SensitiveWordEngine {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordEngine.class);

    @Autowired
    private AuditSensitiveWordMapper sensitiveWordMapper;

    /** DFA引擎实例（支持动态增删词） */
    private SensitiveWordBs wordBs;

    /** DB自定义黑名单词 → level 映射（运行时可刷新） */
    private Map<String, Integer> dbWordLevelMap = new ConcurrentHashMap<>();

    /** DB白名单词（运行时可刷新） */
    private Set<String> dbAllowWordSet = ConcurrentHashMap.newKeySet();

    /** 上次DB缓存刷新时间 */
    private volatile long lastDbRefreshTime = 0;
    private static final long DB_CACHE_TTL_MS = 5 * 60 * 1000;

    /**
     * 初始化DFA引擎：内置默认词库 + 加载DB自定义词库
     */
    @PostConstruct
    public void init() {
        refreshDbWordCache();
        log.info("敏感词DFA引擎初始化完成，黑名单词库数量={}，白名单词库数量={}",
                dbWordLevelMap.size(), dbAllowWordSet.size());
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
            String lowerWord = word.toLowerCase(Locale.ROOT);
            if (dbAllowWordSet.contains(lowerWord)) {
                continue;
            }
            int level = resolveWordLevel(lowerWord);
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
    private void buildEngine(List<String> denyWords, List<String> allowWords) {
        IWordAllow allowSource = WordAllows.empty();

        SensitiveWordBs newWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.defaults())
                .wordAllow(allowSource)
                .init();

        if (denyWords != null && !denyWords.isEmpty()) {
            newWordBs.addWord(denyWords);
        }

        if (allowWords != null && !allowWords.isEmpty()) {
            newWordBs.addWordAllow(allowWords);
        }

        this.wordBs = newWordBs;

        log.info("DFA引擎重建完成，黑名单词数量={}，白名单词数量={}",
                denyWords != null ? denyWords.size() : 0,
                allowWords != null ? allowWords.size() : 0);
    }

    /**
     * 从数据库加载黑白名单词列表
     */
    private CacheSnapshot loadDbWords() {
        Map<String, Integer> denyMap = new ConcurrentHashMap<>();
        Set<String> allowSet = ConcurrentHashMap.newKeySet();
        List<String> denyWords = new ArrayList<>();
        List<String> allowWords = new ArrayList<>();
        try {
            List<AuditSensitiveWord> dbWords = sensitiveWordMapper.selectAllEnabled();
            for (AuditSensitiveWord sw : dbWords) {
                if (sw.getWord() != null && !sw.getWord().trim().isEmpty()) {
                    String lowerWord = sw.getWord().toLowerCase(Locale.ROOT);
                    if (sw.getWordType() != null && sw.getWordType() == 2) {
                        allowWords.add(sw.getWord());
                        allowSet.add(lowerWord);
                    } else {
                        denyWords.add(sw.getWord());
                        denyMap.put(lowerWord, sw.getLevel() != null ? sw.getLevel() : 1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("从DB加载敏感词失败", e);
        }
        return new CacheSnapshot(denyWords, allowWords, denyMap, allowSet);
    }

    /**
     * 刷新DB词库：增量同步到DFA引擎
     */
    private synchronized void refreshDbWordCache() {
        try {
            CacheSnapshot snapshot = loadDbWords();
            buildEngine(snapshot.denyWords, snapshot.allowWords);
            this.dbWordLevelMap = snapshot.denyLevelMap;
            this.dbAllowWordSet = snapshot.allowWordSet;
            this.lastDbRefreshTime = System.currentTimeMillis();
            log.info("DB敏感词库刷新完成，共 {} 个黑名单词，{} 个白名单词", snapshot.denyWords.size(), snapshot.allowWords.size());
        } catch (Exception e) {
            log.error("刷新DB敏感词库失败", e);
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

    private static class CacheSnapshot {
        private final List<String> denyWords;
        private final List<String> allowWords;
        private final Map<String, Integer> denyLevelMap;
        private final Set<String> allowWordSet;

        private CacheSnapshot(List<String> denyWords, List<String> allowWords,
                              Map<String, Integer> denyLevelMap, Set<String> allowWordSet) {
            this.denyWords = denyWords;
            this.allowWords = allowWords;
            this.denyLevelMap = denyLevelMap;
            this.allowWordSet = allowWordSet;
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
