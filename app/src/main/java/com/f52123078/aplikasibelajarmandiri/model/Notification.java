package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String notifId;
    private String userId;      // Penerima notifikasi (Pemilik Postingan)
    private String senderName;  // Nama orang yang membalas
    private String message;     // Isi pesan singkat (misal: "membalas postingan Anda")
    private String postId;      // ID Postingan (supaya kalau diklik bisa ke detail)
    private Date timestamp;

    public Notification() {}

    public Notification(String userId, String senderName, String message, String postId) {
        this.userId = userId;
        this.senderName = senderName;
        this.message = message;
        this.postId = postId;
    }

    // Getters & Setters
    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}