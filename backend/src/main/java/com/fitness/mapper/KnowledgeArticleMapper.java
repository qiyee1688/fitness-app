package com.fitness.mapper;

import com.fitness.domain.ArticleReference;
import com.fitness.domain.KnowledgeArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeArticleMapper {
    List<KnowledgeArticle> findPublished(@Param("offset") int offset, @Param("limit") int limit);

    int countPublished();

    KnowledgeArticle findPublishedBySlug(@Param("slug") String slug);

    List<ArticleReference> findPublishedReferences(@Param("articleId") String articleId);
}
