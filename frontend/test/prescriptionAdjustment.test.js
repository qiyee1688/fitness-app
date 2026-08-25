import test from 'node:test'
import assert from 'node:assert/strict'

import {
  adjustment_status_key,
  has_substitute,
  prescription_difference,
} from '../src/utils/prescriptionAdjustment.js'

test('renders a single RPE change without marking unchanged prescription values', () => {
  const difference = prescription_difference({
    originalPrescription: { sets: 3, reps: 10, load: null, loadType: 'RPE_ONLY', rpe: 7 },
    suggestedPrescription: { sets: 3, reps: 10, load: null, loadType: 'RPE_ONLY', rpe: 7.5 },
  }, { sets: 'Sets', reps: 'Reps', load: 'Load', rpe: 'RPE' })

  assert.deepEqual(difference.filter((field) => field.changed).map((field) => field.key), ['rpe'])
})

test('identifies a real substitute and keeps adjustment state labels explicit', () => {
  assert.equal(has_substitute({
    suggestedExerciseId: 'split-squat',
    originalPrescription: { exerciseId: 'barbell-squat' },
  }), true)
  assert.equal(has_substitute({
    suggestedExerciseId: 'barbell-squat',
    originalPrescription: { exerciseId: 'barbell-squat' },
  }), false)
  assert.equal(adjustment_status_key('EXPIRED'), 'adjustmentExpiredStatus')
})
