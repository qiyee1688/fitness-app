<template>
  <section class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('exerciseEyebrow') }}</p>
        <h1>{{ t('exerciseTitle') }}</h1>
      </div>
      <el-input
        v-model="keyword"
        class="search"
        clearable
        :placeholder="t('searchPlaceholder')"
        @keyup.enter="load_search"
        @clear="load_page"
      >
        <template #append>
          <el-button :icon="Search" @click="load_search" />
        </template>
      </el-input>
    </div>

    <el-radio-group
      v-model="selected_body_part"
      class="body-tabs"
      @change="change_body_part"
    >
      <el-radio-button
        v-for="filter in body_part_filters"
        :key="filter.value"
        :label="filter.value"
      >
        {{ t(filter.label_key) }}
      </el-radio-button>
    </el-radio-group>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />

    <el-skeleton v-if="loading" :rows="8" animated />

    <el-empty v-else-if="!exercises.length" :description="t('emptyExercises')" />

    <div v-else class="exercise-grid">
      <article v-for="exercise in exercises" :key="exercise.id" class="exercise-card">
        <div class="card-media">
          <img
            v-if="card_media_source(exercise)"
            :src="card_media_source(exercise)"
            :alt="display_exercise_name(exercise.name, language)"
            @error="mark_media_broken(exercise.id)"
          />
          <div v-else class="card-media-empty">{{ display_value(exercise.bodyPart, language) || t('exerciseFallback') }}</div>
          <span class="media-label">{{ display_value(exercise.bodyPart, language) || t('exerciseFallback') }}</span>
        </div>
        <div class="card-body">
          <h2>{{ display_exercise_name(exercise.name, language) }}</h2>
          <div class="tags">
            <el-tag size="small">{{ display_value(exercise.bodyPart, language) }}</el-tag>
            <el-tag size="small" type="success">{{ display_value(exercise.target, language) }}</el-tag>
            <el-tag size="small" type="info">{{ display_value(exercise.equipment, language) }}</el-tag>
          </div>
          <el-button :icon="ArrowRight" text @click="go_detail(exercise.id)">{{ t('view') }}</el-button>
        </div>
      </article>
    </div>

    <div v-if="!loading && total > page_size" class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="page_size"
        :total="total"
        layout="prev, pager, next"
        @current-change="load_page"
      />
    </div>
  </section>
</template>

<script setup>
import { ArrowRight, Search } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetch_exercises, search_exercises } from '@/api/exercise'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'

const router = useRouter()
const { language, t } = useLanguage()
const exercises = ref([])
const error = ref('')
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const page_size = 20
const selected_body_part = ref('')
const total = ref(0)
const broken_media_ids = ref(new Set())

const body_part_filters = [
  { label_key: 'all', value: '' },
  { label_key: 'chest', value: 'chest' },
  { label_key: 'backTraining', value: 'back' },
  { label_key: 'shouldersTraining', value: 'shoulders' },
  { label_key: 'legsTraining', value: 'upper legs,lower legs' },
  { label_key: 'coreTraining', value: 'waist' },
]

async function load_page() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetch_exercises({
      page: page.value,
      pageSize: page_size,
      bodyPart: selected_body_part.value,
    })
    exercises.value = data.items
    total.value = data.total
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function load_search() {
  if (!keyword.value.trim()) {
    await load_page()
    return
  }

  selected_body_part.value = ''
  page.value = 1
  loading.value = true
  error.value = ''
  try {
    exercises.value = await search_exercises(keyword.value.trim(), page_size)
    total.value = exercises.value.length
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function change_body_part() {
  keyword.value = ''
  page.value = 1
  await load_page()
}

function go_detail(id) {
  router.push({ name: 'exercise-detail', params: { id } })
}

function card_media_source(exercise) {
  if (broken_media_ids.value.has(exercise.id)) {
    return ''
  }

  return exercise.imageUrl || exercise.gifUrl || ''
}

function mark_media_broken(id) {
  const next_ids = new Set(broken_media_ids.value)
  next_ids.add(id)
  broken_media_ids.value = next_ids
}

onMounted(load_page)
</script>
