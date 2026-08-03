<template>
  <el-container class="app-shell">
    <el-header class="topbar">
      <router-link class="brand" to="/">
        Fitness App
      </router-link>
      <el-menu mode="horizontal" router :ellipsis="false" class="nav">
        <el-menu-item index="/">Exercises</el-menu-item>
      </el-menu>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>
