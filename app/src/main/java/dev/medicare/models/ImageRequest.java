package dev.medicare.models;

import java.util.List;

public class ImageRequest {
    Image image;
    List<Feature> features;

    public ImageRequest(Image image, List<Feature> features) {
        this.image = image;
        this.features = features;
    }
}
