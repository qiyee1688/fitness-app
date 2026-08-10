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
            <router-link :to="`/exercises/${prescription.exercise.id}`">
              <strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong>
            </router-link>
            <p>{{ display_value(prescription.exercise.equipment, language) }}</p>
            <p v-if="coach_cue(prescription.exercise)" class="coach-cue">
              {{ coach_cue(prescription.exercise) }}
            </p>
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
          :icon="Refresh"
          :loading="generating"
          @click="replace_workout"
        >{{ t('replaceWorkout') }}</el-button>
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
import { computed, onMounted, ref } from 'vue'
import { CircleCheck, Refresh, VideoPlay } from '@element-plus/icons-vue'

import {
  complete_on_demand_workout,
  generate_on_demand_workout,
  start_on_demand_workout,
} from '@/api/workout'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'
import { fetch_user_profile } from '@/api/user'

const { language, t } = useLanguage()
const body_part = ref('CHEST')
const equipment = ref(['body weight'])
const error = ref('')
const generating = ref(false)
const save_equipment = ref(false)
const transitioning = ref(false)
const variation = ref(0)
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
  variation.value = 0
  await generate_current_variation()
}

async function replace_workout() {
  variation.value += 1
  await generate_current_variation()
}

async function generate_current_variation() {
  generating.value = true
  error.value = ''
  workout.value = null
  try {
    workout.value = await generate_on_demand_workout({
      bodyPart: body_part.value,
      equipment: equipment.value,
      saveEquipmentToProfile: save_equipment.value,
      variation: variation.value,
    })
  } catch (exception) {
    error.value = exception.message
  } finally {
    generating.value = false
  }
}

function coach_cue(exercise) {
  return language.value === 'zh'
    ? exercise.coachCue || exercise.coachCueEn || ''
    : exercise.coachCueEn || exercise.coachCue || ''
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
  } catch (exception) {
    error.value = exception.message
  } finally {
    transitioning.value = false
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
  try {
    const profile = await fetch_user_profile('demo')
    equipment.value = [...new Set(['body weight', ...(profile.availableEquipment || [])])]
  } catch {
    equipment.value = ['body weight']
  }
}

onMounted(load_profile_equipment)
</script>
