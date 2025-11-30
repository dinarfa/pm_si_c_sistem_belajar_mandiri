package com.f52123078.aplikasibelajarmandiri.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ForumPost {
    private String postId;      // ID Dokumen Firestore
    private String userId;      // Siapa yang posting
    private String userName;    // Nama si pemosting
    private String text;        // Isi pertanyaan
    private String imageUrl;    // URL gambar dari Cloudinary
    private Date timestamp;     // Waktu posting
    private String userPhotoUrl;

    private String prodiName;
    private String mkName;


    public ForumPost() {}

    public ForumPost(String userId, String userName, String userPhotoUrl, String text, String imageUrl, String prodiName, String mkName) {
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.text = text;
        this.imageUrl = imageUrl;
        this.prodiName = prodiName;
        this.mkName = mkName;
    }

    // Getters & Setters Baru
    public String getProdiName() { return prodiName; }
    public void setProdiName(String prodiName) { this.prodiName = prodiName; }

    public String getMkName() { return mkName; }
    public void setMkName(String mkName) { this.mkName = mkName; }

    public String getUserPhotoUrl() { return userPhotoUrl; }
    public void setUserPhotoUrl(String userPhotoUrl) { this.userPhotoUrl = userPhotoUrl; }

    // --- Getters & Setters ---

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}