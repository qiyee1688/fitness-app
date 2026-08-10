DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'workout_template_status_enum'
    ) THEN
        CREATE TYPE workout_template_status_enum AS ENUM ('ACTIVE', 'NEEDS_REPAIR');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS workout_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE RESTRICT,
    name VARCHAR(80) NOT NULL,
    body_part on_demand_body_part_enum NOT NULL,
    equipment_snapshot JSONB NOT NULL,
    status workout_template_status_enum NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_template_source_workout UNIQUE (owner_user_id, source_workout_id)
);

CREATE TABLE IF NOT EXISTS workout_template_exercises (
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

CREATE INDEX IF NOT EXISTS idx_workout_templates_owner_status ON workout_templates(owner_user_id, status);
CREATE INDEX IF NOT EXISTS idx_workout_template_exercises_template_id ON workout_template_exercises(template_id);
CREATE INDEX IF NOT EXISTS idx_workout_template_exercises_exercise_id ON workout_template_exercises(exercise_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger
        WHERE tgname = 'update_workout_templates_updated_at'
          AND tgrelid = 'workout_templates'::regclass
    ) THEN
        CREATE TRIGGER update_workout_templates_updated_at BEFORE UPDATE ON workout_templates
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END
$$;
