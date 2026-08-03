-- Align an already-initialized local database with CONTEXT.md decisions from 2026-08-03.
-- Intended for empty local development databases created before Exercise.id was changed to dataset string IDs.

ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS prescriptions_exercise_id_fkey;
ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS fk_prescription_exercise;
ALTER TABLE exercise_feedbacks DROP CONSTRAINT IF EXISTS exercise_feedbacks_exercise_id_fkey;
ALTER TABLE exercise_feedbacks DROP CONSTRAINT IF EXISTS fk_feedback_exercise;

ALTER TABLE exercises ALTER COLUMN id DROP DEFAULT;
ALTER TABLE exercises ALTER COLUMN id TYPE VARCHAR(64) USING id::text;
ALTER TABLE prescriptions ALTER COLUMN exercise_id TYPE VARCHAR(64) USING exercise_id::text;
ALTER TABLE exercise_feedbacks ALTER COLUMN exercise_id TYPE VARCHAR(64) USING exercise_id::text;

ALTER TABLE prescriptions
    ADD CONSTRAINT fk_prescription_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT;

ALTER TABLE exercise_feedbacks
    ADD CONSTRAINT fk_feedback_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT;

ALTER TABLE user_profiles ALTER COLUMN goal TYPE VARCHAR(50) USING goal::text;
DROP TYPE IF EXISTS goal_enum;
CREATE TYPE goal_enum AS ENUM ('FAT_LOSS', 'MUSCLE_GAIN', 'ENDURANCE', 'GENERAL_FITNESS');
ALTER TABLE user_profiles ALTER COLUMN goal TYPE goal_enum USING goal::goal_enum;

ALTER TABLE plans ALTER COLUMN status DROP DEFAULT;
ALTER TABLE plans ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
DROP TYPE IF EXISTS plan_status_enum;
CREATE TYPE plan_status_enum AS ENUM ('DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'SUPERSEDED', 'CANCELLED');
ALTER TABLE plans ALTER COLUMN status TYPE plan_status_enum USING status::plan_status_enum;
ALTER TABLE plans ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE prescriptions ALTER COLUMN load_type DROP DEFAULT;
ALTER TABLE prescriptions ALTER COLUMN load_type TYPE VARCHAR(50) USING load_type::text;
DROP TYPE IF EXISTS load_type_enum;
CREATE TYPE load_type_enum AS ENUM ('ABSOLUTE_WEIGHT', 'PERCENT_1RM', 'BODYWEIGHT', 'RPE_ONLY', 'DURATION');
ALTER TABLE prescriptions ALTER COLUMN load_type TYPE load_type_enum USING load_type::load_type_enum;
ALTER TABLE prescriptions ALTER COLUMN load_type SET DEFAULT 'BODYWEIGHT';

ALTER TABLE exercise_feedbacks ALTER COLUMN feedback_type TYPE VARCHAR(100) USING feedback_type::text;
DROP TYPE IF EXISTS feedback_type_enum;
ALTER TABLE exercise_feedbacks
    ADD CONSTRAINT ck_feedback_type CHECK (
        feedback_type IN ('TOO_EASY', 'JUST_RIGHT', 'TOO_HARD')
        OR feedback_type LIKE 'HURT_%'
    );
