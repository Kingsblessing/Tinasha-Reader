package com.reader.model.entity;

import java.time.LocalDateTime;

public class Source {
    private long id;
    private String name;
    private String path;
    private String type;
    private boolean enabled;
    private String scanStatus;
    private LocalDateTime lastScanAt;
    private int scanIntervalMin;
    private int comicCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Source() {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public LocalDateTime getLastScanAt() {
        return lastScanAt;
    }

    public void setLastScanAt(LocalDateTime lastScanAt) {
        this.lastScanAt = lastScanAt;
    }

    public int getScanIntervalMin() {
        return scanIntervalMin;
    }

    public void setScanIntervalMin(int scanIntervalMin) {
        this.scanIntervalMin = scanIntervalMin;
    }

    public int getComicCount() {
        return comicCount;
    }

    public void setComicCount(int comicCount) {
        this.comicCount = comicCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
