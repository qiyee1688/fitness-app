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

    <el-empty
      v-else-if="templates.length === 0"
      :description="t('emptyTemplates')"
      class="empty-hint"
    />

    <div v-else class="template-grid">
      <article v-for="template in templates" :key="template.templateId" class="template-card">
        <header class="template-card-header">
          <div>
            <p class="eyebrow">{{ body_part_label(template.bodyPart) }}</p>
            <h2>{{ template.name }}</h2>
          </div>
          <el-tag :type="template.status === 'ACTIVE' ? 'success' : 'warning'">
            {{ template_status_label(template.status) }}
          </el-tag>
        </header>

        <div class="tags">
          <el-tag
            v-for="item in template.equipmentSnapshot || []"
            :key="item"
            effect="plain"
          >
            {{ display_value(item, language) }}
          </el-tag>
        </div>

        <div class="template-prescriptions">
          <article
            v-for="prescription in template.exercises"
            :key="prescription.prescriptionId"
            class="template-prescription"
          >
            <span class="sequence">{{ prescription.sequence }}</span>
            <div>
              <router-link :to="`/exercises/${prescription.exercise.id}`">
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

        <div class="template-actions">
          <el-button
            type="danger"
            plain
            :loading="deleting_template_id === template.templateId"
            @click="delete_template(template)"
          >
            {{ t('deleteTemplate') }}
          </el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { delete_workout_template, fetch_workout_templates } from '@/api/workout'
import { useLanguage } from '@/composables/useLanguage'
import { display_exercise_name, display_value } from '@/utils/exerciseDisplay'

const { language, t } = useLanguage()
const templates = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const deleting_template_id = ref('')

async function load_templates() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    templates.value = await fetch_workout_templates()
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function delete_template(template) {
  if (!window.confirm(t('deleteTemplateConfirm'))) {
    return
  }

  deleting_template_id.value = template.templateId
  error.value = ''
  success.value = ''
  try {
    await delete_workout_template(template.templateId)
    templates.value = templates.value.filter(item => item.templateId !== template.templateId)
    success.value = t('templateDeleted')
  } catch (exception) {
    error.value = exception.message
  } finally {
    deleting_template_id.value = ''
  }
}

function body_part_label(value) {
  const key = { CHEST: 'chest', BACK: 'backTraining', SHOULDERS: 'shouldersTraining', LEGS: 'legsTraining', WAIST: 'coreTraining' }[value]
  return t(key || value)
}

function template_status_label(value) {
  return t({ ACTIVE: 'templateActive', NEEDS_REPAIR: 'templateNeedsRepair' }[value] || value)
}

onMounted(load_templates)
</script>
