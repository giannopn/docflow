# Repository Guidelines

## Project Structure & Module Organization
This is a Java 17 Maven project for a document manager app.
- Source code: `src/main/java/com/docflow`
- Domain models: `src/main/java/com/docflow/model`
- Persistence logic: `src/main/java/com/docflow/repository`
- JavaFX controllers/UI bootstrap: `src/main/java/com/docflow/controller`, `src/main/java/com/docflow/ui`
- UI assets: `src/main/resources/fxml` and `src/main/resources/css`
- JSON data files: `medialab/documents.json`, `medialab/users.json`
- Planning/spec docs: `GUIDE.md`, `IMPLEMENTATION_PLAN.md`

## Build, Test, and Development Commands
- `mvn clean compile`: compile all main sources.
- `mvn test`: run test suite (JUnit 5).
- `mvn -DskipTests package`: build JAR quickly without tests.
- `mvn javafx:run`: launch JavaFX app (`com.docflow.ui.Main`).
- IDE backend smoke run: execute `com.docflow.Main` to verify JSON loading in terminal.

## Coding Style & Naming Conventions
- Use 4-space indentation and UTF-8 text files.
- Java naming: `PascalCase` for classes, `camelCase` for methods/fields, `UPPER_SNAKE_CASE` for constants.
- Keep package organization by layer (`model`, `repository`, `controller`, `ui`).
- Keep repository classes focused on JSON/file persistence; avoid mixing UI code there.
- Keep methods short and single-purpose (for example, `load`, `save`, `findByUsername`).

## Testing Guidelines
- Framework: JUnit 5 (configured in `pom.xml`).
- Place tests in `src/test/java/com/docflow/...` mirroring production packages.
- Naming: class `<ClassName>Test`, method `should<ExpectedBehavior>()`.
- Prioritize tests for JSON parsing, repository behavior, default admin bootstrap, and role-based rules.

## Commit & Pull Request Guidelines
- Follow concise imperative commit messages (seen in history: “Add…”, “Refactor…”, “Enhance…”).
- Keep one logical change per commit.
- PRs should include: short summary, affected packages/files, manual verification steps, and screenshots for UI changes.
- Mention any schema/data changes under `medialab/` explicitly.

## Configuration & Security Notes
- Do not commit machine-specific paths (JavaFX SDK or local library paths).
- Keep generated output (`target/`) out of commits.
- Treat credentials in JSON as development-only; avoid real passwords.
