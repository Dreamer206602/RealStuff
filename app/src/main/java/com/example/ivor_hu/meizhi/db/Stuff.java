package com.example.ivor_hu.meizhi.db;

import java.util.Date;

/**
 * Created by Ivor on 2016/2/28.
 */
public class Stuff {
    private String id;
    private String title, url, author, type;
    private Date publishedAt, lastChanged;
    private boolean isLiked;

    public Stuff() {
    }

    public Stuff(String id, String type, String title, String url, String author, Date publishedAt, Date lastChanged, boolean isLiked) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.url = url;
        this.author = author;
        this.publishedAt = publishedAt;
        this.lastChanged = lastChanged;
        this.isLiked = isLiked;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }
}
