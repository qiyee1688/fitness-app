<template>
  <section class="page article-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('knowledgeArticleEyebrow') }}</p>
        <h1>{{ t('knowledgeArticleTitle') }}</h1>
        <p class="page-subtitle">{{ t('knowledgeArticleSubtitle') }}</p>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="!articles.length" :description="t('emptyArticles')" />
    <div v-else class="article-grid">
      <router-link
        v-for="article in articles"
        :key="article.articleId"
        class="article-card"
        :to="{ name: 'knowledge-article-detail', params: { slug: article.slug } }"
      >
        <img v-if="article.coverImageUrl" :src="article.coverImageUrl" :alt="article_title(article)" />
        <div class="article-card-body">
          <p class="eyebrow">{{ t('knowledgeArticlePublished') }}</p>
          <h2>{{ article_title(article) }}</h2>
          <p>{{ article_summary(article) }}</p>
        </div>
      </router-link>
    </div>
    <el-pagination
      v-if="total > page_size"
      v-model:current-page="page"
      :page-size="page_size"
      :total="total"
      layout="prev, pager, next"
      @current-change="load_articles"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { fetch_articles } from '@/api/article'
import { useLanguage } from '@/composables/useLanguage'

const { language, t } = useLanguage()
const articles = ref([])
const error = ref('')
const loading = ref(false)
const page = ref(1)
const page_size = 12
const total = ref(0)

function article_title(article) {
  return language.value === 'zh' ? article.title || article.titleEn : article.titleEn || article.title
}

function article_summary(article) {
  return language.value === 'zh' ? article.summary || article.summaryEn : article.summaryEn || article.summary
}

async function load_articles() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetch_articles({ page: page.value, pageSize: page_size })
    articles.value = data.items || []
    total.value = data.total || 0
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

onMounted(load_articles)
</script>
