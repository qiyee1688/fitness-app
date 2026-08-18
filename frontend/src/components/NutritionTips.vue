<template>
  <section v-if="tips.length" class="nutrition-tips" :aria-label="t('nutritionTips')">
    <div class="nutrition-tips-heading">
      <div>
        <p class="eyebrow">{{ t('nutritionTips') }}</p>
        <h3>{{ t('nutritionTipsTitle') }}</h3>
      </div>
      <span class="nutrition-tip-count">{{ tips.length }}/3</span>
    </div>

    <div class="nutrition-tip-list">
      <article v-for="tip in tips" :key="tip.tipId" class="nutrition-tip">
        <header class="nutrition-tip-header">
          <el-tag effect="plain" type="success">{{ timing_label(tip.timing) }}</el-tag>
          <span v-if="tip.weightKgSnapshot" class="nutrition-tip-snapshot">
            {{ tip.weightKgSnapshot }} kg {{ t('nutritionTipSnapshot') }}
          </span>
        </header>

        <div class="macro-target-grid">
          <div v-for="entry in macro_entries(tip.macroTargets)" :key="entry.key" class="macro-target">
            <span class="macro-target-label">{{ t(entry.labelKey) }}</span>
            <strong>{{ format_value(entry.value) }} <small>{{ unit_label(entry.unit) }}</small></strong>
            <span class="macro-target-basis">{{ basis_label(entry.basis) }}</span>
          </div>
        </div>

        <p v-if="tip_note(tip)" class="nutrition-tip-note">{{ tip_note(tip) }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { useLanguage } from '@/composables/useLanguage'

defineProps({
  tips: {
    type: Array,
    default: () => [],
  },
})

const { language, t } = useLanguage()

const macro_definitions = [
  { key: 'protein', labelKey: 'nutritionProtein' },
  { key: 'carbs', labelKey: 'nutritionCarbs' },
  { key: 'fat', labelKey: 'nutritionFat' },
  { key: 'kcal', labelKey: 'nutritionKcal' },
]

function macro_entries(targets) {
  if (!targets) return []
  return macro_definitions
    .map((definition) => ({ ...definition, ...(targets[definition.key] || {}) }))
    .filter((entry) => entry.value !== null && entry.value !== undefined)
}

function timing_label(timing) {
  return t({
    PRE_WORKOUT: 'nutritionPreWorkout',
    POST_WORKOUT: 'nutritionPostWorkout',
    DAILY: 'nutritionDaily',
  }[timing] || timing)
}

function unit_label(unit) {
  return t({ GRAMS: 'nutritionGrams', KILOCALORIES: 'nutritionKilocalories' }[unit] || unit)
}

function basis_label(basis) {
  return t({
    ABSOLUTE: 'nutritionAbsolute',
    PER_KG_BODYWEIGHT: 'nutritionPerKgBodyweight',
  }[basis] || basis)
}

function format_value(value) {
  const numeric_value = Number(value)
  if (!Number.isFinite(numeric_value)) return value
  return Number.isInteger(numeric_value) ? numeric_value : numeric_value.toFixed(1)
}

function tip_note(tip) {
  return language.value === 'zh' ? (tip.note || tip.noteEn) : (tip.noteEn || tip.note)
}
</script>
