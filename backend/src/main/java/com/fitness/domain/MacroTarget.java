package com.fitness.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record MacroTarget(
        MacroTargetValue protein,
        MacroTargetValue carbs,
        MacroTargetValue fat,
        MacroTargetValue kcal
) {
    @JsonIgnore
    public boolean isEmpty() {
        return protein == null && carbs == null && fat == null && kcal == null;
    }
}
