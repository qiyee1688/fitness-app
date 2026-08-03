const value_translations = {
  '3/4 sit-up': '四分之三仰卧起坐',
  '45 degree side bend': '45 度侧屈',
  'world greatest stretch': '世界最佳拉伸',
  'wind sprints': '冲刺跑',
  'wrist circles': '手腕绕环',
  'wrist rollerer': '腕力卷轴',
  'wide-grip chest dip on high parallel bars': '高位双杠宽握胸部臂屈伸',
  'wide hand push up': '宽距俯卧撑',
  'wide grip pull-up': '宽握引体向上',
  'wide grip rear pull-up': '宽握颈后引体向上',

  back: '背部',
  cardio: '有氧',
  chest: '胸部',
  lower_arms: '小臂',
  lower_legs: '小腿',
  neck: '颈部',
  shoulders: '肩部',
  upper_arms: '上臂',
  upper_legs: '大腿',
  waist: '核心',

  abdominals: '腹肌',
  abs: '腹肌',
  abductors: '外展肌',
  adductors: '内收肌',
  biceps: '肱二头肌',
  calves: '小腿肌群',
  delts: '三角肌',
  forearms: '前臂肌群',
  glutes: '臀肌',
  hamstrings: '腘绳肌',
  hands: '手部',
  lats: '背阔肌',
  pectorals: '胸肌',
  quads: '股四头肌',
  quadriceps: '股四头肌',
  serratus_anterior: '前锯肌',
  spine: '脊柱',
  traps: '斜方肌',
  triceps: '肱三头肌',
  wrists: '手腕',

  assisted: '辅助',
  band: '弹力带',
  barbell: '杠铃',
  body_weight: '自重',
  bosu_ball: '波速球',
  cable: '绳索',
  dumbbell: '哑铃',
  elliptical_machine: '椭圆机',
  ez_barbell: 'EZ 杠',
  hammer: '锤',
  kettlebell: '壶铃',
  leverage_machine: '固定器械',
  medicine_ball: '药球',
  olympic_barbell: '奥林匹克杠铃',
  resistance_band: '阻力带',
  roller: '泡沫轴',
  rope: '绳',
  skierg_machine: '滑雪机',
  sled_machine: '雪橇机',
  smith_machine: '史密斯机',
  stability_ball: '稳定球',
  stationary_bike: '固定单车',
  stepmill_machine: '登阶机',
  tire: '轮胎',
  trap_bar: '六角杠',
  upper_body_ergometer: '上肢功率车',
  weighted: '负重',
  wheel_roller: '健腹轮',
}

const phrase_translations = [
  ['alternate heel touchers', '交替触踵'],
  ['bench press', '卧推'],
  ['bent over row', '俯身划船'],
  ['chest dip', '胸部臂屈伸'],
  ['deadlift', '硬拉'],
  ['front raise', '前平举'],
  ['hammer curl', '锤式弯举'],
  ['hip thrust', '臀推'],
  ['jump squat', '跳深蹲'],
  ['lateral raise', '侧平举'],
  ['leg curl', '腿弯举'],
  ['leg extension', '腿屈伸'],
  ['pull-up', '引体向上'],
  ['push up', '俯卧撑'],
  ['push-up', '俯卧撑'],
  ['rear delt', '后三角'],
  ['romanian deadlift', '罗马尼亚硬拉'],
  ['russian twist', '俄罗斯转体'],
  ['shoulder press', '肩推'],
  ['side bend', '侧屈'],
  ['sit-up', '仰卧起坐'],
  ['squat', '深蹲'],
  ['triceps extension', '肱三头肌伸展'],
  ['wrist curl', '腕弯举'],
]

const word_translations = {
  alternate: '交替',
  assisted: '辅助',
  back: '背部',
  barbell: '杠铃',
  bench: '凳上',
  biceps: '肱二头肌',
  cable: '绳索',
  calf: '小腿',
  chest: '胸部',
  circles: '绕环',
  curl: '弯举',
  decline: '下斜',
  dumbbell: '哑铃',
  extension: '伸展',
  front: '前',
  grip: '握距',
  hand: '手',
  high: '高位',
  incline: '上斜',
  jump: '跳',
  knee: '膝',
  kneeling: '跪姿',
  lateral: '侧向',
  leg: '腿',
  low: '低位',
  lying: '仰卧',
  machine: '器械',
  narrow: '窄距',
  overhead: '过顶',
  press: '推举',
  pull: '拉',
  raise: '平举',
  rear: '后',
  reverse: '反向',
  roller: '滚轮',
  seated: '坐姿',
  shoulder: '肩部',
  side: '侧向',
  standing: '站姿',
  stretch: '拉伸',
  triceps: '肱三头肌',
  twist: '转体',
  weighted: '负重',
  wide: '宽距',
  wrist: '手腕',
}

function normalize_key(value) {
  return String(value || '')
    .trim()
    .toLowerCase()
    .replaceAll('-', '_')
    .replaceAll(' ', '_')
}

export function display_value(value, language) {
  if (!value) {
    return '-'
  }

  if (language !== 'zh') {
    return value
  }

  return value_translations[normalize_key(value)] || value_translations[String(value).trim().toLowerCase()] || value
}

export function display_list(values, language) {
  if (!values?.length) {
    return '-'
  }

  return values.map((value) => display_value(value, language)).join(language === 'zh' ? ' / ' : ' / ')
}

export function display_exercise_name(name, language) {
  if (!name || language !== 'zh') {
    return name || '-'
  }

  const exact_translation = value_translations[String(name).trim().toLowerCase()]
  if (exact_translation) {
    return exact_translation
  }

  let translated_name = String(name).toLowerCase()
  phrase_translations.forEach(([source, target]) => {
    translated_name = translated_name.replaceAll(source, target)
  })

  const tokens = translated_name.split(/([\s()/,-]+)/)
  const translated_tokens = tokens.map((token) => {
    const key = normalize_key(token)
    return word_translations[key] || token
  })

  return translated_tokens.join('').replace(/\s+/g, ' ').trim()
}
