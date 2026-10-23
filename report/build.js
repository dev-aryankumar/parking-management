/*
 * Builds report.html for the Smart Parking Management System project report.
 * All table data is read from live MySQL dumps so the document cannot drift
 * from the database.
 */
const fs = require('fs');
const path = require('path');

const ROOT = 'C:/Users/aryank/Pictures/Parking_mgmt';
const ASSETS = path.join(ROOT, 'report_assets');
const DATA = path.join(ASSETS, 'data');

const read = (file) => {
  const buffer = fs.readFileSync(file);
  let text;
  if (buffer[0] === 0xff && buffer[1] === 0xfe) {
    text = buffer.toString('utf16le');
  } else {
    text = buffer.toString('utf8').replace(/^\uFEFF/, '');
  }
  return text.replace(/\u0000/g, '');
};

const tsv = (name) => read(path.join(DATA, name + '.tsv'))
  .split(/\r?\n/).filter((l) => l.trim().length).map((l) => l.split('\t'));

const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

// ---------------------------------------------------------------- source data
const columns = tsv('columns');
const fks = tsv('fks');
const counts = tsv('counts');
const sections = tsv('sections');
const users = tsv('users');
const vehicles = tsv('vehicles');
const tickets = tsv('tickets');

const fkMap = {};
for (const [table, column, refTable, refColumn] of fks) {
  fkMap[`${table.toLowerCase()}.${column.toLowerCase()}`] = `${refTable}(${refColumn})`;
}

/** MySQL on Windows reports identifiers in lower case; normalise for lookup. */
const key = (name) => name.toLowerCase();

const byTable = {};
for (const row of columns) {
  (byTable[key(row[0])] = byTable[key(row[0])] || []).push(row);
}

const schemaSql = read(path.join(ROOT, 'schema.sql'));
const ddl = (schemaSql.match(/CREATE TABLE[\s\S]*?ENGINE=[^\n]*;/g) || [])
  .map((s) => s.trim());

// ---------------------------------------------------------------- page helpers
const pages = [];
let toc = [];

function page(html, opts = {}) {
  pages.push({ html, footer: opts.footer !== false });
  return pages.length; // printed page number
}

const fig = (file, caption, cls = '') =>
  `<figure class="shot ${cls}"><img src="report_assets/shots/${file}.png" alt="${esc(caption)}"/>` +
  `<figcaption>${caption}</figcaption></figure>`;

const dataTable = (headers, rows, cls = '') =>
  `<table class="grid ${cls}"><thead><tr>${headers.map((h) => `<th>${h}</th>`).join('')}</tr></thead>` +
  `<tbody>${rows.map((r) => `<tr>${r.map((c) => `<td>${c}</td>`).join('')}</tr>`).join('')}</tbody></table>`;

const code = (text, cls = '') => `<pre class="code ${cls}">${esc(text)}</pre>`;

// ---------------------------------------------------------------------- cover
page(`
<div class="cover">
  <img class="cover-logo" src="report_assets/logo.png" alt="Manipal University Jaipur"/>
  <div class="cover-top">A PROJECT REPORT on</div>
  <h1 class="cover-title">Smart Parking Management System</h1>
  <div class="cover-sub">A Zero-Hardware Desktop Solution using Java Swing, JDBC and MySQL</div>
  <div class="cover-rule"></div>
  <div class="cover-block">
    <div>Submitted to <b>Manipal University Jaipur</b></div>
    <div>Towards the partial fulfillment for the Award of the Degree of</div>
    <div class="cover-degree">B. Tech Computer Science and Engineering</div>
    <div>2026&ndash;2027</div>
  </div>
  <div class="cover-block">
    <div class="cover-small">By</div>
    <div class="cover-name">Aryan Kumar</div>
    <div class="cover-reg">Registration Number: 2502052104</div>
  </div>
  <div class="cover-block">
    <div class="cover-small">Under the guidance of</div>
    <div class="cover-guide">Dr. Mayank Namdev</div>
  </div>
  <div class="cover-foot">
    <div class="cover-dept">Department of Computer Science and Engineering</div>
    <div class="cover-univ">Manipal University Jaipur</div>
    <div class="cover-univ">Jaipur, Rajasthan</div>
  </div>
</div>`, { footer: false });

// ------------------------------------------------------------------- abstract
const abstractPage = page(`
<h1 class="section">Abstract</h1>
<p>Parking management in institutional and commercial campuses is still largely a manual
operation: an attendant writes a vehicle number in a register, walks the floor to find a free
bay, and computes the charge by hand at the exit. The process is slow, error-prone, and gives
the driver no visibility into where space is available. Dedicated hardware &mdash; barrier
gates, inductive loop sensors, RFID readers &mdash; solves the problem but is expensive to
procure and to maintain for a single campus installation.</p>

<p>This project delivers a <b>Smart Parking Management System</b> as a desktop application that
achieves the same operational goals with <b>no external hardware at all</b>. It is built with
<b>Java Swing</b> for the presentation layer, <b>JDBC</b> for data access, and <b>MySQL 8</b>
as the relational back end, organised in a strict model&ndash;view&ndash;controller structure
with one Data Access Object per relation.</p>

<p>The system provides two portals. The <b>customer portal</b> lets a driver self-register,
receive an automatically generated identifier of the form <code>USR-XXXX</code>, register one
or more vehicles, and view the parking sessions that are currently active against them. The
<b>employee portal</b> drives the two gates: at the entry gate the attendant looks up a
customer by identifier, picks one of that customer's registered vehicles, and either lets the
system allocate the first free bay automatically or selects a bay from a visual
<b>4&times;4 pillar-section grid</b> that colours every section by live availability. At the
exit gate the same identifier is used to retrieve the open session, compute the charge and
settle it, which closes the ticket and releases the bay in a single database transaction.</p>

<p>Billing follows the tariff card of the facility: parking is free for the first sixty
minutes, every commenced hour thereafter is charged at <b>&#8377;40</b> for a two-wheeler and
<b>&#8377;80</b> for a four-wheeler. The rate is resolved from the vehicle type recorded in the
database, so a customer who brings a different vehicle on a later visit is charged correctly
without any change to the application.</p>

<p>The database is normalised to third normal form across six relations &mdash; <code>Users</code>,
<code>Vehicles</code>, <code>Employees</code>, <code>Floors</code>, <code>ParkingSlots</code>
and <code>ParkingTickets</code> &mdash; with foreign keys enforced by InnoDB. The seeded
installation models a three-floor facility with <b>144 bays</b> (72 four-wheeler and 72
two-wheeler), 16 pillar sections per floor, 16 customer accounts and 28 registered vehicles.
Availability is derived from the slot table rather than stored as a counter, so concurrent
gate operations can never double-allocate a bay.</p>

<p>The application was validated with a 19-check integration test suite executed against the
live database, covering identifier generation, duplicate-vehicle rejection, slot allocation,
double-allocation prevention, fee boundaries and the full entry&ndash;exit lifecycle. All checks
passed, and the test data was rolled back afterwards.</p>
`);

// ------------------------------------------------------------ table of contents
const tocPage = page(`
<h1 class="section">Table of Contents</h1>
<div id="toc"></div>
`);

// ---------------------------------------------------------------- 1. intro
const intro1 = page(`
<h1 class="section">1. Introduction</h1>
<p>Parking is an inventory problem disguised as real estate. A facility has a fixed number of
bays, each bay can hold one vehicle at a time, and every vehicle that enters must eventually
leave and be billed. When this inventory is tracked on paper, three failures follow almost
immediately: bays are double-booked because the register is out of date, revenue leaks because
the elapsed time is estimated rather than measured, and customers queue at the gate while an
attendant walks the floor to find a space.</p>

<p>This project replaces the register with a relational database and puts a thin, purpose-built
desktop application in front of it. The application is deliberately hardware-free: allocation,
occupancy and billing are all derived from rows in MySQL, so the system can be evaluated,
demonstrated and graded on an ordinary laptop. Every screen in the application reads from and
writes to the database at the moment it is used &mdash; there is no in-memory mock layer and no
pre-computed report.</p>

<h2>1.1 Scope of the Work</h2>
<p>The scope below is what the delivered application actually does, verified against the running
build.</p>

<table class="grid scope">
  <thead><tr><th style="width:26%">Area</th><th>Included in this project</th></tr></thead>
  <tbody>
    <tr><td><b>Customer registration</b></td><td>Self-service sign-up from the login screen. The system generates the next identifier in the
      <code>USR-XXXX</code> range and registers the customer's first vehicle inside the same transaction.</td></tr>
    <tr><td><b>Vehicle registry</b></td><td>Each customer may hold several vehicles, each with a number plate and a type
      (<code>2W</code> / <code>4W</code>). Duplicate plates are rejected across the whole facility.</td></tr>
    <tr><td><b>Authentication</b></td><td>Separate authentication for customers and for employees. Employees are provisioned
      by the administrator; there is no employee self-registration screen.</td></tr>
    <tr><td><b>Entry gate</b></td><td>Lookup by customer identifier, vehicle selection from the vehicles registered against that
      customer, and either automatic allocation of the first free bay of the correct type or
      manual selection from the visual pillar grid.</td></tr>
    <tr><td><b>Occupancy view</b></td><td>A 4&times;4 grid of pillar sections per floor, refreshed from the database, showing the
      number of free bays in each section or <span class="tag-full">FULL</span> when none remain.</td></tr>
    <tr><td><b>Exit gate and billing</b></td><td>Retrieval of the open session, duration calculation, tariff resolution by vehicle type,
      settlement, and an itemised receipt. Settlement closes the ticket and frees the bay atomically.</td></tr>
    <tr><td><b>Customer dashboard</b></td><td>A live list of the customer's active sessions (ticket, vehicle, floor, pillar section and entry
      time) that empties automatically once the vehicle has exited.</td></tr>
    <tr><td><b>Out of scope</b></td><td>Barrier-gate hardware, ANPR cameras, RFID tags, online payment gateways, Native mobile
      clients and reservation of a specific bay in advance.</td></tr>
  </tbody>
</table>
`);

const intro2 = page(`
<h1 class="section">1. Introduction (continued)</h1>
<h2>1.2 Product Scenarios</h2>
<p>Five scenarios describe every interaction the system supports. They are listed in the order a
real customer would experience them, and each corresponds directly to screens shown in
Section&nbsp;7.</p>

<div class="scenario">
  <div class="scenario-title">Scenario 1 &mdash; A new customer joins</div>
  <p>A driver opens the customer portal and fills in a name, a password, a number plate and the
  vehicle type. Submitting the form runs one transaction that generates the next identifier,
  inserts the customer, and inserts the first vehicle. The generated identifier is shown in a
  confirmation dialog and is required at the gate, so the dialog warns the customer to keep it.</p>
</div>

<div class="scenario">
  <div class="scenario-title">Scenario 2 &mdash; A vehicle arrives at the entry gate</div>
  <p>The attendant types the customer identifier. The system resolves the customer, lists the
  vehicles registered to that customer, and defaults to automatic allocation. Confirming inserts
  a parking ticket, marks the chosen bay occupied, and prints an entry receipt with the ticket
  number, floor, pillar section and timestamp.</p>
</div>

<div class="scenario">
  <div class="scenario-title">Scenario 3 &mdash; The attendant prefers to choose the bay</div>
  <p>Switching the allocation mode to <i>Manual</i> reveals the pillar grid for the selected floor.
  Every section is drawn from live counts: a green card reports how many bays of the required
  type are still free, a red card reports <span class="tag-full">FULL</span>. Selecting a section
  outlines it in amber; a section that fills up between the refresh and the confirmation is
  re-checked inside the transaction and rejected rather than double-allocated.</p>
</div>

<div class="scenario">
  <div class="scenario-title">Scenario 4 &mdash; The vehicle leaves</div>
  <p>At the exit gate the attendant searches the same identifier. The system finds the open ticket,
  computes the elapsed time and applies the tariff: nothing for the first hour, then every
  commenced hour at the rate for that vehicle's type. Confirming the payment writes the exit
  timestamp and the amount, and releases the bay. The receipt itemises the rate, the billable
  hours and the total.</p>
</div>

<div class="scenario">
  <div class="scenario-title">Scenario 5 &mdash; The customer checks their own session</div>
  <p>From the customer portal the driver logs in with the identifier and password created in
  Scenario&nbsp;1 and sees the active session &mdash; vehicle, floor, pillar section and entry
  time. The list is populated from the database on every login and becomes empty the moment the
  vehicle has exited, so no stale state is ever displayed.</p>
</div>
`);

// ------------------------------------------------------------------ 2. ERD
const erdPage = page(`
<h1 class="section">2. ER Diagram</h1>
<p>The entity&ndash;relationship model contains six entity types. <code>Users</code> and
<code>Vehicles</code> describe the customer side, <code>Employees</code> describes the staff who
operate the gates, <code>Floors</code> and <code>ParkingSlots</code> describe the physical
facility, and <code>ParkingTickets</code> is the associative entity that records one parking
session. Every relationship in the diagram is one-to-many with the foreign key on the
<i>many</i> side, which is exactly how it is implemented in the schema.</p>

<figure class="diagram">
  <img src="report_assets/erd_final.png" alt="Entity relationship diagram"/>
  <figcaption>Figure 2.1 &mdash; Entity&ndash;relationship diagram of the Smart Parking
  Management System. Primary keys are underlined; cardinality is shown in crow's-foot
  notation.</figcaption>
</figure>

<h2>Cardinality summary</h2>
${dataTable(
  ['Relationship', 'Rule', 'Participation', 'Implementation'],
  [
    ['Users &rarr; Vehicles', '1 : N', 'A vehicle belongs to exactly one customer; a customer may hold many vehicles.', '<code>Vehicles.user_id</code> references <code>Users.user_id</code>'],
    ['Vehicles &rarr; ParkingTickets', '1 : N', 'A vehicle accumulates one ticket per visit.', '<code>ParkingTickets.vehicle_number</code> references <code>Vehicles.vehicle_number</code>'],
    ['Employees &rarr; ParkingTickets', '1 : N', 'Every ticket records the attendant who issued it.', '<code>ParkingTickets.employee_id</code> references <code>Employees.employee_id</code>'],
    ['Floors &rarr; ParkingSlots', '1 : N', 'Each bay belongs to exactly one floor.', '<code>ParkingSlots.floor_id</code> references <code>Floors.floor_id</code>'],
    ['ParkingSlots &rarr; ParkingTickets', '1 : N', 'A bay is reused across visits and therefore appears on many tickets, but at most one open ticket at a time.', '<code>ParkingTickets.slot_id</code> references <code>ParkingSlots.slot_id</code>']
  ])}
`);

// ------------------------------------------------- 3. relational schema diagram
const relPage = page(`
<h1 class="section">3. Relational Schema Diagram</h1>
<p>Mapping the ER model to relations produces six tables. Each entity becomes a table, each
one-to-many relationship is absorbed by placing the primary key of the <i>one</i> side into the
<i>many</i> side as a foreign key, and the ticket entity keeps its own surrogate key because a
single vehicle and a single bay can legitimately appear on many tickets over time.</p>

<figure class="diagram">
  <img src="report_assets/relationalschema_final.png" alt="Relational schema diagram"/>
  <figcaption>Figure 3.1 &mdash; Relational schema. Primary keys are shown in bold, foreign keys
  are annotated with their referenced relation, and every relationship is 1:N.</figcaption>
</figure>

<h2>Normalisation</h2>
<p>All six relations are in <b>third normal form</b>. Every attribute is atomic (1NF) &mdash; the
number of bays is not stored as a comma-separated list, and the vehicle type is a constrained
<code>ENUM</code> rather than free text. Every non-key attribute depends on the whole primary key
(2NF); in practice all primary keys are single columns, so this holds by construction. Finally
there are no transitive dependencies (3NF): the floor number appears only in <code>Floors</code>
and is reached from a slot by join, and the tariff is not stored at all because it is a function
of the vehicle type, which lives in <code>Vehicles</code>.</p>

<p>Occupancy is intentionally <b>derived rather than duplicated</b>. The only stored fact is
<code>ParkingSlots.is_occupied</code>; the number of free bays in a section is computed with a
grouped <code>COUNT</code>. Storing a per-section counter as well would introduce a transitive
dependency and a second source of truth that could drift from the slot rows.</p>
`);

// --------------------------------------------------- 4. relational schema tables
function tablePage(titles, heading, lead) {
  const body = titles.map(({ name, number }) => {
    const rows = byTable[key(name)].map((row) => {
      const [, , column, type, nullable, keyName, dflt] = row;
      const ref = fkMap[`${key(name)}.${column.toLowerCase()}`];
      const keyCell = keyName === 'PRI' ? 'PK' : (ref ? 'FK' : '');
      const refCell = ref ? `<code>${esc(ref)}</code>` : '';
      return [keyCell, `<code>${esc(column)}</code>`, `<code>${esc(type)}</code>`,
        nullable === 'NO' ? 'NOT NULL' : 'NULL',
        dflt === '{NULL}' ? '&mdash;' : `<code>${esc(dflt)}</code>`, refCell];
    });
    return `<h2>4.${number} ${name}</h2>
      <p class="table-desc">${TABLE_NOTES[name]}</p>
      ${dataTable(['Key', 'Column', 'Type', 'Null', 'Default', 'References'], rows, 'schema')}`;
  }).join('\n');

  return page(`<h1 class="section">${heading}</h1>${lead || ''}${body}`);
}

const TABLE_NOTES = {
  Users: 'One row per customer account. The identifier is generated by the application in the USR-XXXX range and is the credential the customer presents at the gate.',
  Vehicles: 'One row per registered vehicle. A customer may register several vehicles, which is why the foreign key points at Users rather than the plate being part of the customer key.',
  Employees: 'Gate staff accounts. Employees are provisioned by the administrator; the role column distinguishes a manager from an attendant.',
  Floors: 'The physical floors of the facility. Floor numbering is stored as text so labels such as "Ground" or "B1" are possible without a schema change.',
  ParkingSlots: 'One row per physical bay. A bay is identified by its floor and pillar section, and its type restricts which vehicles may occupy it. is_occupied is the single source of truth for availability.',
  ParkingTickets: 'One row per parking session. exit_time and total_fee are NULL while the session is open, so an active session is simply a row with a NULL exit_time.'
};

tablePage([{ name: 'Users', number: 1 }, { name: 'Vehicles', number: 2 }, { name: 'Employees', number: 3 }],
  '4. Relational Schema Tables',
  `<p>The six relations below are documented exactly as they exist in the deployed database; the
  column definitions, defaults and foreign keys were read from <code>INFORMATION_SCHEMA</code> at
  the time this report was generated.</p>`);
tablePage([{ name: 'Floors', number: 4 }, { name: 'ParkingSlots', number: 5 }],
  '4. Relational Schema Tables (continued)');
tablePage([{ name: 'ParkingTickets', number: 6 }],
  '4. Relational Schema Tables (continued)',
  `<p>The ticket relation is the only one that grows without bound during operation. Its composite
  index on <code>(vehicle_number, exit_time)</code> keeps the "is this vehicle already parked?"
  check and the active-session lookup equally cheap as history accumulates.</p>`);

// ------------------------------------------------------------------ 5. DDL
const ddlIntro = `<p>The database is created from a single script. The statements below are the
exact DDL used by the deployed schema, including the storage engine, character set and every
referential constraint. Foreign keys use <code>ON UPDATE CASCADE</code> so a corrected key
propagates, and <code>ON DELETE RESTRICT</code> so history cannot be orphaned by deleting a
customer, vehicle or bay that still has tickets.</p>`;

page(`<h1 class="section">5. SQL &amp; DDL Queries</h1>${ddlIntro}
<h2>5.1 Database and first two relations</h2>
${code(`CREATE DATABASE smart_parking
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_parking;

${ddl[0]}

${ddl[1]}`)}`);

page(`<h1 class="section">5. SQL &amp; DDL Queries (continued)</h1>
<h2>5.2 Staff, floors and bays</h2>
${code(`${ddl[2]}

${ddl[3]}

${ddl[4]}`)}`);

page(`<h1 class="section">5. SQL &amp; DDL Queries (continued)</h1>
<h2>5.3 Ticket relation</h2>
${code(ddl[5])}
<h2>5.4 Indexes and why they exist</h2>
${dataTable(['Index', 'Relation', 'Purpose'], [
  ['PRIMARY', 'all six relations', 'Unique row identity; every foreign key targets a primary key.'],
  ['<code>idx_vehicles_user</code>', 'Vehicles', 'Loads the vehicle list for a customer at the entry gate.'],
  ['<code>idx_slots_availability</code>', 'ParkingSlots', 'Makes the automatic allocation lookup (<code>slot_type</code> + <code>is_occupied</code>) a range scan instead of a table scan.'],
  ['<code>idx_slots_pillar</code>', 'ParkingSlots', 'Backs the per-section free-bay counts that colour the pillar grid.'],
  ['<code>idx_tickets_active</code>', 'ParkingTickets', 'Detects "vehicle already parked" and finds open sessions without scanning history.'],
  ['<code>idx_tickets_slot</code>', 'ParkingTickets', 'Supports joins from the bay side and future occupancy reporting.']
])}`);

// --------------------------------------------------------------- 6. DML
page(`<h1 class="section">6. Sample DML Queries</h1>
<p>Three kinds of DML reach the database at runtime: the inserts performed during registration,
the inserts and updates that implement the two gates, and the reads that populate the dashboards.
The statements below are the ones the application actually issues, shown with representative
values.</p>

<h2>6.1 Registration (one transaction)</h2>
${code(`-- The next identifier is derived from the highest existing suffix.
SELECT COUNT(*) FROM Users WHERE user_id = ?;

INSERT INTO Users (user_id, name, password) VALUES ('USR-8401', 'Nisha Verma', 'nisha@123');

INSERT INTO Vehicles (vehicle_number, user_id, type) VALUES ('RJ14-QX-7788', 'USR-8401', '4W');

-- Committed only if both inserts succeed; rolled back otherwise.`)}

<h2>6.2 Entry gate</h2>
${code(`-- Customer lookup and vehicle dropdown
SELECT user_id, name, password FROM Users WHERE user_id = 'USR-1001';
SELECT vehicle_number, user_id, type FROM Vehicles WHERE user_id = 'USR-1001' ORDER BY vehicle_number;

-- Automatic allocation: first free bay of the matching type
SELECT slot_id, floor_id, pillar_section, slot_type, is_occupied
FROM ParkingSlots WHERE slot_type = '4W' AND is_occupied = FALSE
ORDER BY floor_id, slot_id LIMIT 1;

-- Manual allocation: free bays inside the chosen section
SELECT slot_id, floor_id, pillar_section, slot_type, is_occupied
FROM ParkingSlots
WHERE floor_id = 1 AND pillar_section = 'A1-A2' AND slot_type = '4W' AND is_occupied = FALSE
ORDER BY slot_id;

-- Ticket creation, then the bay is marked occupied (same transaction)
INSERT INTO ParkingTickets (vehicle_number, slot_id, employee_id, entry_time)
VALUES ('KA-01-AB-1234', 1, 1, NOW());

UPDATE ParkingSlots SET is_occupied = TRUE WHERE slot_id = 1;`)}`);

page(`<h1 class="section">6. Sample DML Queries (continued)</h1>
<h2>6.3 Exit gate</h2>
${code(`-- Most recent open session for the customer
SELECT pt.* FROM ParkingTickets pt
JOIN Vehicles v ON v.vehicle_number = pt.vehicle_number
WHERE v.user_id = 'USR-1001' AND pt.exit_time IS NULL
ORDER BY pt.entry_time DESC LIMIT 1;

-- Settlement and release, both inside one transaction
UPDATE ParkingTickets SET exit_time = NOW(), total_fee = 240.00
WHERE ticket_id = 1 AND exit_time IS NULL;

UPDATE ParkingSlots SET is_occupied = FALSE WHERE slot_id = 1;`)}

<h2>6.4 Verification queries</h2>
${code(`SELECT COUNT(*) FROM ParkingSlots WHERE slot_type = '4W' AND is_occupied = FALSE;

SELECT floor_id, pillar_section,
       SUM(slot_type = '2W') AS two_wheeler,
       SUM(slot_type = '4W') AS four_wheeler,
       SUM(is_occupied)      AS occupied
FROM ParkingSlots WHERE floor_id = 1
GROUP BY floor_id, pillar_section ORDER BY pillar_section;

SELECT COUNT(*) FROM ParkingTickets WHERE vehicle_number = ? AND exit_time IS NULL;`)}

<h2>6.5 Result of the verification queries</h2>
${dataTable(['Relation', 'Rows'], counts.map((r) => [r[0], r[1]]), 'narrow')}
<p class="note">Counted immediately after the demonstration run shown in Section&nbsp;7, in which one
new customer registered, one vehicle was parked and then released.</p>`);

// ------------------------------------------------------------- 7. screenshots
page(`<h1 class="section">7. Screenshots</h1>
<p>The screens below were captured from the running application against the live database. They are
presented in the order of a complete parking lifecycle: registration, login, entry, allocation,
settlement and the customer's view of an emptied dashboard. The database tables that back these
screens follow in Section&nbsp;7.2.</p>
<h2>7.1 Frontend &mdash; customer and gate screens</h2>
${fig('01_portal', 'Figure 7.1 &mdash; Portal selection. The application starts by asking whether the operator is a customer or an employee.')}
${fig('02_signup', 'Figure 7.2 &mdash; Customer registration. Name, password, number plate and vehicle type are captured in one form; the vehicle is created in the same transaction as the customer.')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('03_signup_success', 'Figure 7.3 &mdash; Confirmation dialog returning the generated identifier. This value is the customer\'s credential at the gate.', 'small')}
${fig('04_login', 'Figure 7.4 &mdash; Customer login. Identifiers follow the USR-XXXX format and are validated before the database is queried.')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('05_employee_login', 'Figure 7.5 &mdash; Employee login. Gate staff are provisioned by the administrator rather than self-registered.')}
${fig('06_entry_search', 'Figure 7.6 &mdash; Entry gate after a customer lookup. The vehicle dropdown is populated from the vehicles registered to that customer.')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('07_manual_grid', 'Figure 7.7 &mdash; Manual allocation grid for Floor 1. Each card is one pillar section; green cards report the free bays of the required type, red cards are full. Mixed sections such as "2 free" and "1 free" appear side by side because bay types are assigned per section, not per row.')}
${fig('08_manual_grid_selected', 'Figure 7.8 &mdash; A section chosen by the attendant. The amber outline marks the selection; the first free bay in that section becomes the ticket\'s bay.')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('09_entry_receipt', 'Figure 7.9 &mdash; Entry receipt written after the ticket and the bay update are committed.', 'small')}
${fig('10_user_dashboard', 'Figure 7.10 &mdash; Customer dashboard showing the live session: ticket, vehicle, type, floor, pillar section and entry time.')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('11_exit_summary', 'Figure 7.11 &mdash; Exit gate. The open session is priced from the stored entry timestamp: a three-hour four-wheeler session at the ₹80 hourly rate.')}
${fig('12_exit_receipt', 'Figure 7.12 &mdash; Settlement receipt itemising the rate, billable hours and total. Confirming also released the bay.', 'small')}`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
${fig('13_dashboard_empty', 'Figure 7.13 &mdash; The same customer after settlement. Because the dashboard is rebuilt from the database on every login, the closed session disappears and the empty state is shown immediately.')}
<h2>7.2 Backend &mdash; database tables</h2>
<p>The tables below are rendered from the live database with the same queries the application uses;
they are not mock-ups. Column headings match the physical schema.</p>`, {});

page(`<h1 class="section">7. Screenshots (continued)</h1>
<h2>Backend &mdash; Users</h2>
${dataTable(['user_id', 'name', 'created_at'], users.map((r) => [`<code>${esc(r[0])}</code>`, esc(r[1]), esc(r[2])]), 'rows')}
<p class="note">Sixteen customer accounts: the fifteen seeded demonstration accounts plus the one
created during the run captured above. Identifiers are generated by the application and are never
reused.</p>`);

page(`<h1 class="section">7. Screenshots (continued)</h1>
<h2>Backend &mdash; Vehicles</h2>
${dataTable(['vehicle_number', 'user_id', 'type'], vehicles.map((r) => [`<code>${esc(r[0])}</code>`, `<code>${esc(r[1])}</code>`, esc(r[2])]), 'rows')}
<p class="note">Twenty-eight registered vehicles. The plate is the primary key, so the same vehicle
cannot be registered twice under different customers.</p>`);

const sectionRows = sections.map((r) => [
  r[0], `<code>${esc(r[1])}</code>`, r[2], r[3],
  Number(r[4]) === 0 ? '<span class="tag-free">free</span>' : `<span class="tag-full">${r[4]} occupied</span>`
]);
page(`<h1 class="section">7. Screenshots (continued)</h1>
<h2>Backend &mdash; ParkingSlots, Floor 1 (aggregated per section)</h2>
${dataTable(['Floor', 'Pillar section', '2W bays', '4W bays', 'Occupancy'], sectionRows, 'rows')}
<p class="note">Bay types are assigned per section at seed time, so the mix is irregular: some
sections are entirely four-wheeler, some entirely two-wheeler, and others hold both. This is what
the grid in Figure&nbsp;7.7 displays as "2 free", "1 free" or FULL. The three floors hold 144 bays
in total, split evenly between the two vehicle types.</p>`);

page(`<h1 class="section">7. Screenshots (continued)</h1>
<h2>Backend &mdash; ParkingTickets after settlement</h2>
${dataTable(['ticket', 'vehicle', 'slot', 'employee', 'entry_time', 'exit_time', 'total_fee'],
  tickets.map((r) => r.map((c, i) => (i === 0 || i === 2 || i === 3) ? `<code>${esc(c)}</code>` : esc(c))), 'rows')}
<p class="note">A single closed session remains from the demonstration run. The amount equals three
commenced hours at the ₹80 four-wheeler rate; the first hour was free and the partial minute is
rounded up to the next commenced hour. The corresponding slot row is back to
<code>is_occupied = FALSE</code>.</p>`);

// --------------------------------------------------------------- 8. conclusion
page(`<h1 class="section">8. Conclusion</h1>
<p>The project set out to show that a parking facility can be operated from a relational database
and an ordinary desktop application, without barrier gates, sensors or tags. That goal has been
met: the delivered build performs registration, gate operations, allocation, billing and receipting
against a normalised MySQL schema, and every screen reads and writes live data.</p>

<p>Three design decisions carried most of the weight. First, <b>availability is derived, never
duplicated</b>: the only stored fact is whether a bay is occupied, so the grid and the automatic
allocator can never disagree with each other. Second, <b>each gate operation is one transaction</b>:
a ticket is inserted and a bay is flipped in the same commit, and the bay is re-checked inside that
transaction, which makes double allocation impossible even if two attendants act at the same
moment. Third, <b>the tariff is a function of the data</b>: the rate is resolved from the vehicle
type on the ticket rather than being hard-coded per screen, so a customer who arrives in a different
vehicle on a later day is priced correctly with no code change.</p>

<p>The visual pillar grid deserves a note because it is the part most often missing from
database coursework. A table of slots is easy to render as a list; rendering it as a floor plan
that an attendant can read at a glance requires the free-bay count per section to be a cheap
grouped query, which in turn justified the composite index on
<code>(floor_id, pillar_section, slot_type)</code>.</p>

<h2>Future scope</h2>
<table class="grid scope">
  <thead><tr><th style="width:26%">Direction</th><th>What it would add</th></tr></thead>
  <tbody>
    <tr><td><b>Online payments</b></td><td>A payment gateway at settlement, with the ticket storing a transaction reference and a settled/failed state.</td></tr>
    <tr><td><b>Reservations</b></td><td>A reservation window on a bay, protected by the same transactional re-check used at the entry gate.</td></tr>
    <tr><td><b>Reporting</b></td><td>Revenue per floor, per hour and per vehicle type, plus average occupancy, driven from <code>ParkingTickets</code> over a date range.</td></tr>
    <tr><td><b>Hardware integration</b></td><td>ANPR or RFID at the gate to remove the identifier lookup, keeping the same service layer beneath.</td></tr>
    <tr><td><b>Security</b></td><td>Password hashing (currently stored as plain text for demonstrability) and role-based separation of manager and attendant actions.</td></tr>
  </tbody>
</table>

<h2>Learning outcomes</h2>
<p>The work consolidated practical database design &mdash; normalisation, referential integrity and
index selection &mdash; with the discipline of writing code that cannot leave the database in an
inconsistent state when something fails midway. It also required a layered Java codebase
(model, DAO, view) that keeps SQL out of the user interface.</p>`);

// ----------------------------------------------------------------- 9. references
page(`<h1 class="section">9. References</h1>
<ol class="refs">
  <li>Oracle Corporation. <i>Java Platform, Standard Edition 21 API Specification</i>. Available at:
    https://docs.oracle.com/en/java/javase/21/docs/api/</li>
  <li>Oracle Corporation. <i>Creating a GUI With Swing &mdash; The Java Tutorials</i>. Available at:
    https://docs.oracle.com/javase/tutorial/uiswing/</li>
  <li>Oracle Corporation. <i>JDBC API Specification and Tutorial</i>. Available at:
    https://docs.oracle.com/javase/tutorial/jdbc/</li>
  <li>Oracle Corporation. <i>MySQL 8.0 Reference Manual</i> &mdash; InnoDB, indexes, <code>ENUM</code>
    and <code>INFORMATION_SCHEMA</code>. Available at: https://dev.mysql.com/doc/refman/8.0/en/</li>
  <li>MySQL Connector/J. <i>Developer Guide</i>. Available at:
    https://dev.mysql.com/doc/connector-j/en/</li>
  <li>Silberschatz, A., Korth, H. F. and Sudarshan, S. <i>Database System Concepts</i>, 7th edition.
    McGraw-Hill, 2019.</li>
  <li>Elmasri, R. and Navathe, S. B. <i>Fundamentals of Database Systems</i>, 7th edition. Pearson,
    2016.</li>
  <li>Gamma, E., Helm, R., Johnson, R. and Vlissides, J. <i>Design Patterns: Elements of Reusable
    Object-Oriented Software</i>. Addison-Wesley, 1994. (Data Access Object and MVC.)</li>
  <li>Manipal University Jaipur. <i>Project Report Format</i>, Department of Computer Science and
    Engineering, 2026.</li>
</ol>`);

// ---------------------------------------------------------------- TOC assembly
const findPage = (needle) => {
  const index = pages.findIndex((p) => p.html.includes(needle));
  return index < 0 ? 0 : index + 1;
};

const tocEntries = [
  ['Abstract', findPage('<h1 class="section">Abstract</h1>')],
  ['1. Introduction', findPage('<h1 class="section">1. Introduction</h1>')],
  ['&nbsp;&nbsp;&nbsp;1.1 Scope of the Work', findPage('1.1 Scope of the Work')],
  ['&nbsp;&nbsp;&nbsp;1.2 Product Scenarios', findPage('1.2 Product Scenarios')],
  ['2. ER Diagram', findPage('2. ER Diagram')],
  ['3. Relational Schema Diagram', findPage('3. Relational Schema Diagram')],
  ['4. Relational Schema Tables', findPage('4. Relational Schema Tables')],
  ['5. SQL &amp; DDL Queries', findPage('5. SQL &amp; DDL Queries')],
  ['6. Sample DML Queries', findPage('6. Sample DML Queries')],
  ['7. Screenshots of Frontend and Backend', findPage('<h1 class="section">7. Screenshots</h1>')],
  ['&nbsp;&nbsp;&nbsp;7.1 Frontend &mdash; customer and gate screens', findPage('7.1 Frontend')],
  ['&nbsp;&nbsp;&nbsp;7.2 Backend &mdash; database tables', findPage('7.2 Backend')],
  ['8. Conclusion', findPage('8. Conclusion')],
  ['9. References', findPage('9. References')]
];

const tocHtml = `<table class="toc">${tocEntries.map(([label, p]) =>
  `<tr><td>${label}</td><td class="dots"></td><td class="num">${p}</td></tr>`).join('')}</table>`;

// The TOC page currently sits at index 2 (printed page 3); indices of all later
// pages shift by nothing, so the numbers above are already final.
pages[tocPage - 1].html = pages[tocPage - 1].html.replace('<div id="toc"></div>', tocHtml);

// --------------------------------------------------------------------- HTML
const html = `<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"/>
<title>Smart Parking Management System - Project Report</title>
<style>
  @page { size: A4; margin: 16mm 15mm 15mm 15mm; }
  * { box-sizing: border-box; }
  body { margin:0; font-family:"Segoe UI", Calibri, Arial, sans-serif; font-size:10.5pt;
         line-height:1.55; color:#1E2B2C; }
  p { margin:0 0 3.2mm 0; text-align:justify; }
  h1.section { font-size:17pt; color:#0F4C5C; margin:0 0 5mm 0; padding-bottom:2mm;
               border-bottom:2.2px solid #F2A900; }
  h2 { font-size:11.5pt; color:#137E7E; margin:6mm 0 2.5mm 0; }
  code { font-family:Consolas, "Courier New", monospace; font-size:9pt; color:#0F4C5C;
         background:#F1F7F7; padding:0 1.2mm; border-radius:2px; }
  .page { position:relative; min-height:262mm; }
  .page + .page { page-break-before:always; }
  .page-footer { position:absolute; left:0; right:0; bottom:-3mm; display:flex;
                 justify-content:space-between; font-size:8pt; color:#7A8A8B;
                 border-top:0.6px solid #CFDDDD; padding-top:1.6mm; }
  .page-logo { position:absolute; top:-12.5mm; right:0; }
  .page-logo img { height:9mm; }

  /* cover */
  .cover { text-align:center; padding-top:0; }
  .cover-top { font-size:12pt; letter-spacing:0.5px; }
  .cover-title { font-size:25pt; color:#0F4C5C; margin:4mm 0 2mm; line-height:1.15; }
  .cover-sub { font-size:13pt; color:#137E7E; font-style:italic; }
  .cover-rule { width:48mm; height:3px; background:#F2A900; margin:6mm auto; }
  .cover-logo { width:70mm; display:block; margin:0 auto 8mm; }
  .cover-block { margin-bottom:6mm; font-size:12pt; line-height:1.7; }
  .cover-degree { font-weight:700; font-size:13pt; }
  .cover-small { font-size:12pt; }
  .cover-name { font-size:14pt; font-weight:700; }
  .cover-reg { font-size:14pt; }
  .cover-guide { font-size:14pt; font-weight:700; color:#0F4C5C; }
  .cover-foot { margin-top:10mm; }
  .cover-dept { font-weight:700; font-size:13pt; color:#0F4C5C; }
  .cover-univ { font-size:16pt; font-weight:700; }

  /* tables */
  table.grid { width:100%; border-collapse:collapse; font-size:8.7pt; margin:2mm 0 4mm;
               page-break-inside:avoid; }
  table.grid th { background:#0F4C5C; color:#fff; text-align:left; padding:1.6mm 2mm;
                  font-weight:600; font-size:8.7pt; }
  table.grid td { padding:1.4mm 2mm; border-bottom:0.5px solid #DCE7E7; vertical-align:top; }
  table.grid tbody tr:nth-child(even) { background:#F5FAFA; }
  table.grid.narrow { width:60%; }
  table.scope td { font-size:9.2pt; }
  table.rows td { font-size:7.9pt; padding:1.05mm 2mm; }
  table.schema td { padding:1.1mm 2mm; }
  table.schema code { font-size:8.4pt; }
  .note { font-size:8.6pt; color:#5C6B6C; font-style:italic; text-align:left; }
  .table-desc { font-size:9.2pt; color:#5C6B6C; margin-bottom:2mm; }

  /* code */
  pre.code { font-family:Consolas, "Courier New", monospace; font-size:8.1pt; line-height:1.42;
             background:#F7FAFA; border:0.6px solid #DCE7E7; border-left:3px solid #137E7E;
             padding:3mm 3.5mm; margin:2mm 0 4mm; white-space:pre-wrap; color:#173B3F; }

  /* figures */
  figure.shot, figure.diagram { margin:0 0 5mm 0; text-align:center; page-break-inside:avoid; }
  figure.shot img { width:72%; border:0.7px solid #CFDDDD; border-radius:1.5mm; }
  figure.shot.small { width:55%; margin-left:auto; margin-right:auto; }
  figure.diagram img { width:100%; border:0.7px solid #CFDDDD; }
  figcaption { font-size:8.4pt; color:#5C6B6C; margin-top:1.6mm; font-style:italic; }

  /* scenarios */
  .scenario { border-left:3px solid #F2A900; background:#FFFBF1; padding:2.5mm 3.5mm;
              margin-bottom:3.5mm; }
  .scenario-title { font-weight:700; color:#0F4C5C; font-size:10pt; margin-bottom:1mm; }
  .scenario p { margin:0; font-size:9.7pt; }
  .tag-full { background:#F8D7D6; color:#8A1C1C; padding:0 1.4mm; border-radius:2px;
              font-size:8.4pt; font-weight:600; }
  .tag-free { background:#CFEFD9; color:#0B5D2A; padding:0 1.4mm; border-radius:2px;
              font-size:8.4pt; font-weight:600; }

  /* toc */
  table.toc { width:100%; border-collapse:collapse; font-size:11pt; }
  table.toc td { padding:1.6mm 0; }
  table.toc td.dots { border-bottom:0.6px dotted #9FB3B4; }
  table.toc td.num { text-align:right; width:12mm; color:#0F4C5C; font-weight:600; }

  ol.refs { font-size:9.6pt; padding-left:6mm; }
  ol.refs li { margin-bottom:2.4mm; }
</style></head><body>
${pages.map((p, i) => `<div class="page">${i > 0
  ? '<div class="page-logo"><img src="report_assets/logo.png" alt="Manipal University Jaipur"/></div>'
  : ''}${p.html}${p.footer
  ? `<div class="page-footer"><span>Smart Parking Management System &mdash; Project Report</span><span>${i + 1}</span></div>`
  : ''}</div>`).join('\n')}
</body></html>`;

fs.writeFileSync(path.join(ROOT, 'report.html'), html);
console.log('pages =', pages.length);
console.log('written report.html', html.length, 'bytes');
