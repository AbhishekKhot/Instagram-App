package com.example.instagram.model;

public class Comments extends com.example.instagram.model.CommentsId {
    private String comment,user;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
