package com.fitness.domain;

import java.util.List;

public enum OnDemandBodyPart {
    CHEST(4, List.of("chest")),
    BACK(5, List.of("back")),
    SHOULDERS(4, List.of("shoulders")),
    LEGS(6, List.of("upper legs", "lower legs")),
    WAIST(3, List.of("waist"));

    private final int exerciseCount;
    private final List<String> datasetValues;

    OnDemandBodyPart(int exerciseCount, List<String> datasetValues) {
        this.exerciseCount = exerciseCount;
        this.datasetValues = datasetValues;
    }

    public int exerciseCount() {
        return exerciseCount;
    }

    public List<String> datasetValues() {
        return datasetValues;
    }
}
