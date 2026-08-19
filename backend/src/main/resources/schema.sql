-- Fitness App Database Schema
-- PostgreSQL 15+

-- 扩展：支持 JSONB
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- Table: exercises (动作)
-- ============================================================
CREATE TABLE exercises (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    body_part VARCHAR(100) NOT NULL,
    equipment VARCHAR(100) NOT NULL,
    target VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(100),
    secondary_muscles JSONB,
    instruction_steps JSONB NOT NULL,
    gif_url TEXT,
    image_url TEXT,
    coach_cue TEXT,
    coach_cue_en TEXT,
    selection_priority INT NOT NULL DEFAULT 1000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercises_category ON exercises(category);
CREATE INDEX idx_exercises_body_part ON exercises(body_part);
CREATE INDEX idx_exercises_equipment ON exercises(equipment);
CREATE INDEX idx_exercises_target ON exercises(target);
CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);
CREATE INDEX idx_exercises_selection_priority ON exercises(selection_priority, body_part, equipment);

-- ============================================================
-- Table: users (用户)
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- Table: user_profiles (用户档案)
-- ============================================================
CREATE TYPE fitness_level_enum AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED');
CREATE TYPE goal_enum AS ENUM ('FAT_LOSS', 'MUSCLE_GAIN', 'ENDURANCE', 'GENERAL_FITNESS');

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fitness_level fitness_level_enum NOT NULL,
    goal goal_enum NOT NULL,
    days_per_week INT NOT NULL CHECK (days_per_week BETWEEN 2 AND 6),
    available_equipment JSONB NOT NULL,
    weight_kg DECIMAL(5, 1) CHECK (weight_kg BETWEEN 30.0 AND 300.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- ============================================================
-- Table: plans (训练计划)
-- ============================================================
CREATE TYPE plan_status_enum AS ENUM ('DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'SUPERSEDED', 'CANCELLED');

CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    profile_snapshot JSONB NOT NULL,
    status plan_status_enum NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    parent_plan_id UUID REFERENCES plans(id) ON DELETE SET NULL,
    version INT NOT NULL DEFAULT 0,
    status_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_plan_parent FOREIGN KEY (parent_plan_id) REFERENCES plans(id)
);

CREATE INDEX idx_plans_user_id ON plans(user_id);
CREATE INDEX idx_plans_status ON plans(status);
CREATE INDEX idx_plans_start_date ON plans(start_date);
CREATE UNIQUE INDEX uq_plans_one_active_per_user
    ON plans(user_id) WHERE status = 'ACTIVE';

-- ============================================================
-- Table: workouts (训练日)
-- ============================================================
CREATE TYPE training_day_focus_enum AS ENUM ('PUSH', 'PULL', 'LEGS', 'FULL_BODY');
CREATE TYPE on_demand_body_part_enum AS ENUM ('CHEST', 'BACK', 'SHOULDERS', 'LEGS', 'WAIST');
CREATE TYPE workout_source_enum AS ENUM ('PLAN_GENERATED', 'ON_DEMAND', 'TEMPLATE_REPLACEMENT');
CREATE TYPE workout_status_enum AS ENUM ('DRAFT', 'READY', 'IN_PROGRESS', 'COMPLETED', 'REPLACED');

CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID REFERENCES plans(id) ON DELETE CASCADE,
    replaced_workout_id UUID REFERENCES workouts(id) ON DELETE RESTRICT,
    day_number INT,
    focus training_day_focus_enum,
    requested_body_part on_demand_body_part_enum,
    equipment_snapshot JSONB,
    source workout_source_enum NOT NULL DEFAULT 'PLAN_GENERATED',
    status workout_status_enum NOT NULL DEFAULT 'READY',
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_plan FOREIGN KEY (plan_id) REFERENCES plans(id)
);

CREATE INDEX idx_workouts_plan_id ON workouts(plan_id);
CREATE UNIQUE INDEX uq_workouts_active_plan_day
    ON workouts(plan_id, day_number)
    WHERE status <> 'REPLACED';
CREATE INDEX idx_workouts_owner_source_status ON workouts(owner_user_id, source, status);
CREATE INDEX idx_workouts_expires_at ON workouts(expires_at) WHERE status = 'DRAFT';
CREATE INDEX idx_workouts_completed_at ON workouts(completed_at);

-- ============================================================
-- Tables: nutrition_rules / nutrition_tips (训练营养小贴士)
-- ============================================================
CREATE TYPE nutrition_timing_enum AS ENUM ('PRE_WORKOUT', 'POST_WORKOUT', 'DAILY');

CREATE TABLE nutrition_rules (
    id UUID PRIMARY KEY,
    business_key VARCHAR(100) NOT NULL UNIQUE,
    goal goal_enum NOT NULL,
    focus training_day_focus_enum,
    timing nutrition_timing_enum NOT NULL,
    formula JSONB NOT NULL,
    note TEXT NOT NULL,
    note_en TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_nutrition_rules_enabled_no_focus
    ON nutrition_rules (goal, timing)
    WHERE enabled = TRUE AND focus IS NULL;
CREATE UNIQUE INDEX uq_nutrition_rules_enabled_with_focus
    ON nutrition_rules (goal, focus, timing)
    WHERE enabled = TRUE AND focus IS NOT NULL;
CREATE INDEX idx_nutrition_rules_goal_timing ON nutrition_rules(goal, timing, enabled);

CREATE TABLE nutrition_tips (
    id UUID PRIMARY KEY,
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    timing nutrition_timing_enum NOT NULL,
    macro_targets JSONB NOT NULL,
    note TEXT,
    note_en TEXT,
    rule_id UUID NOT NULL REFERENCES nutrition_rules(id) ON DELETE RESTRICT,
    rule_version INT NOT NULL,
    weight_kg_snapshot DECIMAL(5, 1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_nutrition_tip_workout_timing UNIQUE (workout_id, timing)
);

CREATE INDEX idx_nutrition_tips_workout_id ON nutrition_tips(workout_id);

-- ============================================================
-- Table: prescriptions (动作处方)
-- ============================================================
CREATE TYPE load_type_enum AS ENUM ('ABSOLUTE_WEIGHT', 'PERCENT_1RM', 'BODYWEIGHT', 'RPE_ONLY', 'DURATION');

CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    sequence INT NOT NULL,
    sets INT NOT NULL CHECK (sets > 0),
    reps INT NOT NULL CHECK (reps > 0),
    load DECIMAL(5, 2),
    load_type load_type_enum NOT NULL DEFAULT 'BODYWEIGHT',
    rpe DECIMAL(3, 1) CHECK (rpe BETWEEN 6.0 AND 10.0),
    removed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_workout FOREIGN KEY (workout_id) REFERENCES workouts(id),
    CONSTRAINT fk_prescription_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id)
);

CREATE INDEX idx_prescriptions_workout_id ON prescriptions(workout_id);
CREATE INDEX idx_prescriptions_exercise_id ON prescriptions(exercise_id);

-- ============================================================
-- Table: workout_templates (用户保存的训练模板)
-- ============================================================
CREATE TYPE workout_template_status_enum AS ENUM ('ACTIVE', 'NEEDS_REPAIR');

CREATE TABLE workout_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE RESTRICT,
    name VARCHAR(80) NOT NULL,
    body_part on_demand_body_part_enum NOT NULL,
    equipment_snapshot JSONB NOT NULL,
    profile_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    status workout_template_status_enum NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_template_source_workout UNIQUE (owner_user_id, source_workout_id)
);

CREATE TABLE workout_template_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID NOT NULL REFERENCES workout_templates(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    sequence INT NOT NULL,
    sets INT NOT NULL CHECK (sets > 0),
    reps INT NOT NULL CHECK (reps > 0),
    load DECIMAL(5, 2),
    load_type load_type_enum NOT NULL DEFAULT 'BODYWEIGHT',
    rpe DECIMAL(3, 1) CHECK (rpe BETWEEN 6.0 AND 10.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_template_exercise_sequence UNIQUE (template_id, sequence)
);

CREATE TABLE exercise_substitutes (
    from_exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    to_exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    reason VARCHAR(32) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (from_exercise_id, to_exercise_id, reason),
    CONSTRAINT chk_exercise_substitute_distinct CHECK (from_exercise_id <> to_exercise_id),
    CONSTRAINT chk_exercise_substitute_reason CHECK (
        reason IN ('EQUIPMENT_SWAP', 'DIFFICULTY_DOWNGRADE', 'INJURY_FRIENDLY', 'OTHER')
    )
);

CREATE INDEX idx_workout_templates_owner_status ON workout_templates(owner_user_id, status);
CREATE INDEX idx_workout_template_exercises_template_id ON workout_template_exercises(template_id);
CREATE INDEX idx_workout_template_exercises_exercise_id ON workout_template_exercises(exercise_id);

-- ============================================================
-- Table: exercise_feedbacks (动作反馈)
-- ============================================================
CREATE TABLE exercise_feedbacks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    feedback_type VARCHAR(100) NOT NULL,
    hurt_body_part VARCHAR(100),
    filter_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_feedback_type CHECK (
        feedback_type IN ('TOO_EASY', 'JUST_RIGHT', 'TOO_HARD')
        OR feedback_type LIKE 'HURT_%'
    ),
    CONSTRAINT fk_feedback_workout FOREIGN KEY (workout_id) REFERENCES workouts(id),
    CONSTRAINT fk_feedback_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id)
);

CREATE INDEX idx_feedbacks_workout_id ON exercise_feedbacks(workout_id);
CREATE INDEX idx_feedbacks_exercise_id ON exercise_feedbacks(exercise_id);

-- ============================================================
-- Triggers: updated_at 自动更新
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_exercises_updated_at BEFORE UPDATE ON exercises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plans_updated_at BEFORE UPDATE ON plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workouts_updated_at BEFORE UPDATE ON workouts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workout_templates_updated_at BEFORE UPDATE ON workout_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
