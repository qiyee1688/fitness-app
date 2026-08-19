package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeArticle {
    private String id;
    private String editorId;
    private String slug;
    private String title;
    private String titleEn;
    private String summary;
    private String summaryEn;
    private String body;
    private String bodyEn;
    private String coverImageUrl;
    private KnowledgeArticleStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Editor editor;
    private List<ArticleReference> references;
}
