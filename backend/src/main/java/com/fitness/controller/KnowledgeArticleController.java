package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.KnowledgeArticleResponse;
import com.fitness.dto.KnowledgeArticleSummary;
import com.fitness.dto.PageResponse;
import com.fitness.service.KnowledgeArticleService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/knowledge-articles")
@CrossOrigin(origins = "*")
public class KnowledgeArticleController {
    private final KnowledgeArticleService articleService;

    public KnowledgeArticleController(KnowledgeArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<KnowledgeArticleResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ResponseEntity.ok(ApiResponse.success(articleService.listPublished(page, pageSize)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> get(
            @PathVariable @NotBlank String slug
    ) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getPublishedBySlug(slug)));
    }

    @GetMapping("/by-exercise/{exerciseId}")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleSummary>>> listByExercise(
            @PathVariable @NotBlank String exerciseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(articleService.listPublishedByExerciseId(exerciseId)));
    }
}
