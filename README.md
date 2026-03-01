# Docflow - Document Manager App

JavaFX document management application with role-based access, document versioning, and JSON persistence.

## Tech Stack

- Java 17
- Maven
- JavaFX
- Gson
- JUnit 5

## Requirements

- Java 17 or newer
- Maven

## Build

```bash
mvn clean compile
```

## Run

```bash
mvn javafx:run
```

## Test

```bash
mvn test
```

## Default Admin Login

- Username: `medialab`
- Password: `medialab_2025`

## Project Structure

- `src/main/java/com/docflow` - application source code
- `src/main/java/com/docflow/model` - domain model
- `src/main/java/com/docflow/repository` - JSON persistence
- `src/main/java/com/docflow/service` - business logic
- `src/main/java/com/docflow/controller` - JavaFX controllers
- `src/main/resources/fxml` - JavaFX views
- `src/main/resources/css` - styling
- `medialab/*.json` - persisted application data

## Core Features

- Role-based access (`SIMPLE_USER`, `AUTHOR`, `ADMIN`)
- Document creation, editing, deletion
- Versioning per document
- Follow/unfollow documents and update notifications
- Search by title, author, and category
- Category and user management (admin)

## Data Persistence

- Application state is loaded from JSON files on startup.
- All changes are performed in-memory.
- The complete state is persisted back to JSON on application shutdown.

## Javadoc Coverage

- `com.docflow.service.AuthService`
- `com.docflow.service.WatchService`

## Notes

- Passwords are stored in plain text JSON for assignment purposes only.
- An `AUTO_LOGIN_AS_ADMIN` flag is available for development convenience.
