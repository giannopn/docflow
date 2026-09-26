# DocFlow

A desktop document manager built with JavaFX. Organize documents by category,
keep version history, and follow changes through a role-based interface for
administrators, authors, and readers.

<img width="1392" height="924" alt="SCR-20260926-oisv" src="https://github.com/user-attachments/assets/c5b0c8b6-f14d-4abb-b5a0-5e0efc2ba769" />

<img width="1392" height="924" alt="SCR-20260926-oknc" src="https://github.com/user-attachments/assets/ea8afe5f-0225-4a94-9594-5c716d06bcf4" />

## Features

- Role-based access to documents and categories.
- Document creation, editing, and deletion for authorized users.
- Version history: readers see the latest version; authors and administrators
  can view the latest three versions.
- Follow documents and receive update notifications when signing in.
- Search by title, author, and category.
- User and category management for administrators.
- Local JSON storage with no database server required.

## Download

Download the latest macOS version from the
[GitHub Releases page](https://github.com/giannopn/docflow/releases/latest).

1. Download and open the release's `.dmg` file.
2. Drag DocFlow into **Applications**.
3. Launch DocFlow from **Applications**.

The current build supports **Apple Silicon Macs** and includes Java and JavaFX.
No separate runtime installation is required.

> The application is currently unsigned. If macOS blocks it, open
> **System Settings → Privacy & Security**, scroll to **Security**, and click
> **Open Anyway** for DocFlow. Authenticate and confirm opening the app if prompted.

## Demo Login

Use the default administrator account to sign in:

- Username: `medialab`
- Password: `medialab_2025`

A fresh installation starts with this account and no documents or categories.
Running from source includes the sample users, categories, and documents in
`medialab/` so you can explore the app with demo data.

## Run From Source

Development requires **JDK 17 or newer**, **Maven**, and Git to clone the repository.
These tools are not required to use the packaged macOS app.

```bash
git clone https://github.com/giannopn/docflow.git
cd docflow
mvn javafx:run
```

Run these commands from the repository root to compile or execute the test suite:

```bash
mvn clean compile
mvn test
```

The project uses Java 17, JavaFX, Gson for JSON serialization, Maven for builds,
and JUnit 5 for tests.

See the [packaging guide](docs/PACKAGING.md) to build a macOS installer.

## Architecture and Storage

Source code is organized under `src/main/java/com/docflow`:

| Layer | Responsibility |
| --- | --- |
| `model` | Users, roles, documents, and versions |
| `repository` | JSON loading and saving |
| `service` | Authentication, permissions, document operations, and notifications |
| `controller` and `ui` | JavaFX interactions and shared styling |

FXML views and CSS live in `src/main/resources`; tests mirror the production
packages under `src/test/java`.

Data is loaded at startup, kept in memory during use, and saved when the
application closes normally. Development and installed-app data are separate:

| Run mode | Data location |
| --- | --- |
| From the repository root | `medialab/` |
| Packaged macOS app | `~/Library/Application Support/DocFlow/` |

The macOS installer does not include the repository's demo JSON files.

## Limitations

- Passwords are stored in plaintext for demonstration purposes. Use demo
  credentials only; the authentication system is not intended for production use.
- Unsaved changes can be lost if the application crashes or is force-quit.
- The distributed installer currently targets macOS on Apple Silicon and is
  not signed or notarized.
