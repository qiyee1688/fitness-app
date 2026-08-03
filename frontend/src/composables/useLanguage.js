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
    navProfile: '用户档案',
    profileEyebrow: '用户档案',
    profileTitle: '用户档案',
    profileSubtitle: '填写训练水平、目标、每周训练天数和可用器械，作为后续生成训练计划的输入。',
    fitnessLevel: '训练水平',
    goal: '训练目标',
    daysPerWeek: '每周训练天数',
    availableEquipment: '可用器械',
    saveProfile: '保存档案',
    saving: '保存中',
    profileSaved: '用户档案已保存',
    profileNotFound: '还没有用户档案，请填写后保存。',
    beginner: '新手',
    intermediate: '有经验',
    advanced: '进阶',
    fatLoss: '减脂',
    muscleGain: '增肌',
    endurance: '耐力',
    generalFitness: '综合健康',
    bodyWeight: '自重',
    dumbbell: '哑铃',
    barbell: '杠铃',
    kettlebell: '壶铃',
    band: '弹力带',
    cable: '绳索',
    exerciseEyebrow: '动作库',
    exerciseTitle: '动作库',
    searchPlaceholder: '搜索名称',
    emptyExercises: '暂无匹配的动作',
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
    exerciseFallback: '动作',
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
    navProfile: 'Profile',
    profileEyebrow: 'UserProfile',
    profileTitle: 'User Profile',
    profileSubtitle: 'Set training level, goal, weekly training days, and available equipment for future Plan generation.',
    fitnessLevel: 'Fitness Level',
    goal: 'Goal',
    daysPerWeek: 'Days Per Week',
    availableEquipment: 'Available Equipment',
    saveProfile: 'Save Profile',
    saving: 'Saving',
    profileSaved: 'Profile saved',
    profileNotFound: 'No profile yet. Fill the form and save it.',
    beginner: 'Beginner',
    intermediate: 'Intermediate',
    advanced: 'Advanced',
    fatLoss: 'Fat Loss',
    muscleGain: 'Muscle Gain',
    endurance: 'Endurance',
    generalFitness: 'General Fitness',
    bodyWeight: 'Body Weight',
    dumbbell: 'Dumbbell',
    barbell: 'Barbell',
    kettlebell: 'Kettlebell',
    band: 'Band',
    cable: 'Cable',
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
