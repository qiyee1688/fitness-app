<template>
  <section class="page article-page">
    <div class="detail-backbar">
      <el-button class="detail-back-button" type="primary" plain @click="go_back">{{ t('backToPrevious') }}</el-button>
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="12" animated />
    <article v-else-if="article" class="article-reader">
      <p class="eyebrow">{{ t('knowledgeArticlePublished') }}</p>
      <h1>{{ article_title(article) }}</h1>
      <p class="article-summary">{{ article_summary(article) }}</p>
      <img v-if="article.coverImageUrl" class="article-cover" :src="article.coverImageUrl" :alt="article_title(article)" />
      <div class="article-body">{{ article_body(article) }}</div>

      <section class="article-references">
        <h2>{{ t('articleReferences') }}</h2>
        <el-empty v-if="!article.references?.length" :description="t('emptyArticleReferences')" />
        <div v-else class="article-reference-grid">
          <router-link
            v-for="reference in article.references"
            :key="reference.exerciseId"
            class="article-reference-card"
            :to="{ name: 'exercise-detail', params: { id: reference.exerciseId }, query: { from: 'article' } }"
          >
            <img v-if="reference.exercise?.gifUrl || reference.exercise?.imageUrl" :src="reference.exercise.gifUrl || reference.exercise.imageUrl" :alt="reference.exercise.name" />
            <div>
              <p class="eyebrow">{{ t('exerciseReference') }} {{ reference.displayOrder }}</p>
              <h3>{{ reference.exercise?.name || reference.exerciseId }}</h3>
            </div>
          </router-link>
        </div>
      </section>

      <section v-if="article.foodItems?.length" class="article-food-references">
        <h2>{{ t('articleFoodReferences') }}</h2>
        <div class="article-food-reference-grid">
          <router-link
            v-for="item in article.foodItems"
            :key="item.id"
            class="article-food-reference-card"
            :to="{ name: 'food-item-detail', params: { id: item.id } }"
          >
            <div>
              <p class="eyebrow">{{ t('articleFoodReference') }}</p>
              <h3>{{ food_name(item) }}</h3>
              <p>{{ food_serving(item) }}</p>
            </div>
            <span>{{ t('foodConvert') }}</span>
          </router-link>
        </div>
      </section>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { fetch_article } from '@/api/article'
import { useLanguage } from '@/composables/useLanguage'

const props = defineProps({ slug: { type: String, required: true } })
const route = useRoute()
const router = useRouter()
const { language, t } = useLanguage()
const article = ref(null)
const error = ref('')
const loading = ref(false)

function article_title(value) {
  return language.value === 'zh' ? value.title || value.titleEn : value.titleEn || value.title
}

function article_summary(value) {
  return language.value === 'zh' ? value.summary || value.summaryEn : value.summaryEn || value.summary
}

function article_body(value) {
  return language.value === 'zh' ? value.body || value.bodyEn : value.bodyEn || value.body
}

function food_name(item) {
  return language.value === 'zh' ? item.name || item.nameEn : item.nameEn || item.name
}

function food_serving(item) {
  return language.value === 'zh'
    ? item.servingDescription || item.servingDescriptionEn
    : item.servingDescriptionEn || item.servingDescription
}

function go_back() {
  if (route.query.from === 'exercise') {
    router.back()
    return
  }
  router.push({ name: 'knowledge-article-list' })
}

async function load_article() {
  loading.value = true
  error.value = ''
  try {
    article.value = await fetch_article(props.slug)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

onMounted(load_article)
</script>
