-- =========================================================
-- SECURE TASK MANAGER - DATABASE SCHEMA
-- PostgreSQL
-- =========================================================

-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,

    -- senha armazenada com bcrypt/argon2
    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- CHECKLISTS
-- =========================================================

CREATE TABLE checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    title VARCHAR(120) NOT NULL,
    description TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checklists_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================================================
-- TASKS
-- =========================================================

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    -- checklist opcional
    checklist_id UUID NULL,

    title VARCHAR(120) NOT NULL,
    description TEXT,

    completed BOOLEAN NOT NULL DEFAULT FALSE,

    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',

    due_date TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tasks_checklist
        FOREIGN KEY (checklist_id)
        REFERENCES checklists(id)
        ON DELETE SET NULL
);

-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX idx_checklists_user_id
    ON checklists(user_id);

CREATE INDEX idx_tasks_user_id
    ON tasks(user_id);

CREATE INDEX idx_tasks_checklist_id
    ON tasks(checklist_id);

CREATE INDEX idx_tasks_completed
    ON tasks(completed);
