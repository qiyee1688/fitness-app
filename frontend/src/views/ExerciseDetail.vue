<template>
  <section class="page detail-page">
    <div class="detail-backbar">
      <el-button
        :icon="ArrowLeft"
        class="detail-back-button"
        size="large"
        type="primary"
        plain
        @click="go_back"
      >{{ t('backToPrevious') }}</el-button>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="10" animated />

    <div v-else-if="exercise" class="detail-layout">
      <div class="media-panel">
        <img
          v-if="media_source"
          :src="media_source"
          :alt="display_exercise_name(exercise.name, language)"
          @error="media_broken = true"
        />
        <div v-else class="media-empty">{{ display_value(exercise.bodyPart, language) }}</div>
        <span class="media-label">{{ display_value(exercise.bodyPart, language) || t('exerciseFallback') }}</span>
      </div>

      <div class="detail-content">
        <p class="eyebrow">{{ display_value(exercise.bodyPart, language) }} / {{ display_value(exercise.target, language) }}</p>
        <h1>{{ display_exercise_name(exercise.name, language) }}</h1>
        <div class="tags">
          <el-tag>{{ display_value(exercise.bodyPart, language) }}</el-tag>
          <el-tag type="success">{{ display_value(exercise.target, language) }}</el-tag>
          <el-tag type="info">{{ display_value(exercise.equipment, language) }}</el-tag>
        </div>

        <section v-if="coach_cue" class="exercise-coach-cue">
          <h2>{{ t('coachCue') }}</h2>
          <p>{{ coach_cue }}</p>
        </section>

        <dl class="meta-grid">
          <div>
            <dt>{{ t('category') }}</dt>
            <dd>{{ display_value(exercise.category, language) }}</dd>
          </div>
          <div>
            <dt>{{ t('bodyPart') }}</dt>
            <dd>{{ display_value(exercise.bodyPart, language) }}</dd>
          </div>
          <div>
            <dt>{{ t('target') }}</dt>
            <dd>{{ display_value(exercise.target, language) }}</dd>
          </div>
          <div>
            <dt>{{ t('equipment') }}</dt>
            <dd>{{ display_value(exercise.equipment, language) }}</dd>
          </div>
          <div>
            <dt>{{ t('muscleGroup') }}</dt>
            <dd>{{ display_value(exercise.muscleGroup, language) }}</dd>
          </div>
          <div>
            <dt>{{ t('secondary') }}</dt>
            <dd>{{ secondary_muscles }}</dd>
          </div>
        </dl>

        <el-divider />

        <h2>{{ t('steps') }}</h2>
        <ol v-if="steps.length" class="steps">
          <li v-for="step in steps" :key="step">{{ step }}</li>
        </ol>
        <el-empty v-else :description="t('emptySteps')" />

        <section class="related-articles">
          <div class="section-heading">
            <div>
              <p class="eyebrow">{{ t('knowledgeArticleEyebrow') }}</p>
              <h2>{{ t('relatedArticles') }}</h2>
            </div>
          </div>
          <el-alert v-if="articles_error" :title="articles_error" type="error" show-icon :closable="false" />
          <el-skeleton v-else-if="articles_loading" :rows="2" animated />
          <el-empty v-else-if="!related_articles.length" :description="t('emptyRelatedArticles')" />
          <div v-else class="related-article-list">
            <router-link
              v-for="article in related_articles"
              :key="article.articleId"
              class="related-article-card"
              :to="{ name: 'knowledge-article-detail', params: { slug: article.slug }, query: { from: 'exercise' } }"
            >
              <div>
                <h3>{{ article_title(article) }}</h3>
                <p>{{ article_summary(article) }}</p>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { fetch_exercise } from '@/api/exercise'
import { fetch_articles_for_exercise } from '@/api/article'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_list, display_value } from '@/utils/exerciseDisplay'

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
})

const route = useRoute()
const router = useRouter()
const { language, t } = useLanguage()
const exercise = ref(null)
const error = ref('')
const loading = ref(false)
const media_broken = ref(false)
const related_articles = ref([])
const articles_loading = ref(false)
const articles_error = ref('')

const media_source = computed(() => {
  if (media_broken.value) {
    return ''
  }

  return exercise.value?.gifUrl || exercise.value?.imageUrl || ''
})

const secondary_muscles = computed(() => {
  const muscles = exercise.value?.secondaryMuscles || []
  return display_list(muscles, language.value)
})

const coach_cue = computed(() => {
  if (!exercise.value) {
    return ''
  }
  return language.value === 'zh'
    ? exercise.value.coachCue || exercise.value.coachCueEn || ''
    : exercise.value.coachCueEn || exercise.value.coachCue || ''
})

const steps = computed(() => {
  const instruction_steps = exercise.value?.instructionSteps
  if (!instruction_steps) {
    return []
  }
  if (language.value === 'zh') {
    return instruction_steps.zh || instruction_steps.en || Object.values(instruction_steps)[0] || []
  }

  return instruction_steps.en || instruction_steps.zh || Object.values(instruction_steps)[0] || []
})

function article_title(article) {
  return language.value === 'zh' ? article.title || article.titleEn : article.titleEn || article.title
}

function article_summary(article) {
  return language.value === 'zh' ? article.summary || article.summaryEn : article.summaryEn || article.summary
}

function go_back() {
  if (route.query.from === 'on-demand') {
    router.push({ name: 'on-demand-workout' })
    return
  }

  router.back()
}

async function load_detail() {
  loading.value = true
  error.value = ''
  media_broken.value = false
  try {
    exercise.value = await fetch_exercise(props.id)
    await load_related_articles()
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function load_related_articles() {
  articles_loading.value = true
  articles_error.value = ''
  try {
    related_articles.value = await fetch_articles_for_exercise(props.id)
  } catch (exception) {
    articles_error.value = exception.message
  } finally {
    articles_loading.value = false
  }
}

onMounted(load_detail)
</script>
