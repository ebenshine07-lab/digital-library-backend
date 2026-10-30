-- ============================================================================
-- DIGITAL LIBRARY MANAGEMENT SYSTEM - SAMPLE DATA SEED
-- Target Database: PostgreSQL 14+ / Supabase
-- Aligned directly with UI Mockup (Austin Kleon, Abhishek Saha, Subjects & Books)
-- Password for all seed users: Password@123
-- BCrypt Hash: $2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. SEED USERS
-- ----------------------------------------------------------------------------
INSERT INTO users (name, email, phone, password_hash, role, status, avatar_url, created_at, updated_at)
VALUES
(
    'Chief Librarian Admin',
    'admin@library.com',
    '+15551234567',
    '$2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.',
    'ROLE_ADMIN',
    'ACTIVE',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80',
    CURRENT_TIMESTAMP - INTERVAL '60 days',
    CURRENT_TIMESTAMP - INTERVAL '60 days'
),
(
    'Abhishek Saha',
    'abhishek.saha@library.com',
    '+15559876543',
    '$2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.',
    'ROLE_USER',
    'ACTIVE',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=256&q=80',
    CURRENT_TIMESTAMP - INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '45 days'
),
(
    'Sarah Jenkins',
    'sarah.jenkins@library.com',
    '+15553334444',
    '$2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.',
    'ROLE_USER',
    'ACTIVE',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days'
),
(
    'Marcus Vance (Restricted)',
    'marcus.vance@library.com',
    '+15557778888',
    '$2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.',
    'ROLE_USER',
    'ACTIVE',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=256&q=80',
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '20 days'
),
(
    'John Suspended',
    'john.suspended@library.com',
    '+15559990000',
    '$2a$10$wT3oXb87kI58JkYqj0Jbku4wO0U2q.K/3z6xXv4k5D8yK1G0fV5c.',
    'ROLE_USER',
    'SUSPENDED',
    'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=256&q=80',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days'
);

-- ----------------------------------------------------------------------------
-- 2. SEED CATEGORIES (Mapped to the Subjects Section in UI Mockup)
-- ----------------------------------------------------------------------------
INSERT INTO categories (name, description, icon, is_active)
VALUES
('Science', 'Scientific research, physics, mathematics, chemistry, and biology', 'flask', TRUE),
('Arts', 'Literature, fine arts, history, philosophy, and humanities', 'palette', TRUE),
('Commerce', 'Economics, finance, management, and entrepreneurial studies', 'trending-up', TRUE),
('Design', 'Graphic design, UI/UX, typography, architecture, and creativity', 'pen-tool', TRUE),
('Cooking', 'Culinary arts, gastronomy, nutrition, and world recipes', 'utensils', TRUE),
('Others', 'General interest, memoirs, self-help, and investigative works', 'book-open', TRUE);

-- ----------------------------------------------------------------------------
-- 3. SEED BOOK TYPES
-- ----------------------------------------------------------------------------
INSERT INTO book_types (name, is_active)
VALUES
('E-Book', TRUE),
('Research Paper', TRUE),
('Journal Article', TRUE),
('Magazine', TRUE);

-- ----------------------------------------------------------------------------
-- 4. SEED BOOKS (Directly matching UI Mockup titles, authors, and cover art)
-- ----------------------------------------------------------------------------
INSERT INTO books (title, author, description, category_id, type_id, pdf_path, cover_path, status, total_pages, file_size_bytes, download_count, view_count, is_featured, created_by)
VALUES
-- UI Section: Previous Reading & Popular Books
(
    'Steal Like an Artist',
    'Austin Kleon',
    '10 Things Nobody Told You About Being Creative. An inspiring guide to unlocking creativity in the digital age.',
    (SELECT id FROM categories WHERE name = 'Design'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/steal_like_an_artist.pdf',
    'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    160,
    4194304,
    496,
    1820,
    TRUE,
    1
),
(
    'Type Matters!',
    'Jim Williams',
    'A simple, clear and practical guide to typography and layout fundamentals for print and digital media.',
    (SELECT id FROM categories WHERE name = 'Design'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/type_matters.pdf',
    'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    160,
    6291456,
    215,
    940,
    TRUE,
    1
),
(
    'How to Become a Multi-Millionaire',
    'Ashok Saxena',
    'Actionable blueprints for financial freedom, compounding wealth, and commercial investments.',
    (SELECT id FROM categories WHERE name = 'Commerce'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/how_to_become_millionaire.pdf',
    'https://images.unsplash.com/photo-1553729459-efe14ef6055d?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    240,
    5242880,
    380,
    1450,
    TRUE,
    1
),
(
    'Show Your Work!',
    'Austin Kleon',
    '10 Ways to Share Your Creativity and Get Discovered. Groundbreaking manual on building an audience and making yourself findable.',
    (SELECT id FROM categories WHERE name = 'Design'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/show_your_work.pdf',
    'https://images.unsplash.com/photo-1532012164546-f432f2e3777f?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    224,
    4812800,
    812,
    3120,
    TRUE,
    1
),
(
    'The Complete Novels of Sherlock Holmes',
    'Arthur Conan Doyle',
    'The definitive collection of Sir Arthur Conan Doyle classic detective mysteries featuring Sherlock Holmes and Dr. Watson.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/sherlock_holmes_novels.pdf',
    'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    1088,
    15728640,
    1240,
    5890,
    TRUE,
    1
),
-- UI Section: Popular Books Sidebar
(
    'The Subtle Art of Not Giving a F*ck',
    'Mark Manson',
    'A counterintuitive approach to living a good life, cutting through delusional positivity.',
    (SELECT id FROM categories WHERE name = 'Others'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/subtle_art.pdf',
    'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    224,
    3932160,
    2450,
    9800,
    TRUE,
    1
),
(
    'The Power of Your Subconscious Mind',
    'Joseph Murphy',
    'Learn how unlocking your subconscious mind transforms mental resilience and prosperity.',
    (SELECT id FROM categories WHERE name = 'Others'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/power_subconscious_mind.pdf',
    'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    304,
    4500000,
    1890,
    7400,
    TRUE,
    1
),
(
    'A Girl to Remember',
    'Ajay K. Pandey',
    'An emotional tale of courage, love, and sacrifice in contemporary literature.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/girl_to_remember.pdf',
    'https://images.unsplash.com/photo-1476275466078-4007374efbbe?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    216,
    3200000,
    920,
    3800,
    FALSE,
    1
),
(
    'Murder on the Orient Express',
    'Agatha Christie',
    'Hercule Poirot investigates a gripping murder aboard the luxury Orient Express locomotive.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/orient_express.pdf',
    'https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=400&q=80',
    'RESTRICTED',
    274,
    4100000,
    640,
    4100,
    FALSE,
    1
),
(
    'Everything Is F*cked: A Book About Hope',
    'Mark Manson',
    'An exploration of reason, religion, and the politics of our modern fragmented world.',
    (SELECT id FROM categories WHERE name = 'Others'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/everything_is_fcked.pdf',
    'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    288,
    4800000,
    1150,
    4900,
    FALSE,
    1
),
(
    'Attitude Is Everything',
    'Jeff Keller',
    'Change your attitude and change your life through 12 life-changing lessons.',
    (SELECT id FROM categories WHERE name = 'Others'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/attitude_is_everything.pdf',
    'https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    144,
    2800000,
    1400,
    5100,
    FALSE,
    1
),
(
    'Bulletproof Memory',
    'Scott Sharp',
    'Neuroscience-backed mnemonic techniques to retain complex information rapidly.',
    (SELECT id FROM categories WHERE name = 'Science'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/bulletproof_memory.pdf',
    'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=400&q=80',
    'RESTRICTED',
    192,
    3700000,
    410,
    2200,
    FALSE,
    1
),
-- UI Section: New Books Row
(
    'Mossad: The Greatest Missions of the Israeli Secret Service',
    'Michael Bar-Zohar',
    'Declassified narratives of high-stakes intelligence and clandestine operations.',
    (SELECT id FROM categories WHERE name = 'Others'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/mossad.pdf',
    'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    400,
    6700000,
    780,
    3200,
    TRUE,
    1
),
(
    'Less: A Novel',
    'Andrew Sean Greer',
    'Winner of the Pulitzer Prize for Fiction. A brilliant comedy of American abroad.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/less_novel.pdf',
    'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    272,
    3900000,
    510,
    2100,
    FALSE,
    1
),
(
    'Vyasa: The Beginning',
    'Sibaji Bandyopadhyay',
    'An in-depth scholarly deconstruction of epic literary genealogy and ancient philosophy.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'Research Paper'),
    'library-pdfs/vyasa_beginning.pdf',
    'https://images.unsplash.com/photo-1463320726281-696a485928c7?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    340,
    5900000,
    320,
    1400,
    FALSE,
    1
),
(
    'Shoe Dog: A Memoir by the Creator of Nike',
    'Phil Knight',
    'The gripping story of building an athletic footwear empire from five borrowed dollars.',
    (SELECT id FROM categories WHERE name = 'Commerce'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/shoe_dog.pdf',
    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    400,
    5400000,
    3100,
    11500,
    TRUE,
    1
),
(
    'Touch the Sky',
    'Rakesh Sharma',
    'Memoirs of astronomical exploration, aeronautics, and human fortitude.',
    (SELECT id FROM categories WHERE name = 'Science'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/touch_the_sky.pdf',
    'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    230,
    4200000,
    430,
    1950,
    FALSE,
    1
),
(
    'The Perfect Murder',
    'Ruskin Bond',
    'Classic Indian gothic mystery short stories filled with subtle intrigue.',
    (SELECT id FROM categories WHERE name = 'Arts'),
    (SELECT id FROM book_types WHERE name = 'E-Book'),
    'library-pdfs/perfect_murder.pdf',
    'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=400&q=80',
    'AVAILABLE',
    180,
    2900000,
    620,
    2700,
    FALSE,
    1
);

-- ----------------------------------------------------------------------------
-- 5. SEED USER PERMISSIONS (Baseline defaults)
-- ----------------------------------------------------------------------------
INSERT INTO user_permissions (user_id, can_use_app, can_read_books, can_download_books, can_request_books)
VALUES
(1, TRUE, TRUE, TRUE, TRUE), -- Admin
(2, TRUE, TRUE, TRUE, TRUE), -- Abhishek Saha
(3, TRUE, TRUE, TRUE, TRUE), -- Sarah Jenkins
(4, TRUE, TRUE, FALSE, TRUE), -- Marcus Vance (No downloads baseline)
(5, FALSE, FALSE, FALSE, FALSE); -- John Suspended (Account revoked)

-- ----------------------------------------------------------------------------
-- 6. SEED USER RESTRICTIONS (Temporary Sanctions)
-- ----------------------------------------------------------------------------
INSERT INTO user_restrictions (user_id, restriction_type, reason, starts_at, expires_at, is_active, created_by)
VALUES
(
    4,
    'BLOCK_DOWNLOADING',
    'Policy violation: multiple concurrent file transfers detected within 5 minutes.',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    TRUE,
    1
);

-- ----------------------------------------------------------------------------
-- 7. SEED BOOK PERMISSIONS (Granular Overrides / Special Grants)
-- ----------------------------------------------------------------------------
-- Give Abhishek Saha special permission to read the restricted book 'Murder on the Orient Express'
INSERT INTO book_permissions (user_id, book_id, can_read, can_download, starts_at, expires_at, created_by)
VALUES
(
    2,
    (SELECT id FROM books WHERE title = 'Murder on the Orient Express'),
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    1
);

-- ----------------------------------------------------------------------------
-- 8. SEED READING HISTORY (Powers "Previous Reading" UI Carousel)
-- ----------------------------------------------------------------------------
INSERT INTO reading_history (user_id, book_id, current_page, progress, completed, started_at, last_read_at)
VALUES
(
    2,
    (SELECT id FROM books WHERE title = 'Steal Like an Artist'),
    120,
    75.00,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    2,
    (SELECT id FROM books WHERE title = 'Type Matters!'),
    67,
    42.00,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    2,
    (SELECT id FROM books WHERE title = 'How to Become a Multi-Millionaire'),
    36,
    15.00,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    2,
    (SELECT id FROM books WHERE title = 'Show Your Work!'),
    224,
    100.00,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '14 days',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    2,
    (SELECT id FROM books WHERE title = 'The Complete Novels of Sherlock Holmes'),
    326,
    30.00,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
);

-- ----------------------------------------------------------------------------
-- 9. SEED DOWNLOADS
-- ----------------------------------------------------------------------------
INSERT INTO downloads (user_id, book_id, downloaded_at, ip_address)
VALUES
(
    2,
    (SELECT id FROM books WHERE title = 'Steal Like an Artist'),
    CURRENT_TIMESTAMP - INTERVAL '6 days',
    '192.168.1.102'
),
(
    2,
    (SELECT id FROM books WHERE title = 'Show Your Work!'),
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    '192.168.1.102'
),
(
    3,
    (SELECT id FROM books WHERE title = 'The Subtle Art of Not Giving a F*ck'),
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    '192.168.1.115'
);

-- ----------------------------------------------------------------------------
-- 10. SEED BOOK REQUESTS
-- ----------------------------------------------------------------------------
INSERT INTO book_requests (user_id, title, author, description, status, admin_response, reviewed_by, reviewed_at)
VALUES
(
    2,
    'Designing Data-Intensive Applications',
    'Martin Kleppmann',
    'Required for our upcoming distributed database engineering course.',
    'APPROVED',
    'Procurement approved. We are acquiring the academic e-license this week.',
    1,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    3,
    'Clean Code: A Handbook of Agile Software Craftsmanship',
    'Robert C. Martin',
    'Would like to read the official edition for object-oriented principles class.',
    'PENDING',
    NULL,
    NULL,
    NULL
);

-- ----------------------------------------------------------------------------
-- 11. SEED ACCESS REQUESTS
-- ----------------------------------------------------------------------------
INSERT INTO access_requests (user_id, book_id, reason, status, admin_response, reviewed_by, reviewed_at)
VALUES
(
    2,
    (SELECT id FROM books WHERE title = 'Murder on the Orient Express'),
    'Conducting comparative study on golden-age British detective fiction for thesis.',
    'APPROVED',
    'Granted 30-day full access privilege for academic research.',
    1,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    3,
    (SELECT id FROM books WHERE title = 'Bulletproof Memory'),
    'Preparing for university competitive exam memory technique requirements.',
    'PENDING',
    NULL,
    NULL,
    NULL
);

-- ----------------------------------------------------------------------------
-- 12. SEED ACTIVITY LOGS
-- ----------------------------------------------------------------------------
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, metadata, ip_address, created_at)
VALUES
(
    1,
    'USER_RESTRICTION_APPLIED',
    'USER',
    4,
    '{"restriction_type": "BLOCK_DOWNLOADING", "duration_days": 14}',
    '127.0.0.1',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    1,
    'ACCESS_REQUEST_APPROVED',
    'ACCESS_REQUEST',
    1,
    '{"book_id": 9, "user_id": 2, "validity_days": 30}',
    '127.0.0.1',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    2,
    'BOOK_DOWNLOADED',
    'BOOK',
    1,
    '{"title": "Steal Like an Artist", "file_size_bytes": 4194304}',
    '192.168.1.102',
    CURRENT_TIMESTAMP - INTERVAL '6 days'
);

