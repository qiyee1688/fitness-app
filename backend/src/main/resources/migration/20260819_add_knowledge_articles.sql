DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'knowledge_article_status_enum') THEN
        CREATE TYPE knowledge_article_status_enum AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
    END IF;
END
$$;

INSERT INTO editors (id, display_name, display_name_en)
VALUES ('20000000-0000-0000-0000-000000000001', '健身计划编辑部', 'Fitness Plan Editorial Team')
ON CONFLICT (id) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    display_name_en = EXCLUDED.display_name_en;

INSERT INTO knowledge_articles (
    id, editor_id, slug, title, title_en, summary, summary_en, body, body_en,
    status, published_at
) VALUES
(
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'build-a-sustainable-training-habit',
    '从每周两次开始，建立可持续训练习惯',
    'Build a Sustainable Training Habit from Two Sessions a Week',
    '先稳定出现，再逐步增加训练量：用简单动作和明确的恢复节奏开始。',
    'Start by showing up consistently, then increase training volume with simple movements and clear recovery.',
    '刚开始训练时，最重要的不是把每一次都练到极限，而是把训练安排成你愿意长期重复的习惯。\n\n每周选择两次固定时段，优先完成全身或核心基础动作。动作过程中保留 2 到 3 次余力，结束后记录感觉、睡眠和酸痛。连续完成几周后，再考虑增加组数、重量或训练天数。',
    'When you are starting out, the priority is not pushing every session to the limit. Build a routine you can repeat for a long time.\n\nChoose two fixed times each week and begin with full-body or core basics. Keep two to three repetitions in reserve, then note how you felt, slept, and recovered. After a few consistent weeks, consider adding sets, load, or another training day.',
    'PUBLISHED',
    TIMESTAMP '2026-08-20 09:00:00'
),
(
    '30000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000001',
    'core-training-without-rushing-reps',
    '核心训练别赶次数：用控制换来更稳定的发力',
    'Core Training Without Rushing Reps',
    '核心动作的质量来自节奏和躯干控制，而不是更快地完成次数。',
    'Core exercise quality comes from tempo and trunk control, not finishing repetitions faster.',
    '做卷腹和侧屈时，先用呼气收紧腹部，再缓慢移动躯干。避免借助惯性猛起或让腰部过度拱起。\n\n如果动作范围变小、颈部紧张或腰部不舒服，可以减少次数、缩小幅度，优先找回稳定控制。每一次重复都应该让你感觉腹部在工作，而不是只是在完成数字。',
    'For sit-ups and side bends, brace your abdomen with an exhale before moving your torso slowly. Avoid jerking with momentum or over-arching your lower back.\n\nIf range of motion shrinks, your neck gets tense, or your lower back feels uncomfortable, reduce repetitions and range first to restore control. Each repetition should feel like your core is working, not like you are only chasing a number.',
    'PUBLISHED',
    TIMESTAMP '2026-08-20 09:10:00'
),
(
    '30000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000001',
    'recovery-checklist-after-your-workout',
    '训练后恢复清单：下一次练得更好的三个信号',
    'A Post-Workout Recovery Checklist',
    '从补水、轻度活动和睡眠中观察恢复情况，为下一次训练调整节奏。',
    'Use hydration, light movement, and sleep to observe recovery and adjust your next session.',
    '训练结束后不必立刻增加更多训练量。先补充水分和正常的一餐，做几分钟轻松走动，并关注当天晚上的睡眠。\n\n第二天轻微酸痛通常可以接受；如果疼痛明显、动作受限或疲劳持续累积，请降低下一次训练的难度，并检查动作控制和恢复安排。',
    'After training, you do not need to add more work immediately. Rehydrate, eat a normal meal, take a few minutes of easy walking, and notice your sleep that night.\n\nMild soreness the next day is usually manageable. If pain is sharp, movement is limited, or fatigue keeps accumulating, reduce the next session and review movement control and recovery.',
    'PUBLISHED',
    TIMESTAMP '2026-08-20 09:20:00'
)
ON CONFLICT (slug) DO UPDATE SET
    editor_id = EXCLUDED.editor_id,
    title = EXCLUDED.title,
    title_en = EXCLUDED.title_en,
    summary = EXCLUDED.summary,
    summary_en = EXCLUDED.summary_en,
    body = EXCLUDED.body,
    body_en = EXCLUDED.body_en,
    status = EXCLUDED.status,
    published_at = EXCLUDED.published_at;

INSERT INTO article_references (article_id, exercise_id, display_order)
VALUES
    ('30000000-0000-0000-0000-000000000001', '0001', 1),
    ('30000000-0000-0000-0000-000000000001', '0002', 2),
    ('30000000-0000-0000-0000-000000000002', '0001', 1),
    ('30000000-0000-0000-0000-000000000002', '0002', 2),
    ('30000000-0000-0000-0000-000000000003', '0003', 1)
ON CONFLICT (article_id, exercise_id) DO UPDATE SET
    display_order = EXCLUDED.display_order;

CREATE TABLE IF NOT EXISTS editors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    display_name VARCHAR(120) NOT NULL,
    display_name_en VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_articles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    editor_id UUID NOT NULL REFERENCES editors(id) ON DELETE RESTRICT,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    title_en VARCHAR(200) NOT NULL,
    summary TEXT NOT NULL,
    summary_en TEXT NOT NULL,
    body TEXT NOT NULL,
    body_en TEXT NOT NULL,
    cover_image_url TEXT,
    status knowledge_article_status_enum NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_published_article_date CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS article_references (
    article_id UUID NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    display_order INT NOT NULL CHECK (display_order > 0),
    PRIMARY KEY (article_id, exercise_id),
    CONSTRAINT uq_article_reference_order UNIQUE (article_id, display_order)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_published
    ON knowledge_articles(published_at DESC, id)
    WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_article_references_exercise_id
    ON article_references(exercise_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger
        WHERE tgname = 'update_knowledge_articles_updated_at'
          AND tgrelid = 'knowledge_articles'::regclass
    ) THEN
        CREATE TRIGGER update_knowledge_articles_updated_at BEFORE UPDATE ON knowledge_articles
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END
$$;
