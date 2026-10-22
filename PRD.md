# 📋 PRD.md — Product Requirements Document

## Smart Parking Management System

| Field               | Value                                          |
|---------------------|------------------------------------------------|
| **Document Type**   | Product Requirements Document (PRD)            |
| **Version**         | 1.0.0                                          |
| **Status**          | APPROVED FOR IMPLEMENTATION                    |
| **Target Platform** | Java SE Desktop (Swing + MySQL)                |

---

## Table of Contents

1. [Functional Requirements](#1-functional-requirements)
2. [Portal A — User Portal](#2-portal-a--user-portal)
3. [Portal B — Employee Portal](#3-portal-b--employee-portal)
4. [Fee Calculation Engine](#4-fee-calculation-engine)
5. [Parking Grid & Slot Model](#5-parking-grid--slot-model)
6. [Validation Rules & Edge Cases](#6-validation-rules--edge-cases)
7. [UI/UX Specifications](#7-uiux-specifications)
8. [Data Integrity Constraints](#8-data-integrity-constraints)
9. [Acceptance Criteria](#9-acceptance-criteria)

---

## 1. Functional Requirements

### 1.1 Requirements Summary Table

| Req ID  | Category      | Requirement                                                | Priority  |
|---------|---------------|------------------------------------------------------------|-----------|
| FR-001  | Auth          | User self-signup with auto-generated unique ID             | P0        |
| FR-002  | Auth          | User login via User ID + Password                          | P0        |
| FR-003  | Auth          | Employee login via Username + Password                     | P0        |
| FR-004  | Dashboard     | User sees only ACTIVE parking tickets                      | P0        |
| FR-005  | Dashboard     | User can register additional vehicles                      | P0        |
| FR-006  | Entry         | Employee searches user by User ID                          | P0        |
| FR-007  | Entry         | System shows customer name + vehicle dropdown              | P0        |
| FR-008  | Entry         | Auto-assign first available matching slot                  | P0        |
| FR-009  | Entry         | Manual-assign via floor → pillar grid selector             | P0        |
| FR-010  | Entry         | Generate parking ticket + mark slot occupied               | P0        |
| FR-011  | Entry         | Display entry receipt modal                                | P0        |
| FR-012  | Exit          | Employee searches user by User ID                          | P0        |
| FR-013  | Exit          | Auto-fetch most recent active ticket                       | P0        |
| FR-014  | Exit          | Calculate fee based on completed hourly blocks             | P0        |
| FR-015  | Exit          | Update ticket (exit_time, fee) + release slot              | P0        |
| FR-016  | Exit          | Display itemized exit receipt modal                        | P0        |
| FR-017  | Schema        | Full referential integrity via InnoDB foreign keys         | P0        |
| FR-018  | Seed          | Pre-seeded test data for instant demo readiness            | P0        |

---

## 2. Portal A — User Portal

### 2.1 Signup Flow

#### 2.1.1 Input Fields

| Field           | Widget           | Constraints                                      |
|-----------------|------------------|--------------------------------------------------|
| Name            | `JTextField`     | Non-empty, max 100 chars, alphabetic + spaces    |
| Password        | `JPasswordField` | Non-empty, min 4 chars                           |
| Vehicle Number  | `JTextField`     | Non-empty, alphanumeric + hyphens, unique in DB  |
| Vehicle Type    | `JComboBox`      | Enum: `2W`, `4W`                                 |

#### 2.1.2 User ID Generation Algorithm

```
PREFIX  = "USR-"
RANDOM  = 4-digit random integer in range [1000, 9999]
USER_ID = PREFIX + RANDOM
```

- **Collision Handling:** Before inserting, query `SELECT COUNT(*) FROM Users WHERE user_id = ?`. If collision detected, regenerate. Retry up to 10 times, then fail with an error dialog.
- **Display:** On successful insert, show a `JOptionPane.showMessageDialog` modal:
  ```
  ╔════════════════════════════════════════╗
  ║   ✅ Registration Successful!          ║
  ║                                        ║
  ║   Your User ID: USR-8492              ║
  ║                                        ║
  ║   ⚠ Save this ID — you need it to    ║
  ║     log in and at the parking gate.   ║
  ╚════════════════════════════════════════╝
  ```
  Use `<html><b>USR-8492</b></html>` for bold formatting inside the dialog message.

#### 2.1.3 Database Operations (Signup)

1. `INSERT INTO Users (user_id, name, password) VALUES (?, ?, ?)`
2. `INSERT INTO Vehicles (vehicle_number, user_id, type) VALUES (?, ?, ?)`
3. Both inserts should be wrapped in a **single transaction** — rollback on failure.

---

### 2.2 Login Flow

| Field    | Widget           | Validation                            |
|----------|------------------|---------------------------------------|
| User ID  | `JTextField`     | Non-empty, must match `USR-XXXX` pattern |
| Password | `JPasswordField` | Non-empty                             |

- **Auth Query:** `SELECT * FROM Users WHERE user_id = ? AND password = ?`
- **Failure:** Show `JOptionPane.showMessageDialog` error: `"Invalid User ID or Password."`
- **Success:** Transition to User Dashboard panel via `CardLayout.show()`.

---

### 2.3 User Dashboard

#### 2.3.1 Active Tickets Table

Display a `JTable` with the following columns:

| Column          | Source                                         |
|-----------------|------------------------------------------------|
| Ticket ID       | `ParkingTickets.ticket_id`                     |
| Vehicle Number  | `ParkingTickets.vehicle_number`                |
| Vehicle Type    | `Vehicles.type` (joined)                       |
| Floor           | `Floors.floor_number` (joined via slot)        |
| Pillar Section  | `ParkingSlots.pillar_section` (joined)         |
| Entry Time      | `ParkingTickets.entry_time`                    |

- **Filter Condition:** `WHERE v.user_id = ? AND pt.exit_time IS NULL`
- **Refresh:** Table data reloads every time the dashboard panel is shown.
- **Empty State:** Display a centered label: `"No active parking tickets."`

#### 2.3.2 Add Vehicle Feature

- Button labeled `"+ Add Vehicle"` opens a `JDialog`.
- Input fields: Vehicle Number (`JTextField`), Vehicle Type (`JComboBox`: 2W/4W).
- **Validation:**
  - Vehicle number must be non-empty and unique in `Vehicles` table.
  - Duplicate vehicle number → error dialog: `"This vehicle is already registered."`
- **Insert:** `INSERT INTO Vehicles (vehicle_number, user_id, type) VALUES (?, ?, ?)`
- On success, close dialog and (optionally) show a brief confirmation.

---

## 3. Portal B — Employee Portal

### 3.1 Employee Login

| Field    | Widget           | Validation     |
|----------|------------------|----------------|
| Username | `JTextField`     | Non-empty      |
| Password | `JPasswordField` | Non-empty      |

- **Auth Query:** `SELECT * FROM Employees WHERE username = ? AND password = ?`
- **Failure:** Error dialog: `"Invalid credentials."`
- **Success:** Transition to Employee Dashboard (`JTabbedPane` with Entry Gate and Exit Gate tabs).

### 3.2 Pre-Seeded Employee Accounts

| Username   | Password    | Role      |
|------------|-------------|-----------|
| `manager`  | `admin123`  | Manager   |
| `attendant`| `park456`   | Attendant |

> [!IMPORTANT]
> These accounts are inserted via `schema.sql` seed data. No employee registration UI exists.

---

### 3.3 Entry Gate Tab — Detailed Workflow

#### Step 1: User Search

- **Widget:** `JTextField` with a `"Search"` `JButton`.
- **Query:** `SELECT user_id, name FROM Users WHERE user_id = ?`
- **Not Found:** Error dialog: `"User ID not found."`
- **Found:** Populate the Customer Summary area and proceed to Step 2.

#### Step 2: Customer Summary & Vehicle Selection

- **Display Area:** A `JLabel` or `JPanel` showing:
  ```
  Customer: Aryan Kumar (USR-3847)
  ```
- **Vehicle Dropdown:** A `JComboBox` populated by:
  ```sql
  SELECT vehicle_number, type FROM Vehicles WHERE user_id = ?
  ```
  Format: `"KA-01-AB-1234 (4W)"` per item.

#### Step 3: Allocation Mode Selection

- **Toggle:** Two `JRadioButton` options grouped in a `ButtonGroup`:
  - `◉ Auto-Assign` (default selected)
  - `○ Manual-Assign`

##### 3a. Auto-Assign Logic

```sql
SELECT slot_id, floor_id, pillar_section
FROM ParkingSlots
WHERE slot_type = ? AND is_occupied = FALSE
ORDER BY floor_id ASC, slot_id ASC
LIMIT 1
```

- If no slot available → error dialog: `"No available slots for this vehicle type."`
- If found → proceed directly to Step 4.

##### 3b. Manual-Assign — Grid Selector UI

**Floor Selector:**
- `JComboBox` populated from `Floors` table.

**Pillar Grid Panel:**
- A `JPanel` using `GridLayout` displaying pillar-section buttons.
- Layout per floor (4 rows × 4 pillar sections = 16 buttons):

  | Row \ Col Pairs | 1-2     | 3-4     | 5-6     | 7-8     |
  |-----------------|---------|---------|---------|---------|
  | **A**           | A1-A2   | A3-A4   | A5-A6   | A7-A8   |
  | **B**           | B1-B2   | B3-B4   | B5-B6   | B7-B8   |
  | **C**           | C1-C2   | C3-C4   | C5-C6   | C7-C8   |
  | **D**           | D1-D2   | D3-D4   | D5-D6   | D7-D8   |

- **Button Color Coding:**
  - 🟢 Green: Has at least 1 available slot of the required type.
  - 🔴 Red: All 3 slots in this section are occupied (for this type).
  - Clicking a green button selects that pillar section. The system picks the first free slot within it.

- **Availability Query (per pillar section):**
  ```sql
  SELECT COUNT(*) FROM ParkingSlots
  WHERE floor_id = ? AND pillar_section = ? AND slot_type = ? AND is_occupied = FALSE
  ```

#### Step 4: Approval & Ticket Generation

**Pre-Validation Checks:**
1. A vehicle is selected from the dropdown.
2. The selected vehicle does NOT already have an active ticket:
   ```sql
   SELECT COUNT(*) FROM ParkingTickets
   WHERE vehicle_number = ? AND exit_time IS NULL
   ```
   If active ticket exists → error: `"This vehicle already has an active parking session."`
3. The target slot is still available (`is_occupied = FALSE`).

**Database Operations (in a single transaction):**
1. `INSERT INTO ParkingTickets (vehicle_number, slot_id, employee_id, entry_time) VALUES (?, ?, ?, NOW())`
2. `UPDATE ParkingSlots SET is_occupied = TRUE WHERE slot_id = ?`

**Entry Receipt Modal (`JDialog`):**
```
╔══════════════════════════════════════════╗
║        🅿 PARKING ENTRY RECEIPT          ║
║──────────────────────────────────────────║
║  Ticket ID    : 1042                     ║
║  Customer     : Aryan Kumar              ║
║  Vehicle      : KA-01-AB-1234 (4W)      ║
║  Floor        : Floor 1                  ║
║  Pillar       : A3-A4                    ║
║  Slot ID      : 15                       ║
║  Entry Time   : 2026-10-22 09:15:00      ║
║  Employee     : attendant                ║
║──────────────────────────────────────────║
║            [  OK  ]                      ║
╚══════════════════════════════════════════╝
```

---

### 3.4 Exit Gate Tab — Detailed Workflow

#### Step 1: User Search

- Identical to Entry Gate Step 1.

#### Step 2: Active Ticket Retrieval

```sql
SELECT pt.*, v.type AS vehicle_type, ps.pillar_section, ps.floor_id, f.floor_number
FROM ParkingTickets pt
JOIN Vehicles v ON pt.vehicle_number = v.vehicle_number
JOIN ParkingSlots ps ON pt.slot_id = ps.slot_id
JOIN Floors f ON ps.floor_id = f.floor_id
WHERE v.user_id = ? AND pt.exit_time IS NULL
ORDER BY pt.entry_time DESC
LIMIT 1
```

- **No Active Ticket:** Info dialog: `"No active parking session found for this user."`
- **Found:** Display ticket summary and proceed.

#### Step 3: Fee Calculation Display

**Ticket Summary Panel:**

| Field          | Value                          |
|----------------|--------------------------------|
| Ticket ID      | 1042                           |
| Vehicle        | KA-01-AB-1234 (4W)            |
| Entry Time     | 2026-10-22 09:15:00            |
| Current Time   | 2026-10-22 12:40:00            |
| Duration       | 3 hrs 25 mins                  |
| Completed Hrs  | 4                              |
| Rate           | ₹80/hr                        |
| **Total Fee**  | **₹320**                      |

#### Step 4: Confirm Exit

- `"Confirm Exit & Payment"` button.

**Database Operations (in a single transaction):**
1. `UPDATE ParkingTickets SET exit_time = NOW(), total_fee = ? WHERE ticket_id = ?`
2. `UPDATE ParkingSlots SET is_occupied = FALSE WHERE slot_id = ?`

**Exit Receipt Modal (`JDialog`):**
```
╔══════════════════════════════════════════╗
║        🅿 PARKING EXIT RECEIPT           ║
║──────────────────────────────────────────║
║  Ticket ID    : 1042                     ║
║  Customer     : Aryan Kumar              ║
║  Vehicle      : KA-01-AB-1234 (4W)      ║
║  Floor        : Floor 1                  ║
║  Pillar       : A3-A4                    ║
║  Entry Time   : 2026-10-22 09:15:00      ║
║  Exit Time    : 2026-10-22 12:40:00      ║
║──────────────────────────────────────────║
║  Duration     : 3 hrs 25 mins            ║
║  Completed Hrs: 4 blocks                 ║
║  Rate         : ₹80/hr (4W)             ║
║  TOTAL FEE    : ₹320.00                 ║
║──────────────────────────────────────────║
║  Status       : PAYMENT SETTLED          ║
║            [  OK  ]                      ║
╚══════════════════════════════════════════╝
```

---

## 4. Fee Calculation Engine

### 4.1 Rate Card

| Vehicle Type | Code | Rate per Completed Hour |
|-------------|------|------------------------|
| Two-Wheeler  | `2W` | ₹40                   |
| Four-Wheeler | `4W` | ₹80                   |

### 4.2 Billing Algorithm (Pseudocode)

```
FUNCTION calculateFee(entryTime, exitTime, vehicleType):
    totalMinutes = differenceInMinutes(entryTime, exitTime)

    IF totalMinutes < 60:
        completedHours = 0
    ELSE:
        completedHours = CEIL(totalMinutes / 60.0)
    END IF

    rate = (vehicleType == "2W") ? 40 : 80
    totalFee = completedHours * rate

    RETURN totalFee
END FUNCTION
```

### 4.3 Billing Truth Table

| Minutes | `< 60?` | `ceil(min/60)` | Completed Hours | 2W Fee | 4W Fee |
|---------|---------|----------------|-----------------|--------|--------|
| 0       | Yes     | —              | 0               | ₹0     | ₹0     |
| 30      | Yes     | —              | 0               | ₹0     | ₹0     |
| 59      | Yes     | —              | 0               | ₹0     | ₹0     |
| 60      | No      | 1              | 1               | ₹40    | ₹80    |
| 61      | No      | 2              | 2               | ₹80    | ₹160   |
| 90      | No      | 2              | 2               | ₹80    | ₹160   |
| 120     | No      | 2              | 2               | ₹80    | ₹160   |
| 121     | No      | 3              | 3               | ₹120   | ₹240   |
| 180     | No      | 3              | 3               | ₹120   | ₹240   |
| 181     | No      | 4              | 4               | ₹160   | ₹320   |

> [!NOTE]
> The spec states "59 min = ₹0" and "61 min = 2 completed blocks." This is consistent with: if `totalMinutes < 60` → 0 hours, else `ceil(totalMinutes / 60)` hours. At exactly 60 min, `ceil(60/60) = 1`, which is 1 completed hour — also consistent.

---

## 5. Parking Grid & Slot Model

### 5.1 Physical Layout

| Dimension     | Value                                        |
|---------------|----------------------------------------------|
| Rows per Floor| 4 (A, B, C, D)                               |
| Column Pairs  | 4 (1-2, 3-4, 5-6, 7-8)                      |
| Pillar Sections per Floor | 16 (4 rows × 4 column-pairs)      |
| Slots per Pillar Section  | 3                                  |
| **Total Slots per Floor** | **48**                             |

### 5.2 Pillar Section Naming Convention

Format: `{Row}{Col1}-{Row}{Col2}`

Examples: `A1-A2`, `A3-A4`, `B5-B6`, `D7-D8`

### 5.3 Slot Type Assignment Strategy

Slot types are **not** tied to a fixed row. Each floor gets its own randomised layout, and a few
pillar sections are deliberately **mixed** so the manual allocation grid shows varied availability.

| Pillar Section Pattern | Slots                        | Rationale                              |
|------------------------|------------------------------|----------------------------------------|
| Uniform `4W` section   | 3 × `4W`                     | Wider spaces for cars                  |
| Uniform `2W` section   | 3 × `2W`                     | Narrower spaces for bikes              |
| Mixed section (2 + 1)  | 2 × `4W` + 1 × `2W`          | Section shared between both vehicle types |
| Mixed section (1 + 2)  | 1 × `4W` + 2 × `2W`          | Section shared between both vehicle types |

Per floor the seed data contains **7 uniform `4W` sections, 7 uniform `2W` sections, 1 section with
2 × `4W` + 1 × `2W` and 1 section with 1 × `4W` + 2 × `2W`**.

> [!TIP]
> This distribution gives 24 four-wheeler slots and 24 two-wheeler slots per floor — a balanced
> 50/50 split — while the mixed sections make the allocation grid interesting to demo.

### 5.4 Number of Floors

The seed data provides **3 floors** (`Floor 1`, `Floor 2`, `Floor 3`), each with 48 slots
(4 rows × 4 pillar sections × 3 slots). Total: **144 slots**, 72 × `4W` and 72 × `2W`.

---

## 6. Validation Rules & Edge Cases

### 6.1 Input Validation Matrix

| Screen          | Field            | Rule                                              | Error Message                                    |
|----------------|------------------|----------------------------------------------------|--------------------------------------------------|
| User Signup    | Name             | Non-empty, alpha + spaces, ≤ 100 chars            | `"Name is required and must contain only letters."` |
| User Signup    | Password         | Non-empty, ≥ 4 chars                              | `"Password must be at least 4 characters."`      |
| User Signup    | Vehicle Number   | Non-empty, unique in DB                            | `"Vehicle number already registered."`           |
| User Login     | User ID          | Non-empty, matches `USR-\d{4}` pattern            | `"Invalid User ID format."`                      |
| User Login     | Password         | Non-empty                                          | `"Password is required."`                        |
| Employee Login | Username         | Non-empty                                          | `"Username is required."`                        |
| Employee Login | Password         | Non-empty                                          | `"Password is required."`                        |
| Entry Gate     | User ID search   | Must exist in Users table                          | `"User ID not found."`                           |
| Entry Gate     | Vehicle selection| Must select from dropdown                          | `"Please select a vehicle."`                     |
| Entry Gate     | Duplicate entry  | Vehicle must not have active ticket                | `"This vehicle already has an active session."`  |
| Entry Gate     | Slot availability| Selected slot must be unoccupied                   | `"Selected slot is no longer available."`        |
| Exit Gate      | User ID search   | Must exist in Users table                          | `"User ID not found."`                           |
| Exit Gate      | Active ticket    | User must have at least one active ticket          | `"No active parking session found."`             |

### 6.2 Edge Case Handling

| # | Edge Case                                              | Expected Behavior                                                   |
|---|--------------------------------------------------------|---------------------------------------------------------------------|
| 1 | User ID collision during signup                        | Regenerate random ID, retry up to 10 times                          |
| 2 | User has no vehicles                                    | Should not occur (initial vehicle required at signup), but dropdown shows empty + error if attempted |
| 3 | All slots of a type are full (auto-assign)             | Display "No available slots" error dialog                           |
| 4 | All slots in a pillar section are full (manual)        | Button is red/disabled; clicking shows error or is non-interactive  |
| 5 | Vehicle already parked (duplicate entry)               | Block entry with error dialog                                       |
| 6 | User has no active ticket (exit attempt)               | Display info dialog: "No active session found"                      |
| 7 | Parking duration < 60 minutes                          | Fee = ₹0                                                           |
| 8 | Parking duration = exactly 60 minutes                  | Fee = 1 × rate                                                      |
| 9 | Concurrent modification (slot taken between select & confirm) | Re-check `is_occupied` inside transaction before insert        |
| 10| Employee enters non-existent User ID at entry or exit  | Error dialog: "User ID not found"                                   |
| 11| User tries to add a vehicle with an already-registered plate | Error dialog with duplicate message                           |
| 12| Database connection failure                            | Show error dialog: "Database connection failed. Please restart."    |

---

## 7. UI/UX Specifications

### 7.1 Window Configuration

| Property        | Value                     |
|-----------------|---------------------------|
| Title           | `"Smart Parking Management System"` |
| Default Size    | 1000 × 700 pixels         |
| Resizable       | Yes                       |
| Close Operation | `EXIT_ON_CLOSE`           |
| Look and Feel   | System default (or Nimbus) |

### 7.2 Navigation Pattern

- **Root Frame:** Single `JFrame` using a `CardLayout` as the main content switcher.
- **Cards:**
  1. Portal Selector
  2. User Auth Panel (internal `JTabbedPane`: Signup / Login)
  3. User Dashboard
  4. Employee Login
  5. Employee Dashboard (internal `JTabbedPane`: Entry Gate / Exit Gate)
- **Back Navigation:** Each sub-panel includes a `"← Back"` or `"Logout"` button to return to the Portal Selector.

### 7.3 Typography & Formatting

- Use `<html>` tags in `JLabel` text for:
  - Bold headings: `<html><b>Customer:</b> Aryan Kumar</html>`
  - Multi-line content: `<html>Line 1<br/>Line 2</html>`
  - Colored text: `<html><font color='green'>Available</font></html>`
- Receipt modals use monospaced-style formatting for alignment.

### 7.4 Color Scheme (Teal + Amber)

> [!IMPORTANT]
> All colours live in `com.parking.ui.Theme`. The slot-grid buttons paint themselves so the
> Windows look-and-feel can never wash the fill out.

| Element                     | Color        | Hex       |
|-----------------------------|-------------|-----------|
| Primary Action Buttons       | Teal        | `#137E7E` |
| Primary Button (hover)       | Deep Teal   | `#0F6B6B` |
| Headings / Titles            | Dark Teal   | `#0F4C5C` |
| Success / Available slot     | Mint fill   | `#CFEFD9` (text `#0B5D2A`) |
| Error / Full slot            | Rose fill   | `#F8D7D6` (text `#8A1C1C`) |
| Selected pillar section      | Amber ring  | `#F2A900` |
| Secondary Buttons            | White       | `#FFFFFF` (text `#0F4C5C`) |
| Panel Backgrounds            | Off-white   | `#F6FAFA` |
| Card / Form Backgrounds      | White       | `#FFFFFF` |
| Table Header                 | Dark Teal   | `#0F4C5C` (text `#FFFFFF`) |
| Text Primary                 | Charcoal    | `#1E2B2C` |
| Text Muted                   | Slate       | `#5C6B6C` |
| Borders                      | Pale Slate  | `#CFDDDD` |

### 7.5 Readability Rules

- Never rely on the look-and-feel to paint a background — set an explicit renderer
  (see the table header renderer in `UserDashboardPanel`).
- Slot grid text always uses a dark colour on a light fill.
- The window auto-sizes to the usable screen area, dividing the physical screen size by the
  display scale so 125% / 150% Windows scaling does not push content off-screen.

---

## 8. Data Integrity Constraints

### 8.1 Database-Level Enforcement

| Constraint                     | Table             | Type        | Details                                        |
|-------------------------------|-------------------|-------------|------------------------------------------------|
| User ID uniqueness            | Users             | PRIMARY KEY | `user_id VARCHAR(10)`                          |
| Vehicle uniqueness            | Vehicles          | PRIMARY KEY | `vehicle_number VARCHAR(20)`                   |
| Vehicle→User relationship     | Vehicles          | FOREIGN KEY | `user_id` → `Users.user_id`                   |
| Ticket→Vehicle relationship   | ParkingTickets    | FOREIGN KEY | `vehicle_number` → `Vehicles.vehicle_number`  |
| Ticket→Slot relationship      | ParkingTickets    | FOREIGN KEY | `slot_id` → `ParkingSlots.slot_id`            |
| Ticket→Employee relationship  | ParkingTickets    | FOREIGN KEY | `employee_id` → `Employees.employee_id`       |
| Slot→Floor relationship       | ParkingSlots      | FOREIGN KEY | `floor_id` → `Floors.floor_id`                |
| Vehicle type domain           | Vehicles          | ENUM        | Only `'2W'` or `'4W'`                         |
| Slot type domain              | ParkingSlots      | ENUM        | Only `'2W'` or `'4W'`                         |
| Slot occupied flag            | ParkingSlots      | BOOLEAN     | `DEFAULT FALSE`                                |
| Exit time nullable            | ParkingTickets    | NULLABLE    | `NULL` = active ticket                         |
| Fee nullable                  | ParkingTickets    | NULLABLE    | `NULL` until exit processed                    |

### 8.2 Application-Level Enforcement

- All multi-table writes (signup, entry, exit) must use **explicit JDBC transactions** (`connection.setAutoCommit(false)` → commit/rollback).
- Slot availability must be re-verified **inside** the transaction before ticket insertion.
- Prepared statements (`PreparedStatement`) must be used exclusively — no string concatenation for SQL.

---

## 9. Acceptance Criteria

### 9.1 Demo Scenario Script

| Step | Action                                              | Expected Result                                      |
|------|-----------------------------------------------------|------------------------------------------------------|
| 1    | Launch app                                          | Portal Selector appears (User / Employee buttons)    |
| 2    | Click "User Portal" → Signup tab                   | Signup form displayed                                |
| 3    | Enter name, password, vehicle → Submit              | Modal shows generated USR-XXXX                       |
| 4    | Login with generated USR-XXXX + password            | User Dashboard loads; empty active tickets table     |
| 5    | Add a second vehicle                                | Vehicle appears in dropdown upon next entry gate use |
| 6    | Logout → Click "Employee Portal"                    | Employee login form displayed                        |
| 7    | Login as `attendant` / `park456`                    | Employee Dashboard with Entry & Exit tabs            |
| 8    | Entry Tab → Search USR-XXXX                         | Customer name shown, vehicle dropdown populated      |
| 9    | Select vehicle → Auto-Assign → Confirm              | Receipt modal with slot details; slot marked occupied|
| 10   | Switch to Exit Tab → Search USR-XXXX                | Active ticket found and displayed                    |
| 11   | Click Confirm Exit                                  | Fee calculated; exit receipt shown; slot released     |
| 12   | Logout → User Portal → Login as USR-XXXX            | Active tickets table is now empty (ticket completed) |

### 9.2 Critical Pass/Fail Criteria

- [ ] All foreign key constraints enforced (test with invalid FK inserts).
- [ ] Duplicate vehicle entry blocked at application AND database level.
- [ ] Fee = ₹0 for sub-60-minute parking.
- [ ] Fee correctly uses `ceil()` for > 60 minutes.
- [ ] Slot `is_occupied` toggles correctly on entry and exit.
- [ ] No SQL injection possible (all queries use `PreparedStatement`).
- [ ] Application handles DB connection failure gracefully.

---

*Document Version: 1.0.0 · Last Updated: 2026-10-22 · Author: Architecture Agent*
