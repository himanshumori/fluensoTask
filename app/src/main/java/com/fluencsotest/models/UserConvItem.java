package com.fluencsotest.models;

public class UserConvItem extends ConvEntityItem {

    private int rating;
    private String feedBackText;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedBackText() {
        return feedBackText;
    }

    public void setFeedBackText(String feedBackText) {
        this.feedBackText = feedBackText;
    }
}
