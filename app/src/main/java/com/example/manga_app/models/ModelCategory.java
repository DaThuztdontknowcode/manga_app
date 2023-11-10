package com.example.manga_app.models;
public class ModelCategory {
    String id, category, uid;
    String timestamp; // Sửa kiểu dữ liệu của timestamp thành String

    public ModelCategory() {
        // Empty constructor required for DataSnapshot.getValue(ModelCategory.class)
    }

    public ModelCategory(String id, String category, String uid, String timestamp) {
        this.id = id;
        this.category = category;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    // Bổ sung các phương thức getter và setter cho timestamp nếu cần thiết

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getUid() {
        return uid;
    }

    public String getTimestamp() {
        return timestamp;
    }
}


