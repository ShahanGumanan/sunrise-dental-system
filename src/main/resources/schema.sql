CREATE DATABASE IF NOT EXISTS sunrise_dental_db;
USE sunrise_dental_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'receptionist', 'dentist') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    date_of_birth DATE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dentists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    specialization VARCHAR(100),
    available_days VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS treatments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_fee DECIMAL(10,2) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    description TEXT,
    duration_minutes INT NOT NULL DEFAULT 30
);

CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(20) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    treatment_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('scheduled', 'completed', 'cancelled') NOT NULL DEFAULT 'scheduled',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (dentist_id) REFERENCES dentists(id),
    FOREIGN KEY (treatment_id) REFERENCES treatments(id),
    INDEX idx_appointments_dentist_slot (dentist_id, appointment_date, appointment_time, status)
);

CREATE TABLE IF NOT EXISTS bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL UNIQUE,
    receipt_number VARCHAR(20) NOT NULL UNIQUE,
    consultation_fee DECIMAL(10,2) NOT NULL,
    treatment_fee DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL,
    bill_type ENUM('standard', 'emergency', 'child') NOT NULL DEFAULT 'standard',
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
