<template>
  <section class="page today-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('todayEyebrow') }}</p>
        <h1>{{ t('todayTitle') }}</h1>
        <p class="page-subtitle">{{ t('todaySubtitle') }}</p>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-if="loading" :rows="9" animated />

    <el-empty v-else-if="no_active_plan" :description="t('noActivePlan')">
      <p class="empty-hint">{{ t('noActivePlanHint') }}</p>
      <el-button type="primary" @click="router.push('/profile')">{{ t('goToProfile') }}</el-button>
    </el-empty>

    <el-empty v-else-if="rest_day" :description="t('restDay')">
      <p class="empty-hint">{{ t('restDayHint') }}</p>
      <el-button @click="router.push('/plan')">{{ t('viewFullPlan') }}</el-button>
    </el-empty>

    <template v-else-if="workout">
      <section class="today-hero" :class="{ completed: workout.completedAt }">
        <div>
          <el-tag :type="workout.completedAt ? 'success' : 'primary'" effect="dark">
            {{ workout.completedAt ? t('completed') : t('readyToTrain') }}
          </el-tag>
          <p class="today-date">{{ format_date(workout.scheduledDate) }}</p>
          <h2>{{ focus_label(workout.focus) }}</h2>
          <p>{{ day_label(workout.dayNumber) }} · {{ workout.prescriptions.length }} {{ t('exercises') }}</p>
        </div>
        <div class="completion-action">
          <template v-if="workout.completedAt">
            <strong>{{ t('workoutCompleted') }}</strong>
            <span>{{ format_time(workout.completedAt) }}</span>
          </template>
          <el-button v-else type="primary" size="large" :loading="completing" @click="complete_today_workout">
            {{ completing ? t('completing') : t('completeWorkout') }}
          </el-button>
        </div>
      </section>

      <section class="today-prescriptions">
        <router-link
          v-for="prescription in workout.prescriptions"
          :key="prescription.prescriptionId"
          class="today-prescription-card"
          :to="`/exercises/${prescription.exercise.id}`"
        >
          <span class="sequence">{{ prescription.sequence }}</span>
          <span class="exercise-copy">
            <strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong>
            <small>{{ display_value(prescription.exercise.bodyPart, language) }} · {{ display_value(prescription.exercise.equipment, language) }}</small>
          </span>
          <dl class="prescription-metrics">
            <div><dt>{{ t('sets') }}</dt><dd>{{ prescription.sets }}</dd></div>
            <div><dt>{{ t('reps') }}</dt><dd>{{ prescription.reps }}</dd></div>
            <div><dt>{{ t('rpe') }}</dt><dd>{{ prescription.rpe }}</dd></div>
            <div><dt>{{ t('load') }}</dt><dd>{{ load_label(prescription) }}</dd></div>
          </dl>
        </router-link>
      </section>
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { complete_workout, fetch_today_workout } from '@/api/plan'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'

const ACTIVE_PLAN_NOT_FOUND = 40404
const TODAY_WORKOUT_NOT_FOUND = 40405
const route = useRoute()
const router = useRouter()
const { language, t } = useLanguage()
const completing = ref(false)
const error = ref('')
const loading = ref(false)
const no_active_plan = ref(false)
const rest_day = ref(false)
const workout = ref(null)

async function load_today_workout() {
  loading.value = true
  error.value = ''
  no_active_plan.value = false
  rest_day.value = false
  try {
    workout.value = await fetch_today_workout(route.query.date)
  } catch (exception) {
    workout.value = null
    if (exception.code === ACTIVE_PLAN_NOT_FOUND) no_active_plan.value = true
    else if (exception.code === TODAY_WORKOUT_NOT_FOUND) rest_day.value = true
    else error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function complete_today_workout() {
  completing.value = true
  error.value = ''
  try {
    workout.value = await complete_workout(workout.value.workoutId)
  } catch (exception) {
    error.value = exception.message
  } finally {
    completing.value = false
  }
}

function focus_label(focus) {
  return t({ PUSH: 'focusPush', PULL: 'focusPull', LEGS: 'focusLegs', FULL_BODY: 'focusFullBody' }[focus] || focus)
}

function day_label(day_number) {
  return language.value === 'zh' ? `第 ${day_number} 天` : `${t('day')} ${day_number}`
}

function format_date(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat(language.value === 'zh' ? 'zh-CN' : 'en-US', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC',
  }).format(new Date(`${value}T00:00:00Z`))
}

function format_time(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat(language.value === 'zh' ? 'zh-CN' : 'en-US', {
    dateStyle: 'medium', timeStyle: 'short',
  }).format(new Date(value))
}

function load_label(prescription) {
  const load_type_labels = {
    BODYWEIGHT: language.value === 'zh' ? '自重' : 'Body weight',
    RPE_ONLY: language.value === 'zh' ? '按 RPE' : 'RPE only',
  }
  const load_type = load_type_labels[prescription.loadType]
    || display_value(prescription.loadType, language.value)
  if (prescription.load === null || prescription.load === undefined) {
    return load_type
  }
  return `${prescription.load} ${load_type}`
}

onMounted(load_today_workout)
</script>
