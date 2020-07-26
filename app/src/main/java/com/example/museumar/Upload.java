package com.example.museumar;

import com.google.firebase.database.Exclude;

// class for getters and setters for the uploading process
public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;
    private long mTimestamp;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public long getTimestamp() {return mTimestamp;}

    public void setTimestamp(long timestamp) {mTimestamp = timestamp;}

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}
