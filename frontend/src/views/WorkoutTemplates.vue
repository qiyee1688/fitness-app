<template>
  <section class="page template-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('templateEyebrow') }}</p>
        <h1>{{ t('templateTitle') }}</h1>
        <p class="page-subtitle">{{ t('templateSubtitle') }}</p>
      </div>
      <router-link to="/workouts/on-demand">
        <el-button type="primary">{{ t('createTemplateFromWorkout') }}</el-button>
      </router-link>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-alert v-if="success" :title="success" type="success" show-icon :closable="false" />
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="templates.length === 0" :description="t('emptyTemplates')" class="empty-hint" />

    <div v-else class="template-grid">
      <article v-for="template in templates" :key="template.templateId" class="template-card">
        <header class="template-card-header">
          <div class="template-title-block">
            <p class="eyebrow">{{ body_part_label(template.bodyPart) }}</p>
            <el-input v-if="is_editing(template)" v-model="edit_draft.name" :maxlength="80" show-word-limit :placeholder="t('templateName')" />
            <h2 v-else>{{ template.name }}</h2>
          </div>
          <el-tag :type="template.status === 'ACTIVE' ? 'success' : 'warning'">{{ template_status_label(template.status) }}</el-tag>
        </header>

        <div class="tags">
          <el-tag v-for="item in template.equipment || []" :key="item" effect="plain">{{ display_value(item, language) }}</el-tag>
        </div>

        <el-alert v-if="template.profileChanged" :title="t('templateProfileChanged')" type="warning" show-icon :closable="false" />
        <el-alert v-if="template.status === 'NEEDS_REPAIR'" :title="t('templateRepairHint')" type="warning" show-icon :closable="false" />

        <div v-if="is_editing(template)" class="template-edit-list">
          <article v-for="(item, index) in edit_draft.exercises" :key="item.templateExerciseId" class="template-edit-item">
            <div class="template-edit-heading">
              <span class="sequence">{{ index + 1 }}</span>
              <div class="template-edit-exercise">
                <strong>{{ display_exercise_name(item.exercise.name, language) }}</strong>
                <small>{{ display_value(item.exercise.equipment, language) }}</small>
                <small v-if="item.repairReason" class="template-inline-warning">{{ repair_reason_label(item.repairReason) }}</small>
              </div>
              <div class="template-order-actions">
                <el-button size="small" plain :disabled="index === 0" @click="move_edit_item(index, -1)">{{ t('moveUp') }}</el-button>
                <el-button size="small" plain :disabled="index === edit_draft.exercises.length - 1" @click="move_edit_item(index, 1)">{{ t('moveDown') }}</el-button>
              </div>
            </div>

            <div class="template-substitute-row">
              <el-button size="small" plain :loading="loading_substitutes_id === item.templateExerciseId" @click="load_substitutes(template, item)">{{ t('findSubstitutes') }}</el-button>
              <el-select v-if="substitutes_by_item[item.templateExerciseId]" v-model="item.exerciseId" :placeholder="t('substituteExercise')" @change="apply_selected_exercise(item)">
                <el-option :label="display_exercise_name(item.originalExercise.name, language)" :value="item.originalExercise.id" />
                <el-option v-for="substitute in substitutes_by_item[item.templateExerciseId]" :key="substitute.id" :label="`${display_exercise_name(substitute.name, language)} · ${display_value(substitute.equipment, language)}`" :value="substitute.id" />
              </el-select>
              <small v-if="substitutes_by_item[item.templateExerciseId]?.length === 0" class="template-inline-warning">{{ t('templateNoSubstitutes') }}</small>
            </div>

            <div class="template-edit-fields">
              <label><span>{{ t('sets') }}</span><el-input-number v-model="item.sets" :min="1" :max="20" controls-position="right" /></label>
              <label><span>{{ t('reps') }}</span><el-input-number v-model="item.reps" :min="1" :max="100" controls-position="right" /></label>
              <label><span>{{ t('load') }}</span><el-input-number v-model="item.load" :min="0" :max="999.99" :precision="2" controls-position="right" /></label>
              <label><span>{{ t('loadType') }}</span><el-select v-model="item.loadType"><el-option v-for="load_type in load_types" :key="load_type" :label="load_type_label(load_type)" :value="load_type" /></el-select></label>
              <label><span>{{ t('rpe') }}</span><el-input-number v-model="item.rpe" :min="6" :max="10" :step="0.5" :precision="1" controls-position="right" /></label>
            </div>

            <div class="template-edit-item-actions">
              <small>{{ minimum_count_message(template) }}</small>
              <el-button type="danger" size="small" plain :disabled="edit_draft.exercises.length <= minimum_exercise_count(template)" @click="delete_edit_item(template, index)">{{ t('deleteExercise') }}</el-button>
            </div>
          </article>
        </div>

        <div v-else class="template-prescriptions">
          <article v-for="prescription in template.exercises" :key="prescription.prescriptionId" class="template-prescription">
            <span class="sequence">{{ prescription.sequence }}</span>
            <div>
              <router-link :to="`/exercises/${prescription.exercise.id}`"><strong>{{ display_exercise_name(prescription.exercise.name, language) }}</strong></router-link>
              <p>{{ display_value(prescription.exercise.equipment, language) }}</p>
              <small v-if="template.repairReasons?.[prescription.prescriptionId]" class="template-inline-warning">{{ repair_reason_label(template.repairReasons[prescription.prescriptionId]) }}</small>
            </div>
            <dl>
              <div><dt>{{ t('sets') }}</dt><dd>{{ prescription.sets }}</dd></div>
              <div><dt>{{ t('reps') }}</dt><dd>{{ prescription.reps }}</dd></div>
              <div><dt>{{ t('load') }}</dt><dd>{{ prescription.load ?? '-' }}</dd></div>
              <div><dt>{{ t('loadType') }}</dt><dd>{{ load_type_label(prescription.loadType) }}</dd></div>
              <div><dt>{{ t('rpe') }}</dt><dd>{{ prescription.rpe ?? '-' }}</dd></div>
            </dl>
          </article>
        </div>

        <div class="template-actions">
          <template v-if="is_editing(template)">
            <el-button @click="cancel_edit">{{ t('cancel') }}</el-button>
            <el-button type="primary" :loading="saving_template_id === template.templateId" @click="save_template(template)">{{ t('saveTemplateChanges') }}</el-button>
          </template>
          <el-button v-else plain @click="begin_edit(template)">{{ template.status === 'NEEDS_REPAIR' ? t('repairTemplate') : t('editTemplate') }}</el-button>
          <el-button type="danger" plain :loading="deleting_template_id === template.templateId" @click="delete_template(template)">{{ t('deleteTemplate') }}</el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { delete_workout_template, fetch_workout_template_substitutes, fetch_workout_templates, update_workout_template } from '@/api/workout'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'

const minimum_exercise_counts = { CHEST: 3, BACK: 3, SHOULDERS: 3, LEGS: 4, WAIST: 2 }
const load_types = ['ABSOLUTE_WEIGHT', 'PERCENT_1RM', 'BODYWEIGHT', 'RPE_ONLY', 'DURATION']
const { language, t } = useLanguage()
const templates = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const deleting_template_id = ref('')
const editing_template_id = ref('')
const saving_template_id = ref('')
const loading_substitutes_id = ref('')
const substitutes_by_item = ref({})
const edit_draft = ref(null)

async function load_templates(clear_messages = true) {
  loading.value = true
  if (clear_messages) {
    error.value = ''
    success.value = ''
  }
  try {
    templates.value = await fetch_workout_templates()
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function delete_template(template) {
  if (!window.confirm(t('deleteTemplateConfirm'))) return
  deleting_template_id.value = template.templateId
  error.value = ''
  success.value = ''
  try {
    await delete_workout_template(template.templateId)
    templates.value = templates.value.filter(item => item.templateId !== template.templateId)
    if (is_editing(template)) cancel_edit()
    success.value = t('templateDeleted')
  } catch (exception) {
    error.value = exception.message
  } finally {
    deleting_template_id.value = ''
  }
}

function is_editing(template) {
  return editing_template_id.value === template.templateId
}

function begin_edit(template) {
  editing_template_id.value = template.templateId
  error.value = ''
  success.value = ''
  substitutes_by_item.value = {}
  edit_draft.value = {
    name: template.name,
    exercises: template.exercises.map((prescription, index) => ({
      templateExerciseId: prescription.prescriptionId,
      exerciseId: prescription.exercise.id,
      originalExercise: { ...prescription.exercise },
      exercise: { ...prescription.exercise },
      repairReason: template.repairReasons?.[prescription.prescriptionId],
      sequence: index + 1,
      sets: prescription.sets,
      reps: prescription.reps,
      load: prescription.load == null ? null : Number(prescription.load),
      loadType: prescription.loadType,
      rpe: prescription.rpe == null ? 6 : Number(prescription.rpe),
    })),
  }
}

function cancel_edit() {
  editing_template_id.value = ''
  edit_draft.value = null
  substitutes_by_item.value = {}
}

function move_edit_item(index, offset) {
  const target_index = index + offset
  if (target_index < 0 || target_index >= edit_draft.value.exercises.length) return
  const exercises = [...edit_draft.value.exercises]
  const [item] = exercises.splice(index, 1)
  exercises.splice(target_index, 0, item)
  edit_draft.value.exercises = exercises
}

function minimum_exercise_count(template) {
  return minimum_exercise_counts[template.bodyPart] || 1
}

function minimum_count_message(template) {
  return t('templateMinimumCount').replace('{count}', minimum_exercise_count(template))
}

function delete_edit_item(template, index) {
  if (edit_draft.value.exercises.length <= minimum_exercise_count(template)) {
    error.value = minimum_count_message(template)
    return
  }
  edit_draft.value.exercises.splice(index, 1)
}

async function load_substitutes(template, item) {
  loading_substitutes_id.value = item.templateExerciseId
  error.value = ''
  try {
    const substitutes = await fetch_workout_template_substitutes(template.templateId, item.templateExerciseId)
    substitutes_by_item.value = { ...substitutes_by_item.value, [item.templateExerciseId]: substitutes }
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading_substitutes_id.value = ''
  }
}

function apply_selected_exercise(item) {
  if (item.exerciseId === item.originalExercise.id) {
    item.exercise = { ...item.originalExercise }
    return
  }
  const substitute = substitutes_by_item.value[item.templateExerciseId]?.find(candidate => candidate.id === item.exerciseId)
  if (substitute) item.exercise = { ...substitute }
}

async function save_template(template) {
  if (!edit_draft.value.name.trim()) {
    error.value = t('templateNameRequired')
    return
  }
  saving_template_id.value = template.templateId
  error.value = ''
  success.value = ''
  try {
    const updated_template = await update_workout_template(template.templateId, {
      expectedVersion: template.version,
      name: edit_draft.value.name.trim(),
      exercises: edit_draft.value.exercises.map((item, index) => ({
        templateExerciseId: item.templateExerciseId,
        exerciseId: item.exerciseId,
        sequence: index + 1,
        sets: item.sets,
        reps: item.reps,
        load: item.load === '' ? null : item.load,
        loadType: item.loadType,
        rpe: item.rpe,
      })),
    })
    templates.value = templates.value.map(item => item.templateId === updated_template.templateId ? updated_template : item)
    cancel_edit()
    success.value = updated_template.status === 'NEEDS_REPAIR' ? t('templateStillNeedsRepair') : t('templateUpdated')
  } catch (exception) {
    if (exception.code === 40904) {
      cancel_edit()
      await load_templates(false)
      error.value = t('templateConflict')
    } else {
      error.value = exception.message
    }
  } finally {
    saving_template_id.value = ''
  }
}

function load_type_label(value) {
  return t({
    ABSOLUTE_WEIGHT: 'loadTypeAbsoluteWeight',
    PERCENT_1RM: 'loadTypePercentOneRepMax',
    BODYWEIGHT: 'loadTypeBodyweight',
    RPE_ONLY: 'loadTypeRpeOnly',
    DURATION: 'loadTypeDuration',
  }[value] || value)
}

function body_part_label(value) {
  const key = { CHEST: 'chest', BACK: 'backTraining', SHOULDERS: 'shouldersTraining', LEGS: 'legsTraining', WAIST: 'coreTraining' }[value]
  return t(key || value)
}

function template_status_label(value) {
  return t({ ACTIVE: 'templateActive', NEEDS_REPAIR: 'templateNeedsRepair' }[value] || value)
}

function repair_reason_label(value) {
  return t({
    EXERCISE_UNAVAILABLE: 'templateRepairExerciseUnavailable',
    EQUIPMENT_UNAVAILABLE: 'templateRepairEquipmentUnavailable',
    PRESCRIPTION_INCOMPATIBLE: 'templateRepairPrescriptionIncompatible',
  }[value] || value)
}

onMounted(load_templates)
</script>
