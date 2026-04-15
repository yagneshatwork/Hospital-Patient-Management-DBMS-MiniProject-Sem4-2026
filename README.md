# 🏥 Hospital Patient Management System
**DBMS Mini Project — Java Swing + JDBC + MySQL**

---

## 📋 Project Overview

A complete Desktop application to manage hospital patient records, treatments, billing, and audit history. Demonstrates key DBMS concepts:

| Concept | Implementation |
|---|---|
| **Stored Function** | `get_total_treatment_cost(patient_id)` — calculates total bill |
| **BEFORE UPDATE Trigger** | Logs old patient data to `Audit` table automatically |
| **BEFORE DELETE Trigger** | Logs deleted patient data to `Audit` table automatically |
| **Foreign Keys** | `Treatment.patient_id` → `Patient`, `Billing.patient_id` → `Patient` |
| **JDBC** | `DBConnection.java` — Java ↔ MySQL connectivity |
| **Java Swing UI** | Dark-themed `JTabbedPane` with 4 functional panels |

---

## 🗂️ Project Structure

```
Hospital Patient Management/
├── src/
│   ├── DBConnection.java      ← JDBC connection factory
│   ├── UIHelper.java          ← Dark theme colours, fonts, factories
│   ├── MainApp.java           ← Main JFrame + JTabbedPane entry point
│   ├── PatientPanel.java      ← Add / Update / Delete patients
│   ├── TreatmentPanel.java    ← Add treatment records
│   ├── BillingPanel.java      ← Calculate bill via stored function
│   └── AuditPanel.java        ← View trigger-generated audit log
├── lib/
│   └── mysql-connector-j-*.jar   ← ⚠ YOU MUST DOWNLOAD THIS
├── bin/                       ← Compiled .class files (auto-created)
├── hospital_db.sql            ← Full DB setup script
├── compile.bat                ← Compile all Java files
├── run.bat                    ← Launch the application
└── README.md
```

---

## ⚙️ Prerequisites

| Requirement | Download |
|---|---|
| Java JDK 8 or later | https://www.oracle.com/java/technologies/downloads/ |
| MySQL Server 8.x | https://dev.mysql.com/downloads/mysql/ |
| MySQL Connector/J | https://dev.mysql.com/downloads/connector/j/ |

---

## 🚀 Setup Instructions

### Step 1 — Download MySQL Connector JAR
1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Select **Platform Independent** → download the ZIP
3. Extract and copy `mysql-connector-j-*.jar` into the `lib\` folder

### Step 2 — Set Up the Database
Open **MySQL Workbench** or the **MySQL CLI** and run:
```sql
source C:/Users/Admin/Desktop/Hospital Patient Management/hospital_db.sql
```
Or from command prompt:
```bat
mysql -u root -p < "hospital_db.sql"
```
This creates:
- Database: `hospital_db`
- Tables: `Patient`, `Treatment`, `Billing`, `Audit`
- Function: `get_total_treatment_cost()`
- Triggers: `before_patient_update`, `before_patient_delete`

### Step 3 — Configure DB Password
Open `src\DBConnection.java` and update:
```java
private static final String DB_PASS = "password";  // ← change to your MySQL root password
```

### Step 4 — Compile
Double-click **`compile.bat`** or run in terminal:
```bat
compile.bat
```

### Step 5 — Run
Double-click **`run.bat`** or:
```bat
run.bat
```

---

## 💻 Application Features

### 👤 Patients Tab
- **Add** a new patient (Name, Age, Gender, Phone, Address)
- **Select** a row → form auto-populates for editing
- **Update** — triggers `before_patient_update` → logs to Audit
- **Delete** — triggers `before_patient_delete` → logs to Audit
- **Clear** form fields

### 💊 Treatments Tab
- Add treatment records linked to a Patient ID
- Fields: Patient ID, Treatment Name, Cost, Date
- View all treatments in the table

### 💰 Billing Tab
- Enter a Patient ID and click **Calculate Bill**
- Calls MySQL stored function: `SELECT get_total_treatment_cost(?)`
- Shows total cost prominently
- **Save to Billing** — inserts a record into the `Billing` table
- Right panel shows billing history

### 📜 Audit Log Tab
- Shows all entries auto-inserted by the two triggers
- **UPDATE** rows — highlighted in amber
- **DELETE** rows — highlighted in red
- **Clear Logs** button to purge audit history

---

## 🗄️ Database Schema

```sql
Patient     (patient_id PK, name, age, gender, phone, address, created_at)
Treatment   (treatment_id PK, patient_id FK, treatment_name, cost, treatment_date)
Billing     (bill_id PK, patient_id FK, total_amount, bill_date)
Audit       (audit_id PK, patient_id, name, action_type, action_time)
```

---

## 🧠 VIVA Answers

**Q: What is a Stored Function?**
> A function stored in the DB that returns a single value. Used in SQL queries.
> Example: `SELECT get_total_treatment_cost(3)` returns the total cost for patient #3.

**Q: What is a Trigger?**
> Code that executes automatically on a DB event (INSERT/UPDATE/DELETE).
> `before_patient_update` fires before any UPDATE on Patient — logs old data to Audit.
> `before_patient_delete` fires before any DELETE on Patient — same.

**Q: What is JDBC?**
> Java Database Connectivity — Java API to connect to relational databases.
> `DBConnection.java` uses `DriverManager.getConnection()` to get a MySQL connection.

**Q: What is an Audit Table?**
> Tracks historical changes to records. Critical in real-world systems for security and compliance.

**Q: What is a Foreign Key?**
> Enforces referential integrity between tables.
> `Treatment.patient_id` references `Patient.patient_id` — can't add treatment for a non-existent patient.

---

## 🛠️ Troubleshooting

| Problem | Fix |
|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Put mysql-connector-j.jar in `lib\` folder |
| `Access denied for user 'root'` | Check DB_PASS in DBConnection.java |
| `Unknown database 'hospital_db'` | Run hospital_db.sql first |
| Blank table after adding patient | Click Refresh or re-open the tab |
| `compile.bat` not recognized | Make sure JDK is installed and JAVA_HOME is set |

---

*Built for DBMS Mini Project — Java Swing + JDBC + MySQL*
