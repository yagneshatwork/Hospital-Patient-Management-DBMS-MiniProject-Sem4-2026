-- ============================================================
-- Hospital Patient Management System - Database Setup Script
-- ============================================================
-- Run this file in MySQL Workbench or MySQL CLI:
--   mysql -u root -p < hospital_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

-- ============================
-- TABLE: Patient
-- ============================
CREATE TABLE IF NOT EXISTS Patient (
    patient_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    age         INT,
    gender      VARCHAR(10),
    phone       VARCHAR(15),
    address     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- TABLE: Treatment
-- ============================
CREATE TABLE IF NOT EXISTS Treatment (
    treatment_id    INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    treatment_name  VARCHAR(100) NOT NULL,
    cost            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    treatment_date  DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id) ON DELETE CASCADE
);

-- ============================
-- TABLE: Billing
-- ============================
CREATE TABLE IF NOT EXISTS Billing (
    bill_id       INT AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT NOT NULL,
    total_amount  DECIMAL(10,2),
    bill_date     DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
);

-- ============================
-- TABLE: Audit
-- ============================
CREATE TABLE IF NOT EXISTS Audit (
    audit_id     INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT,
    name         VARCHAR(100),
    action_type  VARCHAR(10),
    action_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- STORED FUNCTION: get_total_treatment_cost
-- Returns total treatment cost for a given patient_id
-- ============================
DROP FUNCTION IF EXISTS get_total_treatment_cost;

DELIMITER $$

CREATE FUNCTION get_total_treatment_cost(p_id INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE total DECIMAL(10,2);

    SELECT SUM(cost)
    INTO   total
    FROM   Treatment
    WHERE  patient_id = p_id;

    RETURN IFNULL(total, 0.00);
END $$

DELIMITER ;

-- ============================
-- TRIGGERS: Audit Logging
-- ============================
DROP TRIGGER IF EXISTS before_patient_update;
DROP TRIGGER IF EXISTS before_patient_delete;

DELIMITER $$

-- Fires BEFORE every UPDATE on Patient → logs old record
CREATE TRIGGER before_patient_update
BEFORE UPDATE ON Patient
FOR EACH ROW
BEGIN
    INSERT INTO Audit (patient_id, name, action_type)
    VALUES (OLD.patient_id, OLD.name, 'UPDATE');
END $$

-- Fires BEFORE every DELETE on Patient → logs old record
CREATE TRIGGER before_patient_delete
BEFORE DELETE ON Patient
FOR EACH ROW
BEGIN
    INSERT INTO Audit (patient_id, name, action_type)
    VALUES (OLD.patient_id, OLD.name, 'DELETE');
END $$

DELIMITER ;

-- ============================
-- Sample Data (optional - comment out if not needed)
-- ============================
-- INSERT INTO Patient (name, age, gender, phone, address) VALUES
-- ('Aarav Sharma',   32, 'Male',   '9876543210', 'Mumbai, Maharashtra'),
-- ('Priya Mehta',    28, 'Female', '9123456789', 'Delhi, New Delhi'),
-- ('Rahul Verma',    45, 'Male',   '9988776655', 'Bangalore, Karnataka');
--
-- INSERT INTO Treatment (patient_id, treatment_name, cost, treatment_date) VALUES
-- (1, 'Blood Test',       500.00, CURDATE()),
-- (1, 'X-Ray',           1200.00, CURDATE()),
-- (2, 'ECG',              800.00, CURDATE()),
-- (3, 'MRI Scan',        5000.00, CURDATE());

SELECT 'Hospital Patient Management DB setup complete!' AS Status;
