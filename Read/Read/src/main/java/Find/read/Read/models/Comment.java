package Find.read.Read.models;

import java.time.LocalDateTime;

public class Comment {
    private String userId;
    private String content;
    private int rating;  // Add a rating field (e.g., 1-5)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Comment() {}

    public Comment(String userId, String content, int rating) {
        this.userId = userId;
        this.content = content;
        this.rating = rating;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating should be between 1 and 5");
        }
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
