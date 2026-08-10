import { createRouter, createWebHistory } from 'vue-router'

import ExerciseDetail from '@/views/ExerciseDetail.vue'
import ExerciseList from '@/views/ExerciseList.vue'
import PlanView from '@/views/PlanView.vue'
import TodayWorkout from '@/views/TodayWorkout.vue'
import UserProfile from '@/views/UserProfile.vue'
import OnDemandWorkout from '@/views/OnDemandWorkout.vue'
import WorkoutTemplates from '@/views/WorkoutTemplates.vue'

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
      path: '/workouts/on-demand',
      name: 'on-demand-workout',
      component: OnDemandWorkout,
    },
    {
      path: '/workout-templates',
      name: 'workout-templates',
      component: WorkoutTemplates,
    },
    {
      path: '/today',
      name: 'today-workout',
      component: TodayWorkout,
    },
    {
      path: '/plan',
      name: 'plan-view',
      component: PlanView,
    },
    {
      path: '/profile',
      name: 'user-profile',
      component: UserProfile,
    },
  ],
})

export default router
