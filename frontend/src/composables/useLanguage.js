import { computed, ref, watch } from 'vue'

const supported_languages = ['zh', 'en']
const url_language = typeof window === 'undefined'
  ? ''
  : new URLSearchParams(window.location.search).get('lang')
const browser_language = typeof localStorage === 'undefined'
  ? ''
  : localStorage.getItem('fitness-language')
const initial_language = supported_languages.includes(url_language)
  ? url_language
  : browser_language
const language = ref(supported_languages.includes(initial_language) ? initial_language : 'zh')

const messages = {
  zh: {
    appName: '健身计划',
    navExercises: '动作库',
    exerciseEyebrow: 'Exercise',
    exerciseTitle: '动作库',
    searchPlaceholder: '搜索名称',
    emptyExercises: '暂无匹配的 Exercise',
    view: '查看',
    back: '返回',
    steps: '步骤',
    emptySteps: '暂无步骤说明',
    category: '分类',
    bodyPart: '身体部位',
    target: '目标肌群',
    equipment: '器械',
    muscleGroup: '协同肌群',
    secondary: '辅助肌群',
    exerciseFallback: 'Exercise',
    all: '全部',
    chest: '练胸',
    backTraining: '练背',
    shouldersTraining: '练肩',
    legsTraining: '练腿',
    coreTraining: '练核心',
  },
  en: {
    appName: 'Fitness App',
    navExercises: 'Exercises',
    exerciseEyebrow: 'Exercise',
    exerciseTitle: 'Exercises',
    searchPlaceholder: 'Search by name',
    emptyExercises: 'No matching exercises',
    view: 'View',
    back: 'Back',
    steps: 'Steps',
    emptySteps: 'No instructions yet',
    category: 'Category',
    bodyPart: 'BodyPart',
    target: 'Target',
    equipment: 'Equipment',
    muscleGroup: 'Muscle Group',
    secondary: 'Secondary',
    exerciseFallback: 'Exercise',
    all: 'All',
    chest: 'Chest',
    backTraining: 'Back',
    shouldersTraining: 'Shoulders',
    legsTraining: 'Legs',
    coreTraining: 'Core',
  },
}

export function useLanguage() {
  const is_zh = computed(() => language.value === 'zh')

  function set_language(next_language) {
    if (supported_languages.includes(next_language)) {
      language.value = next_language
    }
  }

  function t(key) {
    return messages[language.value][key] || messages.en[key] || key
  }

  watch(language, (next_language) => {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('fitness-language', next_language)
    }
  })

  return {
    is_zh,
    language,
    set_language,
    t,
  }
}
