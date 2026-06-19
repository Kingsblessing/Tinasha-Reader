package com.reader.model.vo;

import java.util.List;

public class ReadingStatsVO {
    private int todayPages;
    private int weekPages;
    private int monthPages;
    private int totalComics;
    private int totalTimeSec;
    private List<DailyStat> dailyStats;

    public ReadingStatsVO() {
    }

    public int getTodayPages() {
        return todayPages;
    }

    public void setTodayPages(int todayPages) {
        this.todayPages = todayPages;
    }

    public int getWeekPages() {
        return weekPages;
    }

    public void setWeekPages(int weekPages) {
        this.weekPages = weekPages;
    }

    public int getMonthPages() {
        return monthPages;
    }

    public void setMonthPages(int monthPages) {
        this.monthPages = monthPages;
    }

    public int getTotalComics() {
        return totalComics;
    }

    public void setTotalComics(int totalComics) {
        this.totalComics = totalComics;
    }

    public int getTotalTimeSec() {
        return totalTimeSec;
    }

    public void setTotalTimeSec(int totalTimeSec) {
        this.totalTimeSec = totalTimeSec;
    }

    public List<DailyStat> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(List<DailyStat> dailyStats) {
        this.dailyStats = dailyStats;
    }

    public static class DailyStat {
        private String date;
        private int pages;

        public DailyStat() {
        }

        public DailyStat(String date, int pages) {
            this.date = date;
            this.pages = pages;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }
    }
}
