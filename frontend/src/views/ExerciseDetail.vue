<template>
  <section class="page detail-page">
    <el-button :icon="ArrowLeft" text @click="router.back()">返回</el-button>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-skeleton v-else-if="loading" :rows="10" animated />

    <div v-else-if="exercise" class="detail-layout">
      <div class="media-panel">
        <img
          v-if="media_source"
          :src="media_source"
          :alt="exercise.name"
          @error="media_broken = true"
        />
        <div v-else class="media-empty">{{ exercise.bodyPart }}</div>
        <span class="media-label">{{ exercise.bodyPart || 'Exercise' }}</span>
      </div>

      <div class="detail-content">
        <p class="eyebrow">{{ exercise.bodyPart }} / {{ exercise.target }}</p>
        <h1>{{ exercise.name }}</h1>
        <div class="tags">
          <el-tag>{{ exercise.bodyPart }}</el-tag>
          <el-tag type="success">{{ exercise.target }}</el-tag>
          <el-tag type="info">{{ exercise.equipment }}</el-tag>
        </div>

        <dl class="meta-grid">
          <div>
            <dt>Category</dt>
            <dd>{{ exercise.category || '-' }}</dd>
          </div>
          <div>
            <dt>BodyPart</dt>
            <dd>{{ exercise.bodyPart || '-' }}</dd>
          </div>
          <div>
            <dt>Target</dt>
            <dd>{{ exercise.target || '-' }}</dd>
          </div>
          <div>
            <dt>Equipment</dt>
            <dd>{{ exercise.equipment || '-' }}</dd>
          </div>
          <div>
            <dt>Muscle Group</dt>
            <dd>{{ exercise.muscleGroup || '-' }}</dd>
          </div>
          <div>
            <dt>Secondary</dt>
            <dd>{{ secondary_muscles }}</dd>
          </div>
        </dl>

        <el-divider />

        <h2>步骤</h2>
        <ol v-if="steps.length" class="steps">
          <li v-for="step in steps" :key="step">{{ step }}</li>
        </ol>
        <el-empty v-else description="暂无步骤说明" />
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
const media_broken = ref(false)

const media_source = computed(() => {
  if (media_broken.value) {
    return ''
  }

  return exercise.value?.gifUrl || exercise.value?.imageUrl || ''
})

const secondary_muscles = computed(() => {
  const muscles = exercise.value?.secondaryMuscles || []
  return muscles.length ? muscles.join(' / ') : '-'
})

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
  media_broken.value = false
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
