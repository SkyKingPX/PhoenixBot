package de.skyking_px.PhoenixBot.faq;

public class FaqEntry {
    private String question;
    private String answer;
    private String imageUrl;
    private String thumbnailUrl;

    public FaqEntry() {
        // Constructor for SnakeYAML
    }

    public FaqEntry(String question, String answer, String imageUrl, String thumbnailUrl) {
        this.question = question;
        this.answer = answer;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
