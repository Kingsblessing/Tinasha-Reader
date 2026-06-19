package com.reader.model.vo;

import java.time.LocalDateTime;

public class FavoriteVO {
    private long id;
    private long comicId;
    private String comicTitle;
    private String comicCoverPath;
    private String groupName;
    private LocalDateTime createdAt;

    public FavoriteVO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getComicId() {
        return comicId;
    }

    public void setComicId(long comicId) {
        this.comicId = comicId;
    }

    public String getComicTitle() {
        return comicTitle;
    }

    public void setComicTitle(String comicTitle) {
        this.comicTitle = comicTitle;
    }

    public String getComicCoverPath() {
        return comicCoverPath;
    }

    public void setComicCoverPath(String comicCoverPath) {
        this.comicCoverPath = comicCoverPath;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
