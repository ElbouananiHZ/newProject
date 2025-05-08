package Find.read.Read.models;

public class Rating {
    private String userId;
    private String novelId;
    private int rating;  // Add a rating field (e.g., 1-5)
    private String ratingContent;  // Optional field for a textual review

    // Constructors


    public Rating(String userId, String novelId, int rating, String ratingContent) {
        this.userId = userId;
        this.novelId = novelId;
        this.rating = rating;
        this.ratingContent = ratingContent;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNovelId() {
        return novelId;
    }

    public void setNovelId(String novelId) {
        this.novelId = novelId;
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

    public String getRatingContent() {
        return ratingContent;
    }

    public void setRatingContent(String reviewContent) {
        this.ratingContent = reviewContent;
    }
}

