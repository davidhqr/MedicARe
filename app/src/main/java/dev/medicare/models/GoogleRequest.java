package dev.medicare.models;

import java.util.List;

public class GoogleRequest {
    private List<ImageRequest> requests;

    public GoogleRequest(List<ImageRequest> requests) {
        this.requests = requests;
    }
}
