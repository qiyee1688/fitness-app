<template>
  <section class="page on-demand-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('onDemandEyebrow') }}</p>
        <h1>{{ t('onDemandTitle') }}</h1>
        <p class="page-subtitle">{{ t('onDemandSubtitle') }}</p>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-alert
      v-if="saved_template"
      :title="t('templateSaved')"
      type="success"
      show-icon
      :closable="false"
    />

    <section class="on-demand-controls">
      <el-form label-position="top" @submit.prevent>
        <el-form-item :label="t('trainingBodyPart')">
          <el-segmented v-model="body_part" :options="body_part_options" />
        </el-form-item>
        <el-form-item :label="t('equipmentForThisWorkout')">
          <el-checkbox-group v-model="equipment" class="equipment-grid">
            <el-checkbox-button
              v-for="option in equipment_options"
              :key="option.value"
              :value="option.value"
              :disabled="option.value === 'body weight'"
            >
              {{ option.label }}
            </el-checkbox-button>
          </el-checkbox-group>
        </el-form-item>
        <el-checkbox v-model="save_equipment">{{ t('saveEquipmentToProfile') }}</el-checkbox>
        <div class="on-demand-actions">
          <el-button type="primary" :loading="generating" @click="generate_workout">
            {{ generating ? t('generatingWorkout') : t('generateWorkout') }}
          </el-button>
        </div>
      </el-form>
    </section>

    <el-skeleton v-if="generating" :rows="6" animated />

    <section v-else-if="workout" class="generated-workout">
      <header class="generated-workout-header">
        <div>
          <p class="eyebrow">{{ body_part_label(workout.bodyPart) }}</p>
          <h2>{{ t('generatedWorkout') }}</h2>
        </div>
        <el-tag :type="status_type(workout.status)">{{ status_label(workout.status) }}</el-tag>
      </header>

      <div class="on-demand-prescriptions">
        <article v-for="prescription in workout.prescriptions" :key="prescription.prescriptionId" class="on-demand-prescription">
          <span class="sequence">{{ prescription.sequence }}</span>
          <div>
            <router-link :to="exercise_detail_link(prescription.exercise.id)">
              <strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong>
            </router-link>
            <p>{{ display_value(prescription.exercise.equipment, language) }}</p>
          </div>
          <dl>
            <div><dt>{{ t('sets') }}</dt><dd>{{ prescription.sets }}</dd></div>
            <div><dt>{{ t('reps') }}</dt><dd>{{ prescription.reps }}</dd></div>
            <div><dt>{{ t('rpe') }}</dt><dd>{{ prescription.rpe }}</dd></div>
          </dl>
        </article>
      </div>

      <div class="on-demand-actions">
        <el-button
          v-if="workout.status === 'DRAFT'"
          :loading="saving_template"
          :disabled="Boolean(saved_template)"
          @click="save_template"
        >{{ saved_template ? t('templateSavedShort') : t('saveAsTemplate') }}</el-button>
        <el-button
          v-if="workout.status === 'DRAFT'"
          type="primary"
          :icon="VideoPlay"
          :loading="transitioning"
          @click="start_workout"
        >{{ t('startWorkout') }}</el-button>
        <el-button
          v-else-if="workout.status === 'IN_PROGRESS'"
          type="success"
          :icon="CircleCheck"
          :loading="transitioning"
          @click="complete_workout"
        >{{ t('completeWorkout') }}</el-button>
        <el-alert
          v-else-if="workout.status === 'COMPLETED'"
          :title="t('workoutCompleted')"
          type="success"
          show-icon
          :closable="false"
        />
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { CircleCheck, VideoPlay } from '@element-plus/icons-vue'

import {
  complete_on_demand_workout,
  generate_on_demand_workout,
  save_workout_template,
  start_on_demand_workout,
} from '@/api/workout'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'
import { fetch_user_profile } from '@/api/user'

const ON_DEMAND_CACHE_KEY = 'fitness:on-demand-workout:last-result'

const { language, t } = useLanguage()
const body_part = ref('CHEST')
const equipment = ref(['body weight'])
const error = ref('')
const generating = ref(false)
const save_equipment = ref(false)
const saved_template = ref(null)
const saving_template = ref(false)
const transitioning = ref(false)
const workout = ref(null)

const body_part_options = computed(() => [
  { label: t('chest'), value: 'CHEST' },
  { label: t('backTraining'), value: 'BACK' },
  { label: t('shouldersTraining'), value: 'SHOULDERS' },
  { label: t('legsTraining'), value: 'LEGS' },
  { label: t('coreTraining'), value: 'WAIST' },
])

const equipment_options = computed(() => [
  { label: t('bodyWeight'), value: 'body weight' },
  { label: t('dumbbell'), value: 'dumbbell' },
  { label: t('barbell'), value: 'barbell' },
  { label: t('kettlebell'), value: 'kettlebell' },
  { label: t('band'), value: 'band' },
  { label: t('cable'), value: 'cable' },
])

async function generate_workout() {
  generating.value = true
  error.value = ''
  workout.value = null
  saved_template.value = null
  persist_cached_workout()
  try {
    workout.value = await generate_on_demand_workout({
      bodyPart: body_part.value,
      equipment: equipment.value,
      saveEquipmentToProfile: save_equipment.value,
    })
    persist_cached_workout()
  } catch (exception) {
    error.value = exception.message
  } finally {
    generating.value = false
  }
}

async function save_template() {
  saving_template.value = true
  error.value = ''
  try {
    saved_template.value = await save_workout_template({
      sourceWorkoutId: workout.value.workoutId,
      name: template_name(),
    })
    persist_cached_workout()
  } catch (exception) {
    error.value = exception.message
  } finally {
    saving_template.value = false
  }
}

function template_name() {
  return `${t('customTemplateName')} - ${body_part_label(workout.value.bodyPart)}`
}

async function start_workout() {
  await transition(() => start_on_demand_workout(workout.value.workoutId))
}

async function complete_workout() {
  await transition(() => complete_on_demand_workout(workout.value.workoutId))
}

async function transition(action) {
  transitioning.value = true
  error.value = ''
  try {
    workout.value = await action()
    persist_cached_workout()
  } catch (exception) {
    error.value = exception.message
  } finally {
    transitioning.value = false
  }
}

function exercise_detail_link(exercise_id) {
  persist_cached_workout()
  return {
    name: 'exercise-detail',
    params: { id: exercise_id },
    query: { from: 'on-demand' },
  }
}

function persist_cached_workout() {
  if (typeof sessionStorage === 'undefined') {
    return
  }

  if (!workout.value) {
    sessionStorage.removeItem(ON_DEMAND_CACHE_KEY)
    return
  }

  sessionStorage.setItem(ON_DEMAND_CACHE_KEY, JSON.stringify({
    bodyPart: body_part.value,
    equipment: equipment.value,
    saveEquipmentToProfile: save_equipment.value,
    workout: workout.value,
    savedTemplate: saved_template.value,
  }))
}

function restore_cached_workout() {
  if (typeof sessionStorage === 'undefined') {
    return false
  }

  const cached = sessionStorage.getItem(ON_DEMAND_CACHE_KEY)
  if (!cached) {
    return false
  }

  try {
    const parsed = JSON.parse(cached)
    if (!parsed?.workout?.workoutId) {
      return false
    }
    body_part.value = parsed.bodyPart || body_part.value
    equipment.value = Array.isArray(parsed.equipment) && parsed.equipment.length
      ? parsed.equipment
      : equipment.value
    save_equipment.value = Boolean(parsed.saveEquipmentToProfile)
    workout.value = parsed.workout
    saved_template.value = parsed.savedTemplate || null
    return true
  } catch {
    sessionStorage.removeItem(ON_DEMAND_CACHE_KEY)
    return false
  }
}

function body_part_label(value) {
  const key = { CHEST: 'chest', BACK: 'backTraining', SHOULDERS: 'shouldersTraining', LEGS: 'legsTraining', WAIST: 'coreTraining' }[value]
  return t(key || value)
}

function status_label(value) {
  return t({ DRAFT: 'workoutDraft', IN_PROGRESS: 'workoutInProgress', COMPLETED: 'completed' }[value] || value)
}

function status_type(value) {
  return { DRAFT: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success' }[value] || 'info'
}

async function load_profile_equipment() {
  if (restore_cached_workout()) {
    return
  }

  try {
    const profile = await fetch_user_profile('demo')
    equipment.value = [...new Set(['body weight', ...(profile.availableEquipment || [])])]
  } catch {
    equipment.value = ['body weight']
  }
}

watch([body_part, equipment, save_equipment], () => {
  if (!workout.value) {
    return
  }
  persist_cached_workout()
}, { deep: true })

onMounted(load_profile_equipment)
</script>
