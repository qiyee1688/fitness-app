<template>
  <section class="page food-page">
    <div class="detail-backbar">
      <el-button class="detail-back-button" type="primary" plain @click="router.push({ name: 'food-item-list' })">
        {{ t('backToPrevious') }}
      </el-button>
    </div>

    <el-alert v-if="load_error" :title="load_error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="10" animated />
    <template v-else-if="food_item">
      <header class="food-detail-header">
        <p class="eyebrow">{{ food_category(food_item.category) }}</p>
        <h1>{{ food_name(food_item) }}</h1>
        <p class="page-subtitle">{{ t('foodStandardServing') }}: {{ food_serving(food_item) }} · {{ food_item.servingGrams }} {{ t('nutritionGrams') }}</p>
      </header>

      <section class="food-conversion-panel" aria-labelledby="food-conversion-title">
        <div>
          <p class="eyebrow">{{ t('foodConversionEyebrow') }}</p>
          <h2 id="food-conversion-title">{{ t('foodConversionTitle') }}</h2>
          <p>{{ t('foodConversionHint') }}</p>
        </div>
        <div class="food-conversion-controls">
          <label>
            <span>{{ t('foodServings') }}</span>
            <el-input-number v-model="servings" :min="0.1" :max="100" :step="0.25" :precision="2" controls-position="right" />
          </label>
          <el-button type="primary" :loading="converting" @click="convert">{{ t('foodConvert') }}</el-button>
        </div>
        <el-alert v-if="conversion_error" :title="conversion_error" type="error" show-icon :closable="false" />
      </section>

      <section v-if="conversion" class="food-result" aria-live="polite">
        <div class="food-result-summary">
          <p class="eyebrow">{{ t('foodConversionResult') }}</p>
          <h2>{{ food_serving(conversion.foodItem) }} × {{ conversion.servings }}</h2>
        </div>
        <dl class="food-macro-grid">
          <div>
            <dt>{{ t('nutritionProtein') }}</dt>
            <dd>{{ conversion.macroTargets.protein.value }} {{ t('nutritionGrams') }}</dd>
          </div>
          <div>
            <dt>{{ t('nutritionCarbs') }}</dt>
            <dd>{{ conversion.macroTargets.carbs.value }} {{ t('nutritionGrams') }}</dd>
          </div>
          <div>
            <dt>{{ t('nutritionFat') }}</dt>
            <dd>{{ conversion.macroTargets.fat.value }} {{ t('nutritionGrams') }}</dd>
          </div>
          <div>
            <dt>{{ t('nutritionKcal') }}</dt>
            <dd>{{ conversion.macroTargets.kcal.value }} {{ t('nutritionKilocalories') }}</dd>
          </div>
        </dl>
      </section>
      <el-empty v-else-if="!conversion_error" :description="t('foodResultEmpty')" />
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { convert_food_item, fetch_food_item } from '@/api/food'
import { useLanguage } from '@/composables/useLanguage'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const { language, t } = useLanguage()
const food_item = ref(null)
const loading = ref(false)
const load_error = ref('')
const conversion_error = ref('')
const conversion = ref(null)
const converting = ref(false)
const servings = ref(1)

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

async function load_food_item() {
  loading.value = true
  load_error.value = ''
  try {
    food_item.value = await fetch_food_item(props.id)
  } catch (exception) {
    load_error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function convert() {
  conversion_error.value = ''
  conversion.value = null
  if (!Number.isFinite(servings.value) || servings.value <= 0 || servings.value > 100) {
    conversion_error.value = t('foodServingsInvalid')
    return
  }
  converting.value = true
  try {
    conversion.value = await convert_food_item(props.id, servings.value)
  } catch (exception) {
    conversion_error.value = exception.message
  } finally {
    converting.value = false
  }
}

onMounted(load_food_item)
</script>
