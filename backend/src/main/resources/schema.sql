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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exercises_category ON exercises(category);
CREATE INDEX idx_exercises_body_part ON exercises(body_part);
CREATE INDEX idx_exercises_equipment ON exercises(equipment);
CREATE INDEX idx_exercises_target ON exercises(target);
CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);

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

CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    day_number INT NOT NULL,
    focus training_day_focus_enum NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_plan FOREIGN KEY (plan_id) REFERENCES plans(id),
    CONSTRAINT uq_plan_day UNIQUE (plan_id, day_number)
);

CREATE INDEX idx_workouts_plan_id ON workouts(plan_id);
CREATE INDEX idx_workouts_completed_at ON workouts(completed_at);

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_workout FOREIGN KEY (workout_id) REFERENCES workouts(id),
    CONSTRAINT fk_prescription_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id)
);

CREATE INDEX idx_prescriptions_workout_id ON prescriptions(workout_id);
CREATE INDEX idx_prescriptions_exercise_id ON prescriptions(exercise_id);

-- ============================================================
-- Table: exercise_feedbacks (动作反馈)
-- ============================================================
CREATE TABLE exercise_feedbacks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    feedback_type VARCHAR(100) NOT NULL,
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
