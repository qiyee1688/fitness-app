CREATE TYPE on_demand_body_part_enum AS ENUM ('CHEST', 'BACK', 'SHOULDERS', 'LEGS', 'WAIST');
CREATE TYPE workout_source_enum AS ENUM ('PLAN_GENERATED', 'ON_DEMAND', 'TEMPLATE_REPLACEMENT');
CREATE TYPE workout_status_enum AS ENUM ('DRAFT', 'READY', 'IN_PROGRESS', 'COMPLETED', 'REPLACED');

ALTER TABLE workouts
    ADD COLUMN owner_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    ADD COLUMN requested_body_part on_demand_body_part_enum,
    ADD COLUMN equipment_snapshot JSONB,
    ADD COLUMN source workout_source_enum NOT NULL DEFAULT 'PLAN_GENERATED',
    ADD COLUMN status workout_status_enum NOT NULL DEFAULT 'READY',
    ADD COLUMN started_at TIMESTAMP,
    ADD COLUMN expires_at TIMESTAMP;

UPDATE workouts w
SET owner_user_id = p.user_id
FROM plans p
WHERE w.plan_id = p.id AND w.owner_user_id IS NULL;

UPDATE workouts
SET status = 'COMPLETED'::workout_status_enum
WHERE completed_at IS NOT NULL;

ALTER TABLE workouts
    ALTER COLUMN owner_user_id SET NOT NULL,
    ALTER COLUMN plan_id DROP NOT NULL,
    ALTER COLUMN day_number DROP NOT NULL,
    ALTER COLUMN focus DROP NOT NULL;

CREATE INDEX idx_workouts_owner_source_status
    ON workouts(owner_user_id, source, status);
CREATE INDEX idx_workouts_expires_at
    ON workouts(expires_at) WHERE status = 'DRAFT';
