# Sunrise Dental System

Sunrise Dental System is a distributed Java Swing desktop application for managing a dental clinic. It supports authentication, patients, appointments, treatments, billing, staff administration, revenue reporting, and role-specific help.

## Technology

- Java 17 (configured in `nbproject/project.properties`)
- NetBeans project with Apache Ant build targets
- Java Swing desktop UI
- MySQL 8 with JDBC
- JDK `HttpServer` REST-style service with Gson JSON
- JUnit 4 and Mockito tests
- MVC, DAO, Singleton, Factory, and Strategy patterns

## Project Layout

```text
src/       Application and server source code
test/      JUnit tests
src/schema.sql   Database schema
src/seed.sql     Demo users and treatments
src/db.properties  Local database connection settings
nbproject/ NetBeans and Ant project configuration
```

Build output is generated under `build/` and packaged artifacts are placed under `dist/`. These directories should not be edited manually.

## Requirements

1. Java 17 or a compatible JDK configured as the NetBeans active platform.
2. Apache Ant.
3. MySQL 8 or XAMPP with MySQL enabled.
4. The JAR dependencies referenced by `nbproject/project.properties`:
   - Gson 2.10.1
   - jBCrypt 0.4
   - JCalendar 1.4
   - MySQL Connector/J 8.3.0
   - JUnit 4.13.2, Mockito 4.11.0, Hamcrest, Byte Buddy, and Objenesis for tests

The current project configuration references these libraries from `lib/` in the repository root. Download the listed JARs into that directory before running Ant locally.

## Database Setup

1. Start MySQL.
2. Run `src/schema.sql` in MySQL. It creates the `sunrise_dental_db` database and its tables.
3. Run `src/seed.sql` to add demo accounts and sample treatments.
4. Check `src/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/sunrise_dental_db
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

Do not commit real database credentials.

The seeded demo password is `password` for these accounts:

| Username | Role |
| --- | --- |
| `admin` | Administrator |
| `receptionist` | Receptionist |
| `dentist` | Dentist |

Change the demo credentials before using the system with real data.

## Architecture

The system runs as two processes:

- `com.sunrisedental.server.DentalServer` owns database access and exposes JSON endpoints on port `8080`.
- `com.sunrisedental.Main` launches the Swing client.

The client communicates with `http://localhost:8080/api` through `ApiClient`. Controllers coordinate client actions, while DAOs and database operations remain behind the server. Billing uses the Strategy pattern selected by `BillFactory`.

## Running the Application

From the project root, compile the project:

```text
ant clean
ant compile
```

Start the server in one terminal:

```text
ant -Dmain.class=com.sunrisedental.server.DentalServer run
```

Start the desktop client in a second terminal:

```text
ant -Dmain.class=com.sunrisedental.Main run
```

The server must be running before the client attempts to log in.

## User Roles

- **Admin:** dashboard, appointments, patients, billing, staff management, treatment management, revenue reports, and system help.
- **Receptionist:** dashboard, patient registration and directory, appointment booking and directory, billing, and system help.
- **Dentist:** dashboard, personal schedule, appointment decisions, appointment information, and system help.

The Swing interface uses a shared visual theme across forms, tables, dashboards, reports, and role-specific help. The Help screen presents each role's manual as selectable topics and guided instruction cards.

## Verification Commands

```text
ant clean
ant compile
ant test
ant jar
```

Run `ant test` after configuring the required dependencies. Tests that access MySQL require a running and seeded database; isolated controller tests may run without MySQL depending on their mocked collaborators.
