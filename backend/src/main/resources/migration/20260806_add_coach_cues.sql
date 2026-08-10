ALTER TABLE exercises
    ADD COLUMN IF NOT EXISTS coach_cue TEXT,
    ADD COLUMN IF NOT EXISTS coach_cue_en TEXT,
    ADD COLUMN IF NOT EXISTS selection_priority INT NOT NULL DEFAULT 1000,
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_exercises_selection_priority
    ON exercises(selection_priority, body_part, equipment);

UPDATE exercises
SET selection_priority = 100
WHERE selection_priority = 1000
  AND id IN (
      '0662', '3211', '0493', '0289', '0314', '0025', '0047', '1430',
      '0652', '1429', '0688', '0027', '0293', '0198', '0238', '0970', '0974',
      '0334', '0405', '0310', '0380', '0178', '0219', '0997', '0041',
      '0032', '0042', '0043', '0085', '0054', '0078', '1004', '1009', '1760',
      '0336', '0534',
      '1373', '0417', '0409', '1372', '1375', '0999',
      '0274', '0464', '0472', '0620', '0872', '0687', '0735', '0175'
  );

UPDATE exercises
SET coach_cue = COALESCE(coach_cue, CASE
        WHEN body_part = 'chest' THEN '收紧核心，肩胛稳定，动作全程控制。'
        WHEN body_part = 'back' THEN '先收紧肩胛，再用背部发力，不要耸肩。'
        WHEN body_part = 'shoulders' THEN '保持躯干稳定，动作慢一点，不要借力。'
        WHEN body_part IN ('upper legs', 'lower legs') THEN '膝盖跟着脚尖方向，保持核心稳定。'
        WHEN body_part = 'waist' THEN '保持呼吸，收紧核心，避免用惯性完成动作。'
        ELSE '保持稳定姿势，控制动作节奏，专注目标肌群。'
    END),
    coach_cue_en = COALESCE(coach_cue_en, CASE
        WHEN body_part = 'chest' THEN 'Brace your core, keep your shoulder blades stable, and control every rep.'
        WHEN body_part = 'back' THEN 'Set your shoulder blades first, then drive with your back without shrugging.'
        WHEN body_part = 'shoulders' THEN 'Keep your torso steady, move slowly, and avoid using momentum.'
        WHEN body_part IN ('upper legs', 'lower legs') THEN 'Track your knees with your toes and keep your core steady.'
        WHEN body_part = 'waist' THEN 'Keep breathing, brace your core, and avoid using momentum.'
        ELSE 'Stay stable, control the tempo, and focus on the target muscle.'
    END)
WHERE coach_cue IS NULL OR coach_cue_en IS NULL;
