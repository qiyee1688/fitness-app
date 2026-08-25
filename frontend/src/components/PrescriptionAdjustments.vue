<template>
  <section
    v-if="loading || error || versionError || adjustments.length"
    class="prescription-adjustments"
    :aria-label="t('adjustmentSectionTitle')"
  >
    <div class="adjustment-heading">
      <div>
        <p class="eyebrow">{{ t('adjustmentEyebrow') }}</p>
        <h2>{{ t('adjustmentSectionTitle') }}</h2>
        <p>{{ t('adjustmentSectionHint') }}</p>
      </div>
      <el-button text :loading="loading" @click="load_adjustments">{{ t('refresh') }}</el-button>
    </div>

    <el-alert v-if="versionError" :title="versionError" type="error" show-icon :closable="false" />
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="3" animated />

    <template v-else-if="adjustments.length">
      <article
        v-for="adjustment in pending_adjustments"
        :key="adjustment.adjustmentId"
        class="adjustment-card pending"
      >
        <header class="adjustment-card-header">
          <div>
            <el-tag type="warning" effect="dark">{{ status_label(adjustment.status) }}</el-tag>
            <h3>{{ exercise_label(adjustment.sourceExerciseId) }}</h3>
          </div>
          <small>{{ target_workout_label(adjustment) }}</small>
        </header>

        <p class="adjustment-reason">{{ adjustment_reason(adjustment) }}</p>

        <dl class="adjustment-diff">
          <div v-for="field in prescription_fields(adjustment)" :key="field.key" :class="{ changed: field.changed }">
            <dt>{{ field.label }}</dt>
            <dd>
              <span>{{ field.before }}</span>
              <span aria-hidden="true">→</span>
              <strong>{{ field.after }}</strong>
            </dd>
          </div>
        </dl>

        <p v-if="has_substitute(adjustment)" class="adjustment-substitute">
          {{ t('adjustmentSubstitute') }}: <strong>{{ exercise_label(adjustment.suggestedExerciseId) }}</strong>
        </p>

        <div class="adjustment-actions">
          <el-button
            :loading="processing_id === adjustment.adjustmentId"
            :disabled="!can_resolve || processing_id !== ''"
            @click="resolve_adjustment(adjustment, 'decline')"
          >{{ t('adjustmentDecline') }}</el-button>
          <el-button
            type="primary"
            :loading="processing_id === adjustment.adjustmentId"
            :disabled="!can_resolve || processing_id !== ''"
            @click="resolve_adjustment(adjustment, 'accept')"
          >{{ t('adjustmentAccept') }}</el-button>
        </div>
      </article>

      <div v-if="resolved_adjustments.length" class="adjustment-history">
        <p>{{ t('adjustmentHistory') }}</p>
        <div v-for="adjustment in resolved_adjustments" :key="adjustment.adjustmentId" class="adjustment-history-item">
          <span>{{ exercise_label(adjustment.sourceExerciseId) }}</span>
          <el-tag size="small" :type="status_type(adjustment.status)">{{ status_label(adjustment.status) }}</el-tag>
        </div>
      </div>
    </template>
  </section>
  <p v-else class="adjustment-empty-status">{{ t('adjustmentEmpty') }}</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'

import { ElMessage } from 'element-plus'
import {
  accept_prescription_adjustment,
  decline_prescription_adjustment,
  fetch_prescription_adjustments,
} from '@/api/plan'
import { fetch_exercise } from '@/api/exercise'
import { useLanguage } from '@/composables/useLanguage'
import {
  adjustment_status_key,
  has_substitute,
  prescription_difference,
} from '@/utils/prescriptionAdjustment'

const props = defineProps({
  planVersion: {
    type: Number,
    default: null,
  },
  refreshKey: {
    type: Number,
    default: 0,
  },
  versionError: {
    type: String,
    default: '',
  },
  workouts: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['resolved'])
const { language, t } = useLanguage()
const adjustments = ref([])
const error = ref('')
const loading = ref(false)
const processing_id = ref('')
const exercise_names = ref({})

const pending_adjustments = computed(() => adjustments.value.filter((adjustment) => adjustment.status === 'PENDING'))
const resolved_adjustments = computed(() => adjustments.value.filter((adjustment) => adjustment.status !== 'PENDING'))
const can_resolve = computed(() => Number.isInteger(props.planVersion) && props.planVersion >= 0)

async function load_adjustments() {
  loading.value = true
  error.value = ''
  try {
    adjustments.value = await fetch_prescription_adjustments()
    await load_exercise_names()
  } catch (exception) {
    adjustments.value = []
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function load_exercise_names() {
  const ids = [...new Set(adjustments.value.flatMap((adjustment) => [
    adjustment.sourceExerciseId,
    adjustment.suggestedExerciseId,
    adjustment.originalPrescription?.exerciseId,
  ]).filter(Boolean))]
  const entries = await Promise.all(ids.map(async (id) => {
    try {
      const exercise = await fetch_exercise(id)
      return [id, language.value === 'zh' ? exercise.name || exercise.nameEn : exercise.nameEn || exercise.name]
    } catch {
      return [id, id]
    }
  }))
  exercise_names.value = Object.fromEntries(entries)
}

async function resolve_adjustment(adjustment, action) {
  if (!can_resolve.value || processing_id.value) {
    error.value = t('adjustmentVersionUnavailable')
    return
  }

  processing_id.value = adjustment.adjustmentId
  error.value = ''
  try {
    const resolved = action === 'accept'
      ? await accept_prescription_adjustment(adjustment.adjustmentId, props.planVersion)
      : await decline_prescription_adjustment(adjustment.adjustmentId, props.planVersion)
    adjustments.value = adjustments.value.map((item) => item.adjustmentId === resolved.adjustmentId ? resolved : item)
    ElMessage.success(t({
      ACCEPTED: 'adjustmentAccepted',
      DECLINED: 'adjustmentDeclined',
      EXPIRED: 'adjustmentExpiredStatus',
    }[resolved.status] || 'adjustmentAccepted'))
    emit('resolved', resolved)
  } catch (exception) {
    error.value = exception.message
  } finally {
    processing_id.value = ''
  }
}

function adjustment_reason(adjustment) {
  return language.value === 'zh' ? adjustment.reason || adjustment.reasonEn : adjustment.reasonEn || adjustment.reason
}

function exercise_label(id) {
  return exercise_names.value[id] || id || '-'
}

function target_workout_label(adjustment) {
  const workout = props.workouts.find((item) => item.workoutId === adjustment.targetWorkoutId)
  if (!workout) {
    return `${t('adjustmentNextWorkout')} · ${adjustment.targetWorkoutId}`
  }
  const date = new Intl.DateTimeFormat(language.value === 'zh' ? 'zh-CN' : 'en-US', {
    month: 'short', day: 'numeric', timeZone: 'UTC',
  }).format(new Date(`${workout.scheduledDate}T00:00:00Z`))
  return `${t('adjustmentNextWorkout')} · ${date} · ${focus_label(workout.focus)}`
}

function focus_label(focus) {
  return t({ PUSH: 'focusPush', PULL: 'focusPull', LEGS: 'focusLegs', FULL_BODY: 'focusFullBody' }[focus] || focus)
}

function prescription_fields(adjustment) {
  return prescription_difference(adjustment, {
    sets: t('sets'),
    reps: t('reps'),
    load: t('load'),
    rpe: t('rpe'),
  })
}

function status_label(status) {
  return t(adjustment_status_key(status))
}

function status_type(status) {
  return { ACCEPTED: 'success', DECLINED: 'info', EXPIRED: 'warning' }[status] || 'info'
}

watch(() => props.refreshKey, load_adjustments)
watch(language, load_exercise_names)
onMounted(load_adjustments)
</script>
