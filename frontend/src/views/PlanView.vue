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

      <PrescriptionAdjustments
        :plan-version="plan.version"
        :workouts="plan.workouts"
        :refresh-key="adjustment_refresh_key"
        @resolved="load_plan"
      />

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

            <div class="workout-actions">
              <el-button
                v-if="can_replace_workout(workout)"
                size="small"
                @click="open_replace_dialog(workout)"
              >
                {{ t('replaceWithTemplate') }}
              </el-button>
            </div>

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

            <NutritionTips :tips="workout.nutritionTips" />
          </article>
        </div>
      </template>
    </template>

    <el-dialog
      v-model="replace_dialog_visible"
      :title="t('replaceWithTemplate')"
      width="520px"
      @closed="reset_replace_dialog"
    >
      <el-alert
        v-if="templates_error"
        :title="templates_error"
        type="error"
        show-icon
        :closable="false"
      />
      <p class="page-subtitle">{{ t('replaceWorkoutHint') }}</p>
      <el-empty
        v-if="available_templates.length === 0"
        :description="t('noTemplatesAvailable')"
        class="empty-hint"
      >
        <p class="empty-hint">{{ t('loadTemplatesFirst') }}</p>
      </el-empty>
      <el-form v-else label-position="top">
        <el-form-item :label="t('selectTemplate')">
          <el-select v-model="selected_template_id" placeholder="Template">
            <el-option
              v-for="template in available_templates"
              :key="template.templateId"
              :label="template.name"
              :value="template.templateId"
            />
          </el-select>
        </el-form-item>
        <div class="template-meta" v-if="selected_template">
          <el-tag effect="plain">{{ body_part_label(selected_template.bodyPart) }}</el-tag>
          <el-tag effect="plain">{{ template_status_label(selected_template.status) }}</el-tag>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="replace_dialog_visible = false">{{ t('cancel') }}</el-button>
        <el-button
          type="primary"
          :loading="replacing"
          :disabled="!selected_template_id || available_templates.length === 0"
          @click="confirm_replace"
        >
          {{ t('replaceWithTemplate') }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetch_workout_templates } from '@/api/workout'
import { fetch_current_plan, replace_plan_workout } from '@/api/plan'
import NutritionTips from '@/components/NutritionTips.vue'
import PrescriptionAdjustments from '@/components/PrescriptionAdjustments.vue'
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
const templates = ref([])
const templates_error = ref('')
const replace_dialog_visible = ref(false)
const replacing = ref(false)
const selected_workout = ref(null)
const selected_template_id = ref('')
const adjustment_refresh_key = ref(0)

const week_numbers = computed(() => Array.from({ length: plan.value?.totalWeeks || 8 }, (_, index) => index + 1))
const selected_workouts = computed(() => plan.value?.workouts?.filter((workout) => workout.weekNumber === selected_week.value) || [])
const available_templates = computed(() => templates.value.filter((template) => template.status === 'ACTIVE'))
const selected_template = computed(() => available_templates.value.find((template) => template.templateId === selected_template_id.value) || null)

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

async function load_templates() {
  templates_error.value = ''
  try {
    templates.value = await fetch_workout_templates()
    if (
      !selected_template_id.value
      || !available_templates.value.some((template) => template.templateId === selected_template_id.value)
    ) {
      selected_template_id.value = ''
    }
    if (!selected_template_id.value && available_templates.value.length > 0) {
      selected_template_id.value = available_templates.value[0].templateId
    }
  } catch (exception) {
    templates_error.value = exception.message
  }
}

async function open_replace_dialog(workout) {
  selected_workout.value = workout
  replace_dialog_visible.value = true
  selected_template_id.value = ''
  await load_templates()
}

function reset_replace_dialog() {
  selected_workout.value = null
  selected_template_id.value = ''
  templates_error.value = ''
}

async function confirm_replace() {
  if (!plan.value || !selected_workout.value || !selected_template_id.value) {
    return
  }

  if (!window.confirm(t('replaceWorkoutConfirm'))) {
    return
  }

  replacing.value = true
  error.value = ''
  try {
    await replace_plan_workout(plan.value.planId, selected_workout.value.workoutId, {
      templateId: selected_template_id.value,
      expectedPlanVersion: plan.value.version,
    })
    replace_dialog_visible.value = false
    selected_workout.value = null
    adjustment_refresh_key.value += 1
    await load_plan()
  } catch (exception) {
    error.value = exception.message
  } finally {
    replacing.value = false
  }
}

function can_replace_workout(workout) {
  return workout.status === 'READY'
    && workout.scheduledDate >= new Date().toISOString().slice(0, 10)
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

function body_part_label(value) {
  const key = { CHEST: 'chest', BACK: 'backTraining', SHOULDERS: 'shouldersTraining', LEGS: 'legsTraining', WAIST: 'coreTraining' }[value]
  return t(key || value)
}

function template_status_label(value) {
  return t({ ACTIVE: 'templateActive', NEEDS_REPAIR: 'templateNeedsRepair' }[value] || value)
}

onMounted(load_plan)
</script>
