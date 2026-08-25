export function has_substitute(adjustment) {
  return adjustment.suggestedExerciseId
    && adjustment.suggestedExerciseId !== adjustment.originalPrescription?.exerciseId
}

export function prescription_difference(adjustment, labels) {
  const before = adjustment.originalPrescription || {}
  const after = adjustment.suggestedPrescription || {}
  return [
    { key: 'sets', label: labels.sets, before: display_value(before.sets), after: display_value(after.sets) },
    { key: 'reps', label: labels.reps, before: display_value(before.reps), after: display_value(after.reps) },
    { key: 'load', label: labels.load, before: load_value(before), after: load_value(after) },
    { key: 'rpe', label: labels.rpe, before: display_value(before.rpe), after: display_value(after.rpe) },
  ].map((field) => ({ ...field, changed: field.before !== field.after }))
}

export function adjustment_status_key(status) {
  return {
    PENDING: 'adjustmentPending',
    ACCEPTED: 'adjustmentAcceptedStatus',
    DECLINED: 'adjustmentDeclinedStatus',
    EXPIRED: 'adjustmentExpiredStatus',
  }[status] || status
}

function load_value(prescription) {
  if (prescription.load === null || prescription.load === undefined) {
    return prescription.loadType || '-'
  }
  return `${prescription.load} ${prescription.loadType || ''}`.trim()
}

function display_value(value) {
  return value === null || value === undefined || value === '' ? '-' : String(value)
}
