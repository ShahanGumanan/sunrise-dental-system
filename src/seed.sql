USE sunrise_dental_db;

-- The hash below is BCrypt for the demo password "password".
INSERT INTO users (username, password_hash, role, full_name)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin', 'Clinic Administrator'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password_hash, role, full_name)
SELECT 'receptionist', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'receptionist', 'Front Desk Receptionist'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'receptionist');

INSERT INTO users (username, password_hash, role, full_name)
SELECT 'dentist', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'dentist', 'Clinic Dentist'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'dentist');

INSERT INTO dentists (user_id, specialization, available_days)
SELECT id, 'General Dentistry', 'Monday, Wednesday, Friday'
FROM users u
WHERE u.username = 'dentist'
  AND NOT EXISTS (SELECT 1 FROM dentists d WHERE d.user_id = u.id);

INSERT INTO treatments (name, base_fee, consultation_fee, description, duration_minutes)
SELECT 'Routine Cleaning', 1000.00, 500.00, 'Standard dental cleaning', 30
WHERE NOT EXISTS (SELECT 1 FROM treatments WHERE name = 'Routine Cleaning');

INSERT INTO treatments (name, base_fee, consultation_fee, description, duration_minutes)
SELECT 'Dental Filling', 2500.00, 500.00, 'Composite filling treatment', 60
WHERE NOT EXISTS (SELECT 1 FROM treatments WHERE name = 'Dental Filling');

INSERT INTO treatments (name, base_fee, consultation_fee, description, duration_minutes)
SELECT 'Tooth Extraction', 3500.00, 500.00, 'Routine extraction procedure', 45
WHERE NOT EXISTS (SELECT 1 FROM treatments WHERE name = 'Tooth Extraction');
