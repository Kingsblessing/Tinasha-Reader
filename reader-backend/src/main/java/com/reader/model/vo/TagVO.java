package com.reader.model.vo;

public class TagVO {
    private long id;
    private String name;
    private String groupName;
    private String color;
    private int comicCount;

    public TagVO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getComicCount() {
        return comicCount;
    }

    public void setComicCount(int comicCount) {
        this.comicCount = comicCount;
    }
}
