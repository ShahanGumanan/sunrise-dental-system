# Sunrise Dental System

Distributed Java Swing client and HTTP web service for dental appointment, patient, treatment, billing, and staff management.

## Technology

- Java 17
- Maven
- Java Swing
- MySQL 8 via JDBC
- JDK HttpServer REST-style web services with Gson JSON
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

The system runs as two processes. `DentalServer` owns the database and DAO layer and exposes JSON HTTP endpoints on port 8080. The Swing/JFrame client sends requests through `ApiClient`; its controllers validate requests and act as network proxies. Fee calculation uses the Strategy pattern selected by the Bill Factory.

## Running the distributed system

Start MySQL and create/seed the database, then use two terminals from the project root:

```text
mvn exec:java "-Dexec.mainClass=com.sunrisedental.server.DentalServer"
mvn exec:java "-Dexec.mainClass=com.sunrisedental.Main"
```

The server must be running before the client logs in. The server endpoint is `http://localhost:8080/api`.

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
