# Quiz Portal Server

Backend API for a classroom-based quiz portal. Built with Spring Boot, this service supports user authentication, classroom management, PDF-based multiple-choice tests, answer submission, and result retrieval for students and teachers.

## Features

- **Authentication:** registration, login, and password-reset flows.
- **Role-based access:** supports admin, teacher, and student roles.
- **Classroom management:** create classrooms, join or leave with a classroom code, and manage enrollment.
- **PDF-based tests:** create tests from PDF question sheets and provide the correct answer for each question.
- **Test lifecycle:** teachers can start and end tests.
- **Submissions and results:** students can save answers during an active test, submit after it ends, and view their results; authorized teachers can review class submissions.
- **PDF delivery:** enrolled students can access the test PDF when the test is available.
- **Database persistence:** uses Spring Data JPA and Hibernate.
- **Monitoring support:** includes Spring Boot Actuator.

## Tech Stack

| Technology | Purpose |
| --- | --- |
| Java 25 | Application language |
| Spring Boot 3.5.7 | Backend framework |
| Spring Web | REST API |
| Spring Security | Authentication and authorization |
| Spring Data JPA / Hibernate | Database persistence |
| MySQL and PostgreSQL drivers | Relational database connectivity |
| Apache PDFBox 3.0.6 | PDF processing |
| Maven Wrapper | Build and dependency management |

> Both MySQL and PostgreSQL JDBC drivers are included. Configure the application to use the database you intend to run.

## Project Structure

```text
src/
└── main/
    ├── java/com/mcq/server/
    │   ├── config/        # Security and CORS configuration
    │   ├── controller/    # Auth, users, classrooms, tests, submissions
    │   ├── dto/           # Request and response objects
    │   ├── model/         # JPA entities and roles
    │   ├── repository/    # Database access
    │   └── service/       # Authentication, PDF handling, code generation
    └── resources/
        └── application.properties
```

## Prerequisites

- JDK 25
- MySQL or PostgreSQL
- Git

Check your Java version:

```bash
java -version
```

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/om29dev/server.git
cd server
```

### 2. Configure the database

Configure your database connection through environment-specific settings. For a local MySQL setup, an example is:

```properties
spring.application.name=server
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/mcqservertest}
spring.datasource.username=${DB_USERNAME:}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
server.servlet.session.timeout=12h
```

Create the database first, then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your environment. For PostgreSQL, use the appropriate PostgreSQL JDBC URL and credentials.

**Security note:** never commit database passwords or other secrets to source control. If credentials have already been pushed publicly, rotate them and move secrets into environment variables or a secrets manager.

### 3. Run the application

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS/Linux:**

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Spring Boot uses port **8080** by default unless configured otherwise. Check the startup logs to confirm the application and database connection are healthy.

### 4. Build and test

**Windows:**

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

**macOS/Linux:**

```bash
./mvnw clean test
./mvnw clean package
```

## API Overview

The main route groups are listed below. Refer to the controller and DTO classes for exact request bodies and response formats.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Log in and establish a session |
| POST | `/api/auth/forgot-password` | Request a password-reset token |
| POST | `/api/auth/reset-password` | Reset a password using a token |
| GET | `/api/users` | Retrieve user summaries |
| GET | `/api/users/{username}` | Retrieve a user summary |
| GET / POST | `/api/classrooms` | List or create classrooms |
| GET / PUT / DELETE | `/api/classrooms/{code}` | Retrieve, update, or delete a classroom |
| POST | `/api/classrooms/{code}/join` | Join a classroom |
| DELETE | `/api/classrooms/{code}/leave` | Leave a classroom |
| GET | `/api/classrooms/student/active-test` | Find an active test for an enrolled student |
| GET / POST | `/api/classrooms/{classroomCode}/tests` | List tests or create a PDF-based test |
| POST | `/api/classrooms/{classroomCode}/tests/{testname}/start` | Start a test |
| POST | `/api/classrooms/{classroomCode}/tests/{testname}/end` | End a test |
| GET | `/api/classrooms/{classroomCode}/tests/{testname}/pdf` | Retrieve the test PDF |
| GET | `/api/classrooms/{classroomCode}/tests/{testname}/submissions/my` | View the student's result/submission |
| POST | `/api/classrooms/{classroomCode}/tests/{testname}/submissions/update` | Save answers during an active test |
| POST | `/api/classrooms/{classroomCode}/tests/{testname}/submissions/submit` | Submit answers after the test ends |
| GET | `/api/classrooms/{classroomCode}/tests/{testname}/submissions` | View results as an authorized teacher/admin |

Protected classroom and test routes require an authenticated session and the appropriate role or ownership permissions. The login flow uses session-based authentication.

## Typical Test Workflow

1. A teacher or admin creates a classroom.
2. Students join using the classroom code.
3. The teacher creates a test by uploading a PDF and supplying the correct answers.
4. The teacher starts the test, creating submission records for enrolled students.
5. Students access the PDF and save answers while the test is active.
6. The teacher ends the test.
7. Students submit their final answers and view their results; authorized teachers can review class results.

## Configuration Notes

- The current application properties target a local MySQL database. Change the connection settings for your environment.
- CORS is configured for `http://localhost:5173` and `http://127.0.0.1:5173`. Update the allowlist when deploying the frontend elsewhere.
- PDF files are stored on the server filesystem. Ensure the application has read/write access to the storage location and use persistent storage in production.
- Hibernate schema generation is currently set to `update`. Consider managed database migrations for production.
- The current security configuration disables CSRF and uses sessions. Review the security configuration before exposing the API publicly.
- Do not expose database credentials, reset tokens, or other secrets in API responses or logs.

Built as the backend for a classroom-based quiz portal.
