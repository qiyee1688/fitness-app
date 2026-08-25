<template>
  <section class="page food-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('foodEyebrow') }}</p>
        <h1>{{ t('foodTitle') }}</h1>
        <p class="page-subtitle">{{ t('foodSubtitle') }}</p>
      </div>
    </div>

    <form class="food-search" @submit.prevent="load_food_items">
      <el-input v-model="query" :aria-label="t('foodSearchPlaceholder')" :placeholder="t('foodSearchPlaceholder')" clearable />
      <el-button type="primary" native-type="submit" :loading="loading">{{ t('search') }}</el-button>
    </form>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="8" animated />
    <el-empty v-else-if="!food_items.length" :description="t('emptyFoodItems')" />
    <div v-else class="food-grid">
      <router-link
        v-for="item in food_items"
        :key="item.id"
        class="food-card"
        :to="{ name: 'food-item-detail', params: { id: item.id } }"
      >
        <p class="eyebrow">{{ food_category(item.category) }}</p>
        <h2>{{ food_name(item) }}</h2>
        <p>{{ food_serving(item) }}</p>
        <span>{{ item.kcal }} {{ t('nutritionKilocalories') }}</span>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { fetch_food_items } from '@/api/food'
import { useLanguage } from '@/composables/useLanguage'

const { language, t } = useLanguage()
const food_items = ref([])
const query = ref('')
const error = ref('')
const loading = ref(false)

function food_name(item) {
  return language.value === 'zh' ? item.name || item.nameEn : item.nameEn || item.name
}

function food_serving(item) {
  return language.value === 'zh'
    ? item.servingDescription || item.servingDescriptionEn
    : item.servingDescriptionEn || item.servingDescription
}

function food_category(category) {
  return t(`foodCategory${category}`)
}

async function load_food_items() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetch_food_items({ query: query.value, pageSize: 100 })
    food_items.value = data.items || []
  } catch (exception) {
    food_items.value = []
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

onMounted(load_food_items)
</script>
