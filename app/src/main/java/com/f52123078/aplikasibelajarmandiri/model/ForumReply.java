package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ForumReply {
    private String replyId;
    private String userId;
    private String userName;
    private String text;
    private Date timestamp;

    public ForumReply() {} // Wajib kosong

    public ForumReply(String userId, String userName, String text) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
    }

    // Getters & Setters
    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}