ALTER TABLE workouts
    ADD COLUMN IF NOT EXISTS replaced_workout_id UUID REFERENCES workouts(id) ON DELETE RESTRICT;

ALTER TABLE workouts
    DROP CONSTRAINT IF EXISTS uq_plan_day;

CREATE UNIQUE INDEX IF NOT EXISTS uq_workouts_active_plan_day
    ON workouts(plan_id, day_number)
    WHERE status <> 'REPLACED'::workout_status_enum;
