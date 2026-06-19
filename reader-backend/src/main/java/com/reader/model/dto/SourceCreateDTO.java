package com.reader.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SourceCreateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Path is required")
    private String path;

    private String type;

    private Integer scanIntervalMin;

    public SourceCreateDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getScanIntervalMin() {
        return scanIntervalMin;
    }

    public void setScanIntervalMin(Integer scanIntervalMin) {
        this.scanIntervalMin = scanIntervalMin;
    }
}
