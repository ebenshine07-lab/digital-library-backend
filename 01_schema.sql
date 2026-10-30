-- ============================================================================
-- DIGITAL LIBRARY MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Target Database: PostgreSQL 14+ (Compatible with Supabase PostgreSQL)
-- Layer: Persistence / DDL
-- Blueprint Standard: Strict Relational Integrity, Identity PKs, Check Constraints
-- ============================================================================

-- Clean up existing tables if running in development mode (reverse dependency order)
DROP TABLE IF EXISTS activity_logs CASCADE;
DROP TABLE IF EXISTS book_permissions CASCADE;
DROP TABLE IF EXISTS user_restrictions CASCADE;
DROP TABLE IF EXISTS user_permissions CASCADE;
DROP TABLE IF EXISTS access_requests CASCADE;
DROP TABLE IF EXISTS book_requests CASCADE;
DROP TABLE IF EXISTS downloads CASCADE;
DROP TABLE IF EXISTS reading_history CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS book_types CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Optional extension for UUID or text search if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. USERS TABLE
-- Manages student/faculty users and library administrators.
-- ============================================================================
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);

-- ============================================================================
-- 2. CATEGORIES TABLE
-- Classifies books into departments/domains (e.g., Science, Arts, Design, Cooking).
-- Includes icon identifier for UI/UX rendering as per dashboard mock.
-- ============================================================================
CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50) DEFAULT 'book',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_is_active ON categories(is_active);

-- ============================================================================
-- 3. BOOK_TYPES TABLE
-- Format or resource category (e.g., E-Book, Research Paper, Journal, Thesis).
-- ============================================================================
CREATE TABLE book_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_book_types_is_active ON book_types(is_active);

-- ============================================================================
-- 4. BOOKS TABLE
-- Central catalog repository containing metadata, Supabase storage file paths,
-- availability status, and reading metrics.
-- ============================================================================
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    pdf_path VARCHAR(500) NOT NULL,
    cover_path VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    total_pages INTEGER NOT NULL DEFAULT 1,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    download_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_books_type FOREIGN KEY (type_id) REFERENCES book_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_books_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_books_status CHECK (status IN ('AVAILABLE', 'RESTRICTED', 'ARCHIVED')),
    CONSTRAINT chk_books_total_pages CHECK (total_pages >= 1),
    CONSTRAINT chk_books_file_size CHECK (file_size_bytes >= 0),
    CONSTRAINT chk_books_counts CHECK (download_count >= 0 AND view_count >= 0)
);

CREATE INDEX idx_books_title_author ON books(title, author);
CREATE INDEX idx_books_category ON books(category_id);
CREATE INDEX idx_books_type ON books(type_id);
CREATE INDEX idx_books_status ON books(status);
CREATE INDEX idx_books_featured ON books(is_featured);

-- Full-text search index for high-speed autocomplete & search queries
CREATE INDEX idx_books_fts ON books USING gin(to_tsvector('english', title || ' ' || author));

-- ============================================================================
-- 5. READING_HISTORY TABLE
-- Tracks user reading progression, current page, completion status, and timestamps.
-- Powers the 'Previous Reading' UI carousel and reading resume capability.
-- ============================================================================
CREATE TABLE reading_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    current_page INTEGER NOT NULL DEFAULT 1,
    progress NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT uq_history_user_book UNIQUE (user_id, book_id),
    CONSTRAINT chk_history_page CHECK (current_page >= 1),
    CONSTRAINT chk_history_progress CHECK (progress >= 0.00 AND progress <= 100.00)
);

CREATE INDEX idx_reading_history_user_last_read ON reading_history(user_id, last_read_at DESC);

-- ============================================================================
-- 6. DOWNLOADS TABLE
-- Immutable record of authorized document downloads by authenticated users.
-- ============================================================================
CREATE TABLE downloads (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    downloaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),

    CONSTRAINT fk_downloads_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_downloads_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

CREATE INDEX idx_downloads_user ON downloads(user_id, downloaded_at DESC);
CREATE INDEX idx_downloads_book ON downloads(book_id);

-- ============================================================================
-- 7. BOOK_REQUESTS TABLE
-- Submissions by users requesting new or unavailable titles for library acquisition.
-- ============================================================================
CREATE TABLE book_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_response TEXT,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_book_req_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_req_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_book_req_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_book_requests_user ON book_requests(user_id);
CREATE INDEX idx_book_requests_status ON book_requests(status);

-- ============================================================================
-- 8. ACCESS_REQUESTS TABLE
-- Submissions by users requesting elevated access to restricted books.
-- ============================================================================
CREATE TABLE access_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    admin_response TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_acc_req_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_acc_req_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_acc_req_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_acc_req_status CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED'))
);

CREATE INDEX idx_access_requests_user ON access_requests(user_id);
CREATE INDEX idx_access_requests_book ON access_requests(book_id);
CREATE INDEX idx_access_requests_status ON access_requests(status);

-- ============================================================================
-- 9. USER_PERMISSIONS TABLE
-- General baseline capabilities assigned to a user account.
-- ============================================================================
CREATE TABLE user_permissions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    can_use_app BOOLEAN NOT NULL DEFAULT TRUE,
    can_read_books BOOLEAN NOT NULL DEFAULT TRUE,
    can_download_books BOOLEAN NOT NULL DEFAULT TRUE,
    can_request_books BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_perms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_perms_user_id ON user_permissions(user_id);

-- ============================================================================
-- 10. USER_RESTRICTIONS TABLE
-- Temporary or scheduled sanctions imposed on user accounts with validity windows.
-- ============================================================================
CREATE TABLE user_restrictions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    restriction_type VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_restr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_restr_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_user_restr_type CHECK (restriction_type IN ('SUSPEND_ALL', 'BLOCK_READING', 'BLOCK_DOWNLOADING', 'BLOCK_REQUESTING')),
    CONSTRAINT chk_user_restr_dates CHECK (expires_at > starts_at)
);

CREATE INDEX idx_user_restr_user_active ON user_restrictions(user_id, is_active, starts_at, expires_at);

-- ============================================================================
-- 11. BOOK_PERMISSIONS TABLE
-- Specific book-level access grants or overrides overriding default restrictions.
-- ============================================================================
CREATE TABLE book_permissions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    can_read BOOLEAN NOT NULL DEFAULT TRUE,
    can_download BOOLEAN NOT NULL DEFAULT FALSE,
    starts_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_book_perms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_perms_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_perms_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_book_perms_user_book UNIQUE (user_id, book_id),
    CONSTRAINT chk_book_perms_dates CHECK (expires_at IS NULL OR expires_at > starts_at)
);

CREATE INDEX idx_book_perms_lookup ON book_permissions(user_id, book_id);

-- ============================================================================
-- 12. ACTIVITY_LOGS TABLE
-- Comprehensive audit trail recording user and administrative operations.
-- ============================================================================
CREATE TABLE activity_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    metadata JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_action ON activity_logs(action);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC);

-- ============================================================================
-- AUTOMATED TRIGGERS FOR UPDATED_AT
-- ============================================================================
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER trg_books_updated_at
BEFORE UPDATE ON books
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER trg_book_requests_updated_at
BEFORE UPDATE ON book_requests
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER trg_access_requests_updated_at
BEFORE UPDATE ON access_requests
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER trg_user_permissions_updated_at
BEFORE UPDATE ON user_permissions
FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

