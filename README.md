# Sunrise Dental System

Java Swing desktop application for dental appointment, patient, treatment, billing, and staff management.

## Technology

- Java 17
- Maven
- Java Swing
- MySQL 8 via JDBC
- JUnit 5 and Mockito
- MVC, DAO, Singleton, Factory, and Strategy patterns

## Setup

1. Install Java 17, Maven, and MySQL/XAMPP.
2. Create the database by running `src/main/resources/schema.sql` in MySQL.
3. Run `src/main/resources/seed.sql` to create demo data.
4. Copy `src/main/resources/db.properties` to a private local configuration if credentials differ. Never commit real credentials.
5. Run `mvn clean test`.
6. Start the application with `mvn exec:java -Dexec.mainClass=com.sunrisedental.Main`.

The seeded demo password is `password` for the `admin`, `receptionist`, and `dentist` accounts. Change demo credentials before using the system with real data.

## Architecture

Swing views send user actions to controllers. Controllers validate input and coordinate DAO operations. DAOs contain prepared SQL and map database rows to model objects. Fee calculation uses the Strategy pattern selected by the Bill Factory.

## Roles

- Admin: full access, staff and treatment administration, revenue reports
- Receptionist: appointments, patients, billing, and daily reports
- Dentist: own schedule and appointment information

## Commands

```text
mvn clean test
mvn clean package
mvn exec:java -Dexec.mainClass=com.sunrisedental.Main
```

Database integration tests require a configured MySQL database. Unit tests do not require MySQL to be running.
