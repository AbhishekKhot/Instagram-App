package com.example.instagram;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class CommentsId {
    @Exclude
    public String CommentsId;

    public <T extends CommentsId>  T withId (@NonNull final String id){
        this.CommentsId = id;
        return (T) this;
    }
}
