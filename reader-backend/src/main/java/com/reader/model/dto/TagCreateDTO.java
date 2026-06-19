package com.reader.model.dto;

import jakarta.validation.constraints.NotBlank;

public class TagCreateDTO {
    @NotBlank(message = "Tag name is required")
    private String name;

    private String groupName;

    private String color;

    private Long parentId;

    public TagCreateDTO() {
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
