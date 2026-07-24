-- ============================================================
-- STUDER - Cleanup blocks & courses tables (PostgreSQL)
-- ============================================================
TRUNCATE user_course_favs RESTART IDENTITY CASCADE;
TRUNCATE user_course_blocks RESTART IDENTITY CASCADE;
TRUNCATE course_blocks RESTART IDENTITY CASCADE;
TRUNCATE course_tags RESTART IDENTITY CASCADE;
TRUNCATE courses RESTART IDENTITY CASCADE;
TRUNCATE block_likes RESTART IDENTITY CASCADE;
TRUNCATE block_tags RESTART IDENTITY CASCADE;
TRUNCATE blocks_versions RESTART IDENTITY CASCADE;
TRUNCATE blocks RESTART IDENTITY CASCADE;
