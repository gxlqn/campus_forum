package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.AuditLog;
import com.campus.forum.entity.AuditSensitiveWord;

import java.util.List;

public interface ContentFilterService {

    FilterResult filter(String content);

    FilterResult filter(String title, String content);

    FilterDetail filterWithDetails(String content);

    FilterDetail filterWithDetails(String title, String content);

    List<AuditSensitiveWord> getAllSensitiveWords();

    List<AuditSensitiveWord> getSensitiveWordsByType(Integer wordType);

    AuditSensitiveWord addSensitiveWord(AuditSensitiveWord word);

    boolean updateSensitiveWord(AuditSensitiveWord word);

    boolean deleteSensitiveWord(Integer id);

    List<AuditSensitiveWord> getSensitiveWordsByCategory(Integer category);

    PageResult<AuditSensitiveWord> getSensitiveWordsPage(Long current, Long size, Integer wordType, Integer category, String keyword);

    enum FilterResult {
        PASS(0, "通过"),
        SUSPICIOUS(1, "可疑-命中弱级词"),
        NEED_MANUAL(2, "需人工审核-命中中级词"),
        BLOCKED(3, "直接拒绝-命中强级词");

        private final int code;
        private final String description;

        FilterResult(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public boolean shouldBlock() {
            return this == BLOCKED;
        }

        public boolean needManualReview() {
            return this == NEED_MANUAL || this == SUSPICIOUS;
        }

        public boolean isPass() {
            return this == PASS;
        }
    }

    class FilterDetail {
        private FilterResult result;
        private List<String> matchedKeywords;
        private Integer maxLevel;
        private String filteredContent;

        public FilterDetail() {
        }

        public FilterDetail(FilterResult result, List<String> matchedKeywords, Integer maxLevel, String filteredContent) {
            this.result = result;
            this.matchedKeywords = matchedKeywords;
            this.maxLevel = maxLevel;
            this.filteredContent = filteredContent;
        }

        public FilterResult getResult() {
            return result;
        }

        public void setResult(FilterResult result) {
            this.result = result;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }

        public Integer getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(Integer maxLevel) {
            this.maxLevel = maxLevel;
        }

        public String getFilteredContent() {
            return filteredContent;
        }

        public void setFilteredContent(String filteredContent) {
            this.filteredContent = filteredContent;
        }
    }
}
