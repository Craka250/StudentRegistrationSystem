
-- ============================================================
-- STUDENT REGISTRATION AND MANAGEMENT SYSTEM
-- Database: srms_db
-- PostgreSQL: 18+
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1. DEPARTMENTS
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_departments_name
ON departments (LOWER(TRIM(name)));


-- ------------------------------------------------------------
-- 2. PROGRAMMES
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS programmes (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    department_id UUID NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_programmes_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_programmes_name_department
ON programmes (LOWER(TRIM(name)), department_id);

CREATE INDEX IF NOT EXISTS idx_programmes_department
ON programmes (department_id);


-- ------------------------------------------------------------
-- 3. STUDENTS
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS students (
    registration_number VARCHAR(40) PRIMARY KEY,

    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50) NOT NULL DEFAULT '',
    last_name VARCHAR(50) NOT NULL,

    gender VARCHAR(30) NOT NULL,
    date_of_birth DATE NOT NULL,

    email VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,

    programme_id UUID NOT NULL,

    year_of_study SMALLINT NOT NULL
        CHECK (year_of_study BETWEEN 1 AND 6),

    admission_date DATE NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_students_programme
        FOREIGN KEY (programme_id)
        REFERENCES programmes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_student_registration_number
        CHECK (LENGTH(TRIM(registration_number)) > 0),

    CONSTRAINT chk_student_email
        CHECK (POSITION('@' IN email) > 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_students_email
ON students (LOWER(TRIM(email)));

CREATE UNIQUE INDEX IF NOT EXISTS uq_students_phone
ON students (phone_number);

CREATE INDEX IF NOT EXISTS idx_students_programme
ON students (programme_id);

CREATE INDEX IF NOT EXISTS idx_students_admission_date
ON students (admission_date);

CREATE INDEX IF NOT EXISTS idx_students_full_name
ON students (last_name, first_name);


-- ------------------------------------------------------------
-- 4. STUDENT PHOTOS
-- Stores file references, not large image binaries.
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS student_photos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    registration_number VARCHAR(40) NOT NULL UNIQUE,
    file_path TEXT NOT NULL,
    original_filename VARCHAR(255),
    content_type VARCHAR(50),
    file_size BIGINT,

    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_student_photos_student
        FOREIGN KEY (registration_number)
        REFERENCES students(registration_number)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_student_photo_size
        CHECK (file_size IS NULL OR file_size BETWEEN 1 AND 5242880)
);


-- ------------------------------------------------------------
-- 5. ADMINISTRATOR
-- One administrator account only.
-- Passwords must be securely hashed by the application.
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS admin_users (
    id SMALLINT PRIMARY KEY DEFAULT 1
        CHECK (id = 1),

    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ
);


-- ------------------------------------------------------------
-- 6. AUDIT LOGS
-- Records important administrator activities.
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    admin_user_id SMALLINT,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(100),

    details JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_admin
        FOREIGN KEY (admin_user_id)
        REFERENCES admin_users(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at
ON audit_logs (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
ON audit_logs (entity_type, entity_id);


-- ------------------------------------------------------------
-- 7. SYSTEM SETTINGS
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT,
    description TEXT,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ------------------------------------------------------------
-- 8. INITIAL ACADEMIC DATA
-- Matches the existing application seed data.
-- ------------------------------------------------------------

INSERT INTO departments (id, name, description)
VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'Computing and Information Technology',
    'Department responsible for computing and information technology programmes.'
),
(
    '10000000-0000-0000-0000-000000000002',
    'Business and Management',
    'Department responsible for business and management programmes.'
)
ON CONFLICT (id) DO NOTHING;


INSERT INTO programmes (id, name, department_id, description)
VALUES
(
    '20000000-0000-0000-0000-000000000001',
    'Bachelor of Software Engineering',
    '10000000-0000-0000-0000-000000000001',
    'Undergraduate programme in software engineering.'
),
(
    '20000000-0000-0000-0000-000000000002',
    'Bachelor of Computer Science',
    '10000000-0000-0000-0000-000000000001',
    'Undergraduate programme in computer science.'
),
(
    '20000000-0000-0000-0000-000000000003',
    'Bachelor of Information Technology',
    '10000000-0000-0000-0000-000000000001',
    'Undergraduate programme in information technology.'
),
(
    '20000000-0000-0000-0000-000000000004',
    'Diploma in Information Technology',
    '10000000-0000-0000-0000-000000000001',
    'Diploma programme in information technology.'
),
(
    '20000000-0000-0000-0000-000000000005',
    'Bachelor of Business Information Technology',
    '10000000-0000-0000-0000-000000000002',
    'Undergraduate programme combining business and information technology.'
)
ON CONFLICT (id) DO NOTHING;


-- ------------------------------------------------------------
-- 9. APPLICATION PERMISSIONS
-- ------------------------------------------------------------

GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO srms_app;

GRANT USAGE, SELECT
ON ALL SEQUENCES IN SCHEMA public
TO srms_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO srms_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO srms_app;

COMMIT;

