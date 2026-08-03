import { createRouter, createWebHistory } from 'vue-router'

import ExerciseDetail from '@/views/ExerciseDetail.vue'
import ExerciseList from '@/views/ExerciseList.vue'
import UserProfile from '@/views/UserProfile.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'exercise-list',
      component: ExerciseList,
    },
    {
      path: '/exercises/:id',
      name: 'exercise-detail',
      component: ExerciseDetail,
      props: true,
    },
    {
      path: '/profile',
      name: 'user-profile',
      component: UserProfile,
    },
  ],
})

export default router
