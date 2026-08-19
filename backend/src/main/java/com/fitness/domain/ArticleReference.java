package com.fitness.domain;

import lombok.Data;

@Data
public class ArticleReference {
    private String articleId;
    private String exerciseId;
    private int displayOrder;
    private Exercise exercise;
}
