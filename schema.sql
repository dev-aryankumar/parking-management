-- ============================================================================
-- Smart Parking Management System — Database Schema & Seed Data
-- Engine: MySQL 8.x / InnoDB with enforced Foreign Key Constraints
-- Version: 1.1.0
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. DATABASE SETUP
-- ----------------------------------------------------------------------------
DROP DATABASE IF EXISTS smart_parking;
CREATE DATABASE smart_parking
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_parking;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE Users (
    user_id     VARCHAR(10)     NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    password    VARCHAR(100)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. VEHICLES TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE Vehicles (
    vehicle_number  VARCHAR(20)             NOT NULL,
    user_id         VARCHAR(10)             NOT NULL,
    type            ENUM('2W', '4W')        NOT NULL,
    registered_at   TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vehicle_number),
    INDEX idx_vehicles_user (user_id),
    CONSTRAINT fk_vehicles_user
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. EMPLOYEES TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE Employees (
    employee_id INT             NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(100)    NOT NULL,
    role        VARCHAR(30)     NOT NULL DEFAULT 'Attendant',
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. FLOORS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE Floors (
    floor_id        INT             NOT NULL AUTO_INCREMENT,
    floor_number    VARCHAR(20)     NOT NULL UNIQUE,
    PRIMARY KEY (floor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. PARKING SLOTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE ParkingSlots (
    slot_id         INT                     NOT NULL AUTO_INCREMENT,
    floor_id        INT                     NOT NULL,
    pillar_section  VARCHAR(10)             NOT NULL,
    slot_type       ENUM('2W', '4W')        NOT NULL,
    is_occupied     BOOLEAN                 NOT NULL DEFAULT FALSE,
    PRIMARY KEY (slot_id),
    INDEX idx_slots_floor (floor_id),
    INDEX idx_slots_availability (slot_type, is_occupied),
    INDEX idx_slots_pillar (floor_id, pillar_section, slot_type),
    CONSTRAINT fk_slots_floor
        FOREIGN KEY (floor_id) REFERENCES Floors(floor_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. PARKING TICKETS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE ParkingTickets (
    ticket_id       INT                 NOT NULL AUTO_INCREMENT,
    vehicle_number  VARCHAR(20)         NOT NULL,
    slot_id         INT                 NOT NULL,
    employee_id     INT                 NOT NULL,
    entry_time      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time       DATETIME            NULL DEFAULT NULL,
    total_fee       DECIMAL(10,2)       NULL DEFAULT NULL,
    PRIMARY KEY (ticket_id),
    INDEX idx_tickets_vehicle (vehicle_number),
    INDEX idx_tickets_active (vehicle_number, exit_time),
    INDEX idx_tickets_slot (slot_id),
    CONSTRAINT fk_tickets_vehicle
        FOREIGN KEY (vehicle_number) REFERENCES Vehicles(vehicle_number)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_slot
        FOREIGN KEY (slot_id) REFERENCES ParkingSlots(slot_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_employee
        FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- SEED DATA
-- ============================================================================

-- ----------------------------------------------------------------------------
-- S1. SEED EMPLOYEES (Hardcoded — no registration UI)
-- ----------------------------------------------------------------------------
INSERT INTO Employees (username, password, role) VALUES
    ('manager',   'admin123', 'Manager'),
    ('attendant', 'park456',  'Attendant');

-- ----------------------------------------------------------------------------
-- S2. SEED USERS (15 demo customers)
-- ----------------------------------------------------------------------------
INSERT INTO Users (user_id, name, password) VALUES
    ('USR-1001', 'Aryan Kumar',      'pass1234'),
    ('USR-2045', 'Priya Sharma',     'priya@99'),
    ('USR-3782', 'Rahul Verma',      'rahul123'),
    ('USR-4510', 'Sneha Patel',      'sneha456'),
    ('USR-5267', 'Amit Singh',       'amit2026'),
    ('USR-6104', 'Neha Kulkarni',    'neha2026'),
    ('USR-7321', 'Rohan Mehta',      'rohan789'),
    ('USR-8456', 'Divya Nair',       'divya321'),
    ('USR-9572', 'Arjun Rao',        'arjun2025'),
    ('USR-1093', 'Fatima Sheikh',    'fatima88'),
    ('USR-1187', 'Karan Malhotra',   'karan543'),
    ('USR-1294', 'Ishita Bose',      'ishita99'),
    ('USR-1358', 'Vikram Joshi',     'vikram111'),
    ('USR-1467', 'Sanjay Gupta',     'sanjay777'),
    ('USR-1573', 'Meera Iyer',       'meera456');

-- ----------------------------------------------------------------------------
-- S3. SEED VEHICLES (27 vehicles spread across the 15 users)
-- ----------------------------------------------------------------------------
INSERT INTO Vehicles (vehicle_number, user_id, type) VALUES
    -- Aryan Kumar
    ('KA-01-AB-1234', 'USR-1001', '4W'),
    ('KA-01-CD-5678', 'USR-1001', '2W'),
    ('KA-01-EF-9012', 'USR-1001', '4W'),
    -- Priya Sharma
    ('MH-02-EF-9012', 'USR-2045', '4W'),
    ('MH-02-GH-3456', 'USR-2045', '2W'),
    -- Rahul Verma
    ('DL-03-GH-3456', 'USR-3782', '2W'),
    ('DL-03-IJ-7890', 'USR-3782', '4W'),
    -- Sneha Patel
    ('GJ-04-KL-1122', 'USR-4510', '2W'),
    ('GJ-04-MN-7788', 'USR-4510', '2W'),
    -- Amit Singh
    ('UP-05-MN-3344', 'USR-5267', '4W'),
    ('UP-05-OP-5566', 'USR-5267', '2W'),
    ('UP-05-QR-1123', 'USR-5267', '4W'),
    -- Neha Kulkarni
    ('MH-12-XY-1122', 'USR-6104', '4W'),
    ('MH-12-ZA-3344', 'USR-6104', '2W'),
    -- Rohan Mehta
    ('GJ-05-QR-7788', 'USR-7321', '2W'),
    ('GJ-05-ST-9900', 'USR-7321', '4W'),
    -- Divya Nair
    ('KL-07-UV-4455', 'USR-8456', '4W'),
    -- Arjun Rao
    ('TN-09-WX-6677', 'USR-9572', '2W'),
    -- Fatima Sheikh
    ('TS-09-YZ-2233', 'USR-1093', '4W'),
    ('TS-09-AB-5566', 'USR-1093', '2W'),
    -- Karan Malhotra
    ('PB-11-CD-7788', 'USR-1187', '4W'),
    -- Ishita Bose
    ('WB-12-EF-9900', 'USR-1294', '2W'),
    -- Vikram Joshi
    ('RJ-14-GH-1234', 'USR-1358', '4W'),
    ('RJ-14-IJ-5678', 'USR-1358', '2W'),
    -- Sanjay Gupta
    ('UP-18-KL-2468', 'USR-1467', '4W'),
    -- Meera Iyer
    ('TN-19-MN-3690', 'USR-1573', '2W'),
    ('TN-19-OP-8021', 'USR-1573', '4W');

-- ----------------------------------------------------------------------------
-- S4. SEED FLOORS (3 floors)
-- ----------------------------------------------------------------------------
INSERT INTO Floors (floor_number) VALUES
    ('Floor 1'),
    ('Floor 2'),
    ('Floor 3');

-- ----------------------------------------------------------------------------
-- S5. SEED PARKING SLOTS
--
-- Every floor has the same physical shape:
--   4 rows (A-D) x 4 pillar sections (1-2, 3-4, 5-6, 7-8) x 3 slots = 48 slots
--
-- The vehicle type of a pillar section is NOT fixed to a row anymore. Each
-- floor has its own randomised layout, and a few sections are deliberately
-- MIXED (2 slots of one type + 1 slot of the other type) so the manual
-- allocation grid shows varied availability.
--
-- Every floor still ends up perfectly balanced: 24 x 4W and 24 x 2W.
-- ----------------------------------------------------------------------------

-- ── FLOOR 1 ─────────────────────────────────────────────────────────────────
-- Row A: 4W | 4W | 2W | 4W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (1, 'A1-A2', '4W'), (1, 'A1-A2', '4W'), (1, 'A1-A2', '4W'),
    (1, 'A3-A4', '4W'), (1, 'A3-A4', '4W'), (1, 'A3-A4', '4W'),
    (1, 'A5-A6', '2W'), (1, 'A5-A6', '2W'), (1, 'A5-A6', '2W'),
    (1, 'A7-A8', '4W'), (1, 'A7-A8', '4W'), (1, 'A7-A8', '4W');
-- Row B: 2W | 4W | 2W | 4W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (1, 'B1-B2', '2W'), (1, 'B1-B2', '2W'), (1, 'B1-B2', '2W'),
    (1, 'B3-B4', '4W'), (1, 'B3-B4', '4W'), (1, 'B3-B4', '4W'),
    (1, 'B5-B6', '2W'), (1, 'B5-B6', '2W'), (1, 'B5-B6', '2W'),
    (1, 'B7-B8', '4W'), (1, 'B7-B8', '4W'), (1, 'B7-B8', '4W');
-- Row C: 2W | 2W | 4W | MIXED (4W, 4W, 2W)
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (1, 'C1-C2', '2W'), (1, 'C1-C2', '2W'), (1, 'C1-C2', '2W'),
    (1, 'C3-C4', '2W'), (1, 'C3-C4', '2W'), (1, 'C3-C4', '2W'),
    (1, 'C5-C6', '4W'), (1, 'C5-C6', '4W'), (1, 'C5-C6', '4W'),
    (1, 'C7-C8', '4W'), (1, 'C7-C8', '4W'), (1, 'C7-C8', '2W');
-- Row D: 4W | MIXED (4W, 2W, 2W) | 2W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (1, 'D1-D2', '4W'), (1, 'D1-D2', '4W'), (1, 'D1-D2', '4W'),
    (1, 'D3-D4', '4W'), (1, 'D3-D4', '2W'), (1, 'D3-D4', '2W'),
    (1, 'D5-D6', '2W'), (1, 'D5-D6', '2W'), (1, 'D5-D6', '2W'),
    (1, 'D7-D8', '2W'), (1, 'D7-D8', '2W'), (1, 'D7-D8', '2W');

-- ── FLOOR 2 ─────────────────────────────────────────────────────────────────
-- Row A: 4W | 2W | 4W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (2, 'A1-A2', '4W'), (2, 'A1-A2', '4W'), (2, 'A1-A2', '4W'),
    (2, 'A3-A4', '2W'), (2, 'A3-A4', '2W'), (2, 'A3-A4', '2W'),
    (2, 'A5-A6', '4W'), (2, 'A5-A6', '4W'), (2, 'A5-A6', '4W'),
    (2, 'A7-A8', '2W'), (2, 'A7-A8', '2W'), (2, 'A7-A8', '2W');
-- Row B: 4W | 2W | MIXED (4W, 4W, 2W) | 4W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (2, 'B1-B2', '4W'), (2, 'B1-B2', '4W'), (2, 'B1-B2', '4W'),
    (2, 'B3-B4', '2W'), (2, 'B3-B4', '2W'), (2, 'B3-B4', '2W'),
    (2, 'B5-B6', '4W'), (2, 'B5-B6', '4W'), (2, 'B5-B6', '2W'),
    (2, 'B7-B8', '4W'), (2, 'B7-B8', '4W'), (2, 'B7-B8', '4W');
-- Row C: 2W | 4W | 4W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (2, 'C1-C2', '2W'), (2, 'C1-C2', '2W'), (2, 'C1-C2', '2W'),
    (2, 'C3-C4', '4W'), (2, 'C3-C4', '4W'), (2, 'C3-C4', '4W'),
    (2, 'C5-C6', '4W'), (2, 'C5-C6', '4W'), (2, 'C5-C6', '4W'),
    (2, 'C7-C8', '2W'), (2, 'C7-C8', '2W'), (2, 'C7-C8', '2W');
-- Row D: MIXED (4W, 2W, 2W) | 2W | 4W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (2, 'D1-D2', '4W'), (2, 'D1-D2', '2W'), (2, 'D1-D2', '2W'),
    (2, 'D3-D4', '2W'), (2, 'D3-D4', '2W'), (2, 'D3-D4', '2W'),
    (2, 'D5-D6', '4W'), (2, 'D5-D6', '4W'), (2, 'D5-D6', '4W'),
    (2, 'D7-D8', '2W'), (2, 'D7-D8', '2W'), (2, 'D7-D8', '2W');

-- ── FLOOR 3 ─────────────────────────────────────────────────────────────────
-- Row A: 2W | 4W | 4W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (3, 'A1-A2', '2W'), (3, 'A1-A2', '2W'), (3, 'A1-A2', '2W'),
    (3, 'A3-A4', '4W'), (3, 'A3-A4', '4W'), (3, 'A3-A4', '4W'),
    (3, 'A5-A6', '4W'), (3, 'A5-A6', '4W'), (3, 'A5-A6', '4W'),
    (3, 'A7-A8', '2W'), (3, 'A7-A8', '2W'), (3, 'A7-A8', '2W');
-- Row B: 4W | 2W | 4W | 2W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (3, 'B1-B2', '4W'), (3, 'B1-B2', '4W'), (3, 'B1-B2', '4W'),
    (3, 'B3-B4', '2W'), (3, 'B3-B4', '2W'), (3, 'B3-B4', '2W'),
    (3, 'B5-B6', '4W'), (3, 'B5-B6', '4W'), (3, 'B5-B6', '4W'),
    (3, 'B7-B8', '2W'), (3, 'B7-B8', '2W'), (3, 'B7-B8', '2W');
-- Row C: 2W | 2W | 4W | 4W
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (3, 'C1-C2', '2W'), (3, 'C1-C2', '2W'), (3, 'C1-C2', '2W'),
    (3, 'C3-C4', '2W'), (3, 'C3-C4', '2W'), (3, 'C3-C4', '2W'),
    (3, 'C5-C6', '4W'), (3, 'C5-C6', '4W'), (3, 'C5-C6', '4W'),
    (3, 'C7-C8', '4W'), (3, 'C7-C8', '4W'), (3, 'C7-C8', '4W');
-- Row D: 4W | MIXED (4W, 4W, 2W) | 2W | MIXED (4W, 2W, 2W)
INSERT INTO ParkingSlots (floor_id, pillar_section, slot_type) VALUES
    (3, 'D1-D2', '4W'), (3, 'D1-D2', '4W'), (3, 'D1-D2', '4W'),
    (3, 'D3-D4', '4W'), (3, 'D3-D4', '4W'), (3, 'D3-D4', '2W'),
    (3, 'D5-D6', '2W'), (3, 'D5-D6', '2W'), (3, 'D5-D6', '2W'),
    (3, 'D7-D8', '4W'), (3, 'D7-D8', '2W'), (3, 'D7-D8', '2W');

-- ============================================================================
-- VERIFICATION QUERIES (run manually to confirm seed data)
-- ============================================================================
-- SELECT COUNT(*) AS total_users FROM Users;              -- Expected: 15
-- SELECT COUNT(*) AS total_vehicles FROM Vehicles;        -- Expected: 27
-- SELECT COUNT(*) AS total_employees FROM Employees;      -- Expected: 2
-- SELECT COUNT(*) AS total_floors FROM Floors;            -- Expected: 3
-- SELECT COUNT(*) AS total_slots FROM ParkingSlots;       -- Expected: 144 (48 per floor x 3 floors)
-- SELECT slot_type, COUNT(*) FROM ParkingSlots GROUP BY slot_type;
--   Expected: 4W = 72, 2W = 72
-- SELECT floor_id, slot_type, COUNT(*) AS slots
--   FROM ParkingSlots GROUP BY floor_id, slot_type;
--   Expected: 24 x 4W and 24 x 2W on every floor
-- SELECT floor_id, pillar_section, COUNT(*) AS slots
--   FROM ParkingSlots GROUP BY floor_id, pillar_section;
--   Expected: 3 slots per pillar_section per floor
-- SELECT floor_id, pillar_section, slot_type, COUNT(*) AS slots
--   FROM ParkingSlots GROUP BY floor_id, pillar_section, slot_type;
--   Expected: mixed sections appear with counts 1 and 2
-- ============================================================================
