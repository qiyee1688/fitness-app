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
      </div>
    </div>
  </section>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { fetch_exercise } from '@/api/exercise'
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
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

onMounted(load_detail)
</script>
