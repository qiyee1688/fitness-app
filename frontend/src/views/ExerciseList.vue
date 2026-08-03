<template>
  <section class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Exercise</p>
        <h1>动作库</h1>
      </div>
      <el-input
        v-model="keyword"
        class="search"
        clearable
        placeholder="搜索名称"
        @keyup.enter="load_search"
        @clear="load_page"
      >
        <template #append>
          <el-button :icon="Search" @click="load_search" />
        </template>
      </el-input>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />

    <el-skeleton v-if="loading" :rows="8" animated />

    <div v-else class="exercise-grid">
      <article v-for="exercise in exercises" :key="exercise.id" class="exercise-card">
        <img v-if="exercise.imageUrl || exercise.gifUrl" :src="exercise.imageUrl || exercise.gifUrl" :alt="exercise.name" />
        <div class="card-body">
          <h2>{{ exercise.name }}</h2>
          <div class="tags">
            <el-tag size="small">{{ exercise.bodyPart }}</el-tag>
            <el-tag size="small" type="success">{{ exercise.target }}</el-tag>
            <el-tag size="small" type="info">{{ exercise.equipment }}</el-tag>
          </div>
          <el-button :icon="ArrowRight" text @click="go_detail(exercise.id)">查看</el-button>
        </div>
      </article>
    </div>

    <div v-if="!loading && total > page_size" class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="page_size"
        :total="total"
        layout="prev, pager, next"
        @current-change="load_page"
      />
    </div>
  </section>
</template>

<script setup>
import { ArrowRight, Search } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetch_exercises, search_exercises } from '@/api/exercise'

const router = useRouter()
const exercises = ref([])
const error = ref('')
const keyword = ref('')
const loading = ref(false)
const page = ref(1)
const page_size = 20
const total = ref(0)

async function load_page() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetch_exercises({ page: page.value, pageSize: page_size })
    exercises.value = data.items
    total.value = data.total
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function load_search() {
  if (!keyword.value.trim()) {
    await load_page()
    return
  }

  loading.value = true
  error.value = ''
  try {
    exercises.value = await search_exercises(keyword.value.trim(), page_size)
    total.value = exercises.value.length
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function go_detail(id) {
  router.push({ name: 'exercise-detail', params: { id } })
}

onMounted(load_page)
</script>
