<template>
  <section class="page profile-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">{{ t('profileEyebrow') }}</p>
        <h1>{{ t('profileTitle') }}</h1>
        <p class="page-subtitle">{{ t('profileSubtitle') }}</p>
      </div>
    </div>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
    />
    <el-alert
      v-if="success_message"
      :title="success_message"
      type="success"
      show-icon
      :closable="false"
    />

    <el-skeleton v-if="loading" :rows="7" animated />

    <el-form
      v-else
      class="profile-form"
      label-position="top"
      :model="form"
      @submit.prevent
    >
      <el-form-item :label="t('fitnessLevel')">
        <el-segmented v-model="form.fitnessLevel" :options="fitness_level_options" />
      </el-form-item>

      <el-form-item :label="t('goal')">
        <el-segmented v-model="form.goal" :options="goal_options" />
      </el-form-item>

      <el-form-item :label="t('daysPerWeek')">
        <el-slider
          v-model="form.daysPerWeek"
          :min="2"
          :max="6"
          :step="1"
          show-stops
          :marks="day_marks"
        />
      </el-form-item>

      <el-form-item :label="t('availableEquipment')">
        <el-checkbox-group v-model="form.availableEquipment" class="equipment-grid">
          <el-checkbox-button
            v-for="option in equipment_options"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </el-checkbox-button>
        </el-checkbox-group>
      </el-form-item>

      <el-button
        type="primary"
        :loading="saving"
        @click="submit_profile"
      >
        {{ saving ? t('saving') : t('saveProfile') }}
      </el-button>
    </el-form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { fetch_user_profile, save_user_profile } from '@/api/user'
import { useLanguage } from '@/composables/useLanguage'

const DEMO_USER = {
  username: 'demo',
  email: 'demo@fitness.local',
}

const { t } = useLanguage()
const error = ref('')
const loading = ref(false)
const saving = ref(false)
const success_message = ref('')

const form = reactive({
  fitnessLevel: 'BEGINNER',
  goal: 'GENERAL_FITNESS',
  daysPerWeek: 3,
  availableEquipment: ['body weight'],
})

const fitness_level_options = computed(() => [
  { label: t('beginner'), value: 'BEGINNER' },
  { label: t('intermediate'), value: 'INTERMEDIATE' },
  { label: t('advanced'), value: 'ADVANCED' },
])

const goal_options = computed(() => [
  { label: t('fatLoss'), value: 'FAT_LOSS' },
  { label: t('muscleGain'), value: 'MUSCLE_GAIN' },
  { label: t('endurance'), value: 'ENDURANCE' },
  { label: t('generalFitness'), value: 'GENERAL_FITNESS' },
])

const equipment_options = computed(() => [
  { label: t('bodyWeight'), value: 'body weight' },
  { label: t('dumbbell'), value: 'dumbbell' },
  { label: t('barbell'), value: 'barbell' },
  { label: t('kettlebell'), value: 'kettlebell' },
  { label: t('band'), value: 'band' },
  { label: t('cable'), value: 'cable' },
])

const day_marks = {
  2: '2',
  3: '3',
  4: '4',
  5: '5',
  6: '6',
}

async function load_profile() {
  loading.value = true
  error.value = ''
  success_message.value = ''
  try {
    const profile = await fetch_user_profile(DEMO_USER.username)
    apply_profile(profile)
  } catch (exception) {
    error.value = t('profileNotFound')
  } finally {
    loading.value = false
  }
}

async function submit_profile() {
  if (!form.availableEquipment.length) {
    error.value = t('availableEquipment')
    return
  }

  saving.value = true
  error.value = ''
  success_message.value = ''
  try {
    const profile = await save_user_profile({
      ...DEMO_USER,
      fitnessLevel: form.fitnessLevel,
      goal: form.goal,
      daysPerWeek: form.daysPerWeek,
      availableEquipment: form.availableEquipment,
    })
    apply_profile(profile)
    success_message.value = t('profileSaved')
  } catch (exception) {
    error.value = exception.message
  } finally {
    saving.value = false
  }
}

function apply_profile(profile) {
  form.fitnessLevel = profile.fitnessLevel
  form.goal = profile.goal
  form.daysPerWeek = profile.daysPerWeek
  form.availableEquipment = profile.availableEquipment?.length
    ? [...profile.availableEquipment]
    : ['body weight']
}

onMounted(load_profile)
</script>
