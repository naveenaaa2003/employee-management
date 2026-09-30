# Employee Management System

A full-stack CRUD application for managing employee records. A Spring Boot REST API backed by PostgreSQL (via Hibernate/JPA), with a browser UI and interactive Swagger docs.

## Features 

- Add, view, update and delete employees 
- Search by name (partial, case-insensitive) and filter by department or salary range 
- Department list and per-department headcount 
- Paginated listing (`page` and `size` parameters) 
- Bean Validation (blank name, invalid email, and so on) with clean JSON errors: `400` validation, `404` not found, `409` duplicate email
- HTTP Basic authentication: reads are public, writes (POST, PUT, DELETE) require login
- Single-page frontend (HTML, CSS, JavaScript) served by Spring Boot 
- Swagger UI for interactive API documentation 
- 17 automated integration tests (JUnit 5 and MockMvc) 

## Tech Stack 

Java 17, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Hibernate, Spring Security, PostgreSQL 18, springdoc-openapi (Swagger UI), Maven, JUnit 5 and MockMvc. 

## Prerequisites 

- Java 17 or later 
- PostgreSQL 18 running on `localhost:5432` 
- No separate Maven install needed; the Maven Wrapper (`mvnw`) is included 

## Setup 

1. Create two databases, one for the app and one for the tests: 

   ```sql
   CREATE DATABASE employee_mgmt_v2; 
   CREATE DATABASE employee_mgmt_test; 
   ```

2. Check the connection details in `src/main/resources/application.properties` (default user `postgres`, password `root`) and change them to match your PostgreSQL install.

3. Tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

## Run the application

```
mvnw.cmd spring-boot:run
```

On macOS or Linux use `./mvnw spring-boot:run`.

- Web UI: http://localhost:8080/
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Run the tests

```
mvnw.cmd test
```

The tests run with the `test` Spring profile (`src/test/resources/application-test.properties`), which points at a separate `employee_mgmt_test` database that is cleared after each test, so your real data is never touched.

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/employees` | Add an employee | Required |
| GET | `/employees?page=0&size=10` | List employees (paginated) | Public |
| GET | `/employees/{id}` | Get one employee | Public |
| PUT | `/employees/{id}` | Update an employee | Required |
| DELETE | `/employees/{id}` | Delete an employee | Required |
| GET | `/employees/department/{department}` | Filter by department | Public |
| GET | `/employees/search?name=...` | Search by name | Public |
| GET | `/employees/salary-range?min=...&max=...` | Filter by salary range | Public |
| GET | `/employees/departments` | List distinct departments | Public |
| GET | `/employees/department/{department}/count` | Headcount for a department | Public |

## Authentication

Write endpoints use HTTP Basic auth. Demo credentials: `admin` / `admin123`.

```
curl -u admin:admin123 -X POST http://localhost:8080/employees -H "Content-Type: application/json" -d "{\"name\":\"Priya Sharma\",\"email\":\"priya@company.com\",\"department\":\"Engineering\",\"salary\":65000}"
```

In Swagger UI, click **Authorize** and enter the same credentials.

## Project Structure 

```
src/main/java/com/naveena/employeemanagement/
  entity/       Employee (JPA entity and validation rules)
  repository/   EmployeeRepository (Spring Data JPA)
  service/      EmployeeService (business logic)
  controller/   EmployeeController (REST endpoints)
  exception/    GlobalExceptionHandler (structured error responses)
  config/       SecurityConfig, OpenApiConfig
src/main/resources/static/index.html   Frontend
src/test/java/...                      Integration tests
```

## Known Limitations

- The credentials are a single in-memory demo user kept in `application.properties`. A production system would store hashed passwords in the database, and would not commit secrets to source control.
- The frontend loads the first 50 employees and has no page controls yet, although the API itself is fully paginated.