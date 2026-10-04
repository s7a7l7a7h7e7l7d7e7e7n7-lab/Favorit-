package com.example.favorit;

public class FavoriteItem {
    private long id;
    private String title;
    private String details;
    private String category;
    private long timestamp;
    private boolean isFavorite;

    public FavoriteItem(long id, String title, String details, String category, long timestamp, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.category = category;
        this.timestamp = timestamp;
        this.isFavorite = isFavorite;
    }

    public FavoriteItem(String title, String details, String category, long timestamp, boolean isFavorite) {
        this(-1, title, details, category, timestamp, isFavorite);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
