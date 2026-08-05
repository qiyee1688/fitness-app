<template>
  <el-container class="app-shell">
    <el-header class="topbar">
      <router-link class="brand" to="/">
        {{ t('appName') }}
      </router-link>
      <el-menu mode="horizontal" router :ellipsis="false" class="nav">
        <el-menu-item index="/">{{ t('navExercises') }}</el-menu-item>
        <el-menu-item index="/plan">{{ t('navPlan') }}</el-menu-item>
        <el-menu-item index="/today">{{ t('navToday') }}</el-menu-item>
        <el-menu-item index="/profile">{{ t('navProfile') }}</el-menu-item>
      </el-menu>
      <el-radio-group v-model="language" class="language-switch" size="small">
        <el-radio-button value="zh">中文</el-radio-button>
        <el-radio-button value="en">English</el-radio-button>
      </el-radio-group>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useLanguage } from '@/composables/useLanguage'

const { language, t } = useLanguage()
</script>
