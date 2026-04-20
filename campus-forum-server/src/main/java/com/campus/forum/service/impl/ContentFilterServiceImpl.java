package com.campus.forum.service.impl;

import com.campus.forum.entity.AuditSensitiveWord;
import com.campus.forum.mapper.AuditSensitiveWordMapper;
import com.campus.forum.service.ContentFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class ContentFilterServiceImpl implements ContentFilterService {

    private static final Logger log = LoggerFactory.getLogger(ContentFilterServiceImpl.class);

    @Autowired
    private AuditSensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private SensitiveWordEngine wordEngine;

        /**
         * 业务白名单词：这些词在特定业务语境中是正常表达，不应触发拦截。
         */
        private static final Set<String> BUSINESS_ALLOW_WORDS = Set.of(
            "求购"
        );

    @Override
    public FilterResult filter(String content) {
        FilterDetail detail = filterWithDetails(content);
        return detail.getResult();
    }

    @Override
    public FilterResult filter(String title, String content) {
        FilterDetail detail = filterWithDetails(title, content);
        return detail.getResult();
    }

    @Override
    public FilterDetail filterWithDetails(String content) {
        return doFilter(content, null);
    }

    @Override
    public FilterDetail filterWithDetails(String title, String content) {
        return doFilter(content, title);
    }

    /**
     * 核心过滤方法 — 使用DFA引擎进行高效匹配
     */
    private FilterDetail doFilter(String content, String title) {
        if (content == null && title == null) {
            return new FilterDetail(FilterResult.PASS, Collections.emptyList(), 0, null);
        }

        String fullText = buildFullText(title, content);
        if (fullText.trim().isEmpty()) {
            return new FilterDetail(FilterResult.PASS, Collections.emptyList(), 0, fullText);
        }

        // 使用DFA引擎检测（O(n)复杂度，内置词库+DB自定义词双层）
        SensitiveWordEngine.MatchResult matchResult = wordEngine.findSensitive(fullText);

        if (!matchResult.hasMatch()) {
            return new FilterDetail(FilterResult.PASS, Collections.emptyList(), 0, fullText);
        }

        List<SensitiveWordEngine.WordWithLevel> effectiveMatches = matchResult.getMatchedWords().stream()
            .filter(item -> item != null && item.getWord() != null)
            .filter(item -> !BUSINESS_ALLOW_WORDS.contains(item.getWord().toLowerCase()))
            .toList();

        if (effectiveMatches.isEmpty()) {
            return new FilterDetail(FilterResult.PASS, Collections.emptyList(), 0, fullText);
        }

        List<String> keywords = effectiveMatches.stream()
            .map(SensitiveWordEngine.WordWithLevel::getWord)
            .toList();
        int maxLevel = effectiveMatches.stream()
            .map(SensitiveWordEngine.WordWithLevel::getLevel)
            .max(Integer::compareTo)
            .orElse(0);

        // DB自定义词优先使用DB配置的replacement，否则用 ***
        String filteredContent = applyCustomReplacements(fullText, keywords);

        FilterResult result = determineResult(maxLevel, keywords.size());

        log.info("内容审核命中: result={}, level={}, keywords={}", result, maxLevel, keywords);

        return new FilterDetail(result, keywords, maxLevel, filteredContent);
    }

    /**
     * 应用DB中配置的敏感词替换规则
     */
    private String applyCustomReplacements(String text, List<String> matchedWords) {
        String result = text;
        for (String word : matchedWords) {
            // 查找DB中是否有自定义替换词
            AuditSensitiveWord sw = findDbWord(word);
            if (sw != null && sw.getReplacement() != null && !sw.getReplacement().isEmpty()) {
                result = result.replaceAll(
                        "(?i)" + java.util.regex.Pattern.quote(word),
                        sw.getReplacement()
                );
            }
        }
        return result;
    }

    private FilterResult determineResult(int maxLevel, int matchCount) {
        if (matchCount == 0 || maxLevel == 0) {
            return FilterResult.PASS;
        }
        switch (maxLevel) {
            case 3:
                return FilterResult.BLOCKED;
            case 2:
                return FilterResult.NEED_MANUAL;
            case 1:
                return FilterResult.SUSPICIOUS;
            default:
                return matchCount >= 3 ? FilterResult.SUSPICIOUS : FilterResult.PASS;
        }
    }

    private String buildFullText(String title, String content) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isEmpty()) {
            sb.append(title).append(" ");
        }
        if (content != null && !content.isEmpty()) {
            sb.append(content);
        }
        return sb.toString();
    }

    // ==================== 词库CRUD操作（保持向后兼容） ====================

    @Override
    public List<AuditSensitiveWord> getAllSensitiveWords() {
        return sensitiveWordMapper.selectPage(0, 1000);
    }

    @Override
    public AuditSensitiveWord addSensitiveWord(AuditSensitiveWord word) {
        sensitiveWordMapper.insert(word);
        wordEngine.refreshDbCache();  // 同步刷新DFA引擎
        log.info("新增敏感词: {} (level={})", word.getWord(), word.getLevel());
        return word;
    }

    @Override
    public boolean updateSensitiveWord(AuditSensitiveWord word) {
        int rows = sensitiveWordMapper.update(word);
        wordEngine.refreshDbCache();
        log.info("更新敏感词: id={}", word.getId());
        return rows > 0;
    }

    @Override
    public boolean deleteSensitiveWord(Integer id) {
        int rows = sensitiveWordMapper.deleteById(id);
        wordEngine.refreshDbCache();
        log.info("删除敏感词: id={}", id);
        return rows > 0;
    }

    @Override
    public List<AuditSensitiveWord> getSensitiveWordsByCategory(Integer category) {
        return sensitiveWordMapper.selectByCategory(category);
    }

    /**
     * 从DB缓存查找敏感词实体（用于获取replacement等属性）
     */
    private AuditSensitiveWord findDbWord(String word) {
        try {
            return sensitiveWordMapper.selectAllEnabled().stream()
                    .filter(sw -> sw.getWord() != null && sw.getWord().equalsIgnoreCase(word))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
