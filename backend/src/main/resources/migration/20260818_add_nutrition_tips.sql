DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type WHERE typname = 'nutrition_timing_enum'
    ) THEN
        CREATE TYPE nutrition_timing_enum AS ENUM ('PRE_WORKOUT', 'POST_WORKOUT', 'DAILY');
    END IF;
END
$$;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS weight_kg DECIMAL(5, 1)
    CHECK (weight_kg BETWEEN 30.0 AND 300.0);

CREATE TABLE IF NOT EXISTS nutrition_rules (
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

DROP INDEX IF EXISTS uq_nutrition_rules_enabled_condition;

CREATE UNIQUE INDEX IF NOT EXISTS uq_nutrition_rules_enabled_no_focus
    ON nutrition_rules (goal, timing)
    WHERE enabled = TRUE AND focus IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_nutrition_rules_enabled_with_focus
    ON nutrition_rules (goal, focus, timing)
    WHERE enabled = TRUE AND focus IS NOT NULL;

CREATE TABLE IF NOT EXISTS nutrition_tips (
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

CREATE INDEX IF NOT EXISTS idx_nutrition_tips_workout_id ON nutrition_tips(workout_id);
CREATE INDEX IF NOT EXISTS idx_nutrition_rules_goal_timing ON nutrition_rules(goal, timing, enabled);

INSERT INTO nutrition_rules (
    id, business_key, goal, focus, timing, formula, note, note_en, version, enabled
) VALUES
(
    '10000000-0000-0000-0000-000000000001', 'MUSCLE_GAIN_PRE_WORKOUT', 'MUSCLE_GAIN', NULL,
    'PRE_WORKOUT',
    '{"protein":{"perKg":0.3,"absolute":20,"unit":"GRAMS"},"carbs":{"perKg":0.8,"absolute":50,"unit":"GRAMS"}}'::jsonb,
    '练前补一点蛋白质和碳水，训练时更有力气，也不容易练到一半就没电。',
    'A little protein and carbs before training can keep your energy steady through the session.',
    1, TRUE
),
(
    '10000000-0000-0000-0000-000000000002', 'MUSCLE_GAIN_POST_WORKOUT', 'MUSCLE_GAIN', NULL,
    'POST_WORKOUT',
    '{"protein":{"perKg":0.4,"absolute":30,"unit":"GRAMS"},"carbs":{"perKg":1.0,"absolute":60,"unit":"GRAMS"}}'::jsonb,
    '练完后把蛋白质和碳水补回来，给肌肉修复和下一次训练留足材料。',
    'Refuel with protein and carbs after training so your muscles have what they need to recover.',
    1, TRUE
),
(
    '10000000-0000-0000-0000-000000000003', 'MUSCLE_GAIN_DAILY', 'MUSCLE_GAIN', NULL,
    'DAILY',
    '{"protein":{"perKg":1.8,"absolute":120,"unit":"GRAMS"},"carbs":{"perKg":4.0,"absolute":250,"unit":"GRAMS"},"fat":{"perKg":0.8,"absolute":55,"unit":"GRAMS"},"kcal":{"perKg":35.0,"absolute":2400,"unit":"KILOCALORIES"}}'::jsonb,
    '全天先把蛋白质吃够，再用碳水和脂肪把训练需要的能量补齐。',
    'Across the day, hit your protein first, then fill in the energy you need with carbs and fats.',
    1, TRUE
),
(
    '10000000-0000-0000-0000-000000000004', 'FAT_LOSS_PRE_WORKOUT', 'FAT_LOSS', NULL,
    'PRE_WORKOUT',
    '{"protein":{"perKg":0.3,"absolute":20,"unit":"GRAMS"},"carbs":{"perKg":0.5,"absolute":30,"unit":"GRAMS"}}'::jsonb,
    '练前吃得简单一点，保留训练需要的能量，不用为了减脂把自己饿到发软。',
    'Keep your pre-workout fuel simple: enough energy to train, without trying to exercise on empty.',
    1, TRUE
),
(
    '10000000-0000-0000-0000-000000000005', 'FAT_LOSS_POST_WORKOUT', 'FAT_LOSS', NULL,
    'POST_WORKOUT',
    '{"protein":{"perKg":0.4,"absolute":25,"unit":"GRAMS"},"carbs":{"perKg":0.6,"absolute":40,"unit":"GRAMS"}}'::jsonb,
    '练完优先补蛋白质，配一点碳水帮助恢复，减脂不等于训练后什么都不吃。',
    'Prioritize protein after training and add some carbs for recovery; fat loss does not mean skipping your next meal.',
    1, TRUE
),
(
    '10000000-0000-0000-0000-000000000006', 'FAT_LOSS_DAILY', 'FAT_LOSS', NULL,
    'DAILY',
    '{"protein":{"perKg":1.8,"absolute":110,"unit":"GRAMS"},"carbs":{"perKg":2.5,"absolute":160,"unit":"GRAMS"},"fat":{"perKg":0.7,"absolute":45,"unit":"GRAMS"},"kcal":{"perKg":28.0,"absolute":1800,"unit":"KILOCALORIES"}}'::jsonb,
    '全天保持稳定的蛋白质和总能量，慢慢减脂比忽高忽低的节食更容易坚持。',
    'Keep protein and total energy steady across the day; gradual fat loss is easier to sustain than crash dieting.',
    1, TRUE
)
ON CONFLICT (business_key) DO UPDATE SET
    goal = EXCLUDED.goal,
    focus = EXCLUDED.focus,
    timing = EXCLUDED.timing,
    formula = EXCLUDED.formula,
    note = EXCLUDED.note,
    note_en = EXCLUDED.note_en,
    version = EXCLUDED.version,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP
WHERE nutrition_rules.version < EXCLUDED.version;
