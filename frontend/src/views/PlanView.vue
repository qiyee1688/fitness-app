<template>
  <section class="page plan-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('planEyebrow') }}</p>
        <h1>{{ t('planTitle') }}</h1>
        <p class="page-subtitle">{{ t('planSubtitle') }}</p>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-if="loading" :rows="10" animated />

    <el-empty v-else-if="no_active_plan" :description="t('noActivePlan')">
      <p class="empty-hint">{{ t('noActivePlanHint') }}</p>
      <el-button type="primary" @click="router.push('/profile')">{{ t('goToProfile') }}</el-button>
    </el-empty>

    <template v-else-if="plan">
      <section class="plan-summary">
        <div>
          <el-tag type="success" effect="dark">{{ t('activePlan') }}</el-tag>
          <h2>{{ t('planPeriod') }}</h2>
          <p>{{ format_date(plan.startDate) }} — {{ format_date(plan.endDate) }}</p>
        </div>
        <div class="plan-stats">
          <div><strong>{{ plan.totalWeeks }}</strong><span>{{ t('weeks') }}</span></div>
          <div><strong>{{ plan.profileSnapshot?.daysPerWeek || '-' }}</strong><span>{{ t('daysPerWeek') }}</span></div>
          <div><strong>{{ plan.workouts?.length || 0 }}</strong><span>{{ t('workouts') }}</span></div>
        </div>
      </section>

      <el-empty v-if="!plan.workouts?.length" :description="t('emptyPlan')" />
      <template v-else>
        <div class="week-tabs" role="tablist" :aria-label="t('weeks')">
          <el-button
            v-for="week_number in week_numbers"
            :key="week_number"
            :type="selected_week === week_number ? 'primary' : 'default'"
            @click="selected_week = week_number"
          >
            {{ week_label(week_number) }}
          </el-button>
        </div>

        <div class="workout-grid">
          <article v-for="workout in selected_workouts" :key="workout.workoutId" class="workout-card">
            <header class="workout-header">
              <div>
                <p class="eyebrow">{{ day_label(workout.dayNumber) }}</p>
                <h2>{{ focus_label(workout.focus) }}</h2>
              </div>
              <div class="scheduled-date">
                <span>{{ t('scheduledDate') }}</span>
                <strong>{{ format_date(workout.scheduledDate) }}</strong>
              </div>
            </header>

            <div class="prescription-list">
              <router-link
                v-for="prescription in workout.prescriptions"
                :key="prescription.prescriptionId"
                class="prescription-row"
                :to="`/exercises/${prescription.exercise.id}`"
              >
                <span class="sequence">{{ prescription.sequence }}</span>
                <span class="exercise-copy">
                  <strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong>
                  <small>{{ display_value(prescription.exercise.bodyPart, language) }} · {{ display_value(prescription.exercise.equipment, language) }}</small>
                </span>
                <span class="prescription-basics">
                  <strong>{{ prescription.sets }} × {{ prescription.reps }}</strong>
                  <small>{{ prescription_detail(prescription) }}</small>
                </span>
              </router-link>
            </div>
          </article>
        </div>
      </template>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetch_current_plan } from '@/api/plan'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'

const ACTIVE_PLAN_NOT_FOUND = 40404
const router = useRouter()
const { language, t } = useLanguage()
const error = ref('')
const loading = ref(false)
const no_active_plan = ref(false)
const plan = ref(null)
const selected_week = ref(1)

const week_numbers = computed(() => Array.from({ length: plan.value?.totalWeeks || 8 }, (_, index) => index + 1))
const selected_workouts = computed(() => plan.value?.workouts?.filter((workout) => workout.weekNumber === selected_week.value) || [])

async function load_plan() {
  loading.value = true
  error.value = ''
  no_active_plan.value = false
  try {
    plan.value = await fetch_current_plan()
  } catch (exception) {
    plan.value = null
    if (exception.code === ACTIVE_PLAN_NOT_FOUND) {
      no_active_plan.value = true
    } else {
      error.value = exception.message
    }
  } finally {
    loading.value = false
  }
}

function format_date(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat(language.value === 'zh' ? 'zh-CN' : 'en-US', {
    year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC',
  }).format(new Date(`${value}T00:00:00Z`))
}

function week_label(week_number) {
  return language.value === 'zh' ? `第 ${week_number} 周` : `${t('week')} ${week_number}`
}

function day_label(day_number) {
  return language.value === 'zh' ? `第 ${day_number} 天` : `${t('day')} ${day_number}`
}

function focus_label(focus) {
  return t({ PUSH: 'focusPush', PULL: 'focusPull', LEGS: 'focusLegs', FULL_BODY: 'focusFullBody' }[focus] || focus)
}

function prescription_detail(prescription) {
  const details = [`${t('rpe')} ${prescription.rpe}`]
  if (prescription.load !== null && prescription.load !== undefined) {
    details.push(`${prescription.load} ${display_value(prescription.loadType, language.value)}`)
  }
  return details.join(' · ')
}

onMounted(load_plan)
</script>
