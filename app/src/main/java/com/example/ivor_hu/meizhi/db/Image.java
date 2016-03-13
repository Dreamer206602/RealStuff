package com.example.ivor_hu.meizhi.db;

import android.graphics.Point;

import com.example.ivor_hu.meizhi.net.ImageFetcher;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ivor on 2016/2/9.
 */
public class Image {
    private String id;
    private String url;
    private int width;
    private int height;
    private Date publishedAt;

    public Image(String id, String url, Date publishedAt) {
        this(id, url, 0, 0, publishedAt);
    }

    public Image(String id, String url, int width, int height, Date publishedAt) {
        this.id = id;
        this.url = url;
        this.width = width;
        this.height = height;
        this.publishedAt = publishedAt;
    }

    public static Image persist(Image image, ImageFetcher imageFetcher)
            throws IOException, InterruptedException, ExecutionException {
        Point size = new Point();

        imageFetcher.prefetchImage(image.getUrl(), size);

        image.setWidth(size.x);
        image.setHeight(size.y);

        return image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
}
