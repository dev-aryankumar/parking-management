# Smart Parking Management System

A desktop parking management application built with **Java Swing**, **JDBC** and **MySQL**.
It has a user portal (signup, login, active tickets, add vehicle) and an employee portal
(entry gate with auto/manual slot allocation, exit gate with fee calculation).

## Requirements

- JDK 17 or newer (`JAVA_HOME` set, `javac` on PATH)
- MySQL Server 8.x running locally
- `lib/mysql-connector-j-26.10.0.jar`

## 1. Create the database (first time only)

Double-click **`setup-db.bat`** (or run it from a terminal) and enter your MySQL root password
when prompted.

It executes `schema.sql`, which drops and recreates the `smart_parking` database and loads
seed users, employees, vehicles and 144 parking slots.

## 2. Configure the password

The app reads the MySQL password from the `MYSQL_PASSWORD` environment variable. Set it once:

```bat
setx MYSQL_PASSWORD "your-mysql-root-password"
```

Open a **new** terminal afterwards so the variable is visible.

Optional overrides:

| Variable | Default |
|----------|---------|
| `MYSQL_PASSWORD` | *(empty)* |
| `PARKING_DB_USER` | `root` |
| `PARKING_DB_URL` | `jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true` |

## 3. Build and run

```bat
build.bat
run.bat
```

## Demo accounts

Employees (seeded):

| Username | Password | Role |
|----------|----------|------|
| `manager` | `admin123` | Manager |
| `attendant` | `park456` | Attendant |

Seeded users (15) — passwords are shown in `schema.sql`, e.g. `USR-1001` / `pass1234`,
`USR-2045` / `priya@99`, `USR-7321` / `rohan789`.

## Seed data summary

| Table | Rows |
|-------|------|
| `Users` | 15 |
| `Vehicles` | 27 |
| `Employees` | 2 |
| `Floors` | 3 |
| `ParkingSlots` | 144 (48 per floor) |
| `ParkingTickets` | 0 (created at runtime) |

Each floor has 4 rows (A–D) × 4 pillar sections × 3 slots. The slot type of a pillar section is
**randomised per floor**, and a few sections are **mixed** (2 slots of one type + 1 of the other),
so the manual allocation grid always shows a varied picture. Every floor still balances to
24 × `4W` and 24 × `2W`.

## Project structure

```
src/com/parking/
├── Main.java
├── config/     DBConnection (singleton JDBC connection)
├── model/      POJOs (User, Vehicle, Employee, Floor, ParkingSlot, ParkingTicket)
├── dao/        JDBC data access objects
├── ui/         Swing screens and dialogs
└── util/       FeeCalculator, UserIdGenerator
```

## Billing rules

- Under 60 minutes: free
- 60 minutes and above: rounded up to the next completed hour
- 2W: ₹40/hour
- 4W: ₹80/hour

## Notes

- All SQL uses `PreparedStatement` (no string concatenation).
- Signup, entry and exit use explicit JDBC transactions with rollback on failure.
- Slot availability is re-checked inside the entry transaction to avoid double booking.
- Hovering is not required: every workflow completes with a search, a selection and a confirm.
