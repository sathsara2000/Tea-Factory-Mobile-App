package com.example.teafcatory;

public class PredictionItem {
    private String imageUrl;
    private String predictionResult;

    public PredictionItem(String imageUrl, String predictionResult) {
        this.imageUrl = imageUrl;
        this.predictionResult = predictionResult;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPredictionResult() {
        return predictionResult;
    }
}
