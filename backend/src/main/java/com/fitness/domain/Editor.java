package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Editor {
    private String id;
    private String displayName;
    private String displayNameEn;
    private LocalDateTime createdAt;
}
