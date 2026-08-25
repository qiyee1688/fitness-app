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

      <PrescriptionAdjustments
        :plan-version="plan_version"
        :version-error="adjustment_version_error"
        :workouts="plan_workouts"
        :refresh-key="adjustment_refresh_key"
        @resolved="refresh_after_adjustment"
      />

      <section class="today-prescriptions">
        <article
          v-for="prescription in workout.prescriptions"
          :key="prescription.prescriptionId"
          class="today-prescription-card"
        >
          <router-link class="prescription-exercise-link" :to="`/exercises/${prescription.exercise.id}`">
            <span class="sequence">{{ prescription.sequence }}</span>
            <span class="exercise-copy">
            <strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong>
            <small>{{ display_value(prescription.exercise.bodyPart, language) }} · {{ display_value(prescription.exercise.equipment, language) }}</small>
            </span>
          </router-link>
          <dl class="prescription-metrics">
            <div><dt>{{ t('sets') }}</dt><dd>{{ prescription.sets }}</dd></div>
            <div><dt>{{ t('reps') }}</dt><dd>{{ prescription.reps }}</dd></div>
            <div><dt>{{ t('rpe') }}</dt><dd>{{ prescription.rpe }}</dd></div>
            <div><dt>{{ t('load') }}</dt><dd>{{ load_label(prescription) }}</dd></div>
          </dl>
          <div class="feedback-controls">
            <el-select v-model="feedback_types[prescription.prescriptionId]" :placeholder="t('feedback')">
              <el-option :label="t('tooEasy')" value="TOO_EASY" />
              <el-option :label="t('justRight')" value="JUST_RIGHT" />
              <el-option :label="t('tooHard')" value="TOO_HARD" />
              <el-option :label="t('hurt')" value="HURT" />
            </el-select>
            <el-input
              v-if="feedback_types[prescription.prescriptionId] === 'HURT'"
              v-model="hurt_body_parts[prescription.prescriptionId]"
              :placeholder="t('hurtBodyPart')"
            />
            <el-button
              :loading="submitting_feedback === prescription.prescriptionId"
              :disabled="!can_submit_feedback(prescription)"
              @click="submit_feedback(prescription)"
            >{{ t('submitFeedback') }}</el-button>
          </div>
        </article>
      </section>

      <NutritionTips :tips="workout.nutritionTips" />
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { ElMessage } from 'element-plus'
import { complete_workout, fetch_current_plan, fetch_today_workout, submit_exercise_feedback } from '@/api/plan'
import NutritionTips from '@/components/NutritionTips.vue'
import PrescriptionAdjustments from '@/components/PrescriptionAdjustments.vue'
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
const feedback_types = ref({})
const hurt_body_parts = ref({})
const submitting_feedback = ref('')
const plan_version = ref(null)
const adjustment_refresh_key = ref(0)
const adjustment_version_error = ref('')
const plan_workouts = ref([])

async function load_today_workout() {
  loading.value = true
  error.value = ''
  no_active_plan.value = false
  rest_day.value = false
  adjustment_version_error.value = ''
  try {
    workout.value = await fetch_today_workout(route.query.date)
  } catch (exception) {
    workout.value = null
    plan_version.value = null
    plan_workouts.value = []
    if (exception.code === ACTIVE_PLAN_NOT_FOUND) no_active_plan.value = true
    else if (exception.code === TODAY_WORKOUT_NOT_FOUND) rest_day.value = true
    else error.value = exception.message
    return
  } finally {
    loading.value = false
  }

  try {
    const plan = await fetch_current_plan()
    plan_version.value = plan.version
    plan_workouts.value = plan.workouts || []
  } catch (exception) {
    plan_version.value = null
    plan_workouts.value = []
    adjustment_version_error.value = exception.message
  }
}

async function complete_today_workout() {
  completing.value = true
  error.value = ''
  try {
    workout.value = await complete_workout(workout.value.workoutId)
    adjustment_refresh_key.value += 1
  } catch (exception) {
    error.value = exception.message
  } finally {
    completing.value = false
  }
}

function can_submit_feedback(prescription) {
  const feedback_type = feedback_types.value[prescription.prescriptionId]
  return feedback_type && (feedback_type !== 'HURT'
    || hurt_body_parts.value[prescription.prescriptionId]?.trim())
}

async function submit_feedback(prescription) {
  submitting_feedback.value = prescription.prescriptionId
  error.value = ''
  try {
    const feedback_type = feedback_types.value[prescription.prescriptionId]
    const result = await submit_exercise_feedback(workout.value.workoutId, prescription.exercise.id, {
      feedbackType: feedback_type,
      hurtBodyPart: feedback_type === 'HURT'
        ? hurt_body_parts.value[prescription.prescriptionId].trim()
        : null,
    })
    workout.value = result.workout
    adjustment_refresh_key.value += 1
    ElMessage.success(result.substituted ? t('exerciseSubstituted')
      : result.removedForSafety ? t('exerciseRemovedForSafety') : t('feedbackSaved'))
  } catch (exception) {
    error.value = exception.message
  } finally {
    submitting_feedback.value = ''
  }
}

async function refresh_after_adjustment() {
  adjustment_refresh_key.value += 1
  try {
    const plan = await fetch_current_plan()
    plan_version.value = plan.version
    plan_workouts.value = plan.workouts || []
    workout.value = await fetch_today_workout(route.query.date)
  } catch (exception) {
    error.value = exception.message
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
