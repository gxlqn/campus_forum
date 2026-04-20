package com.campus.forum.dto.im;

public class ImSyncRequest {

    private String conversationId;
    private Long cursorMessageId;
    private Integer size;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getCursorMessageId() {
        return cursorMessageId;
    }

    public void setCursorMessageId(Long cursorMessageId) {
        this.cursorMessageId = cursorMessageId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
