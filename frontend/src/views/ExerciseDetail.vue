<template>
  <section class="page detail-page">
    <el-button :icon="ArrowLeft" text @click="router.back()">返回</el-button>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="10" animated />

    <div v-else-if="exercise" class="detail-layout">
      <div class="media-panel">
        <img v-if="exercise.gifUrl || exercise.imageUrl" :src="exercise.gifUrl || exercise.imageUrl" :alt="exercise.name" />
        <div v-else class="media-empty">{{ exercise.bodyPart }}</div>
      </div>

      <div class="detail-content">
        <p class="eyebrow">{{ exercise.bodyPart }} / {{ exercise.target }}</p>
        <h1>{{ exercise.name }}</h1>
        <div class="tags">
          <el-tag>{{ exercise.category }}</el-tag>
          <el-tag type="success">{{ exercise.equipment }}</el-tag>
          <el-tag v-if="exercise.muscleGroup" type="info">{{ exercise.muscleGroup }}</el-tag>
        </div>

        <el-divider />

        <h2>步骤</h2>
        <ol class="steps">
          <li v-for="step in steps" :key="step">{{ step }}</li>
        </ol>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetch_exercise } from '@/api/exercise'

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
})

const router = useRouter()
const exercise = ref(null)
const error = ref('')
const loading = ref(false)

const steps = computed(() => {
  const instruction_steps = exercise.value?.instructionSteps
  if (!instruction_steps) {
    return []
  }
  return instruction_steps.zh || instruction_steps.en || Object.values(instruction_steps)[0] || []
})

async function load_detail() {
  loading.value = true
  error.value = ''
  try {
    exercise.value = await fetch_exercise(props.id)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

onMounted(load_detail)
</script>
