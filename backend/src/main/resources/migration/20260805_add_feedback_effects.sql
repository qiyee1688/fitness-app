ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS removed_at TIMESTAMP;
ALTER TABLE exercise_feedbacks ADD COLUMN IF NOT EXISTS hurt_body_part VARCHAR(100);
ALTER TABLE exercise_feedbacks ADD COLUMN IF NOT EXISTS filter_until DATE;
CREATE INDEX IF NOT EXISTS idx_feedbacks_filter_until ON exercise_feedbacks(filter_until);
