# Document Manager Implementation Plan

## Summary
Backend-first completion, then JavaFX integration, aligned with [GUIDE.md](GUIDE.md) and your clarifications.

## Locked Decisions
- Scope: Core-first, then full requirements closure.
- Build tool: Maven.
- UI layout: `BorderPane` with always-visible summary panel + center `TabPane`.
- Persistence: normalized JSON under `medialab/`.
- User edit: full domain capability (admin can edit role/password/allowed categories).

## Phase 1: Stabilize Build and Codebase
1. Add `pom.xml` (JavaFX, Gson, JUnit 5).
2. Fix compile blockers (`UserRepository` truncation, model mismatches).
3. Verify clean `mvn test` / `mvn package` baseline.

## Phase 2: Domain Model
1. Finalize user hierarchy: `SimpleUser`, `Author`, `Admin`.
2. Define `Document` with stable id + metadata (`title`, `author`, `category`, `createdAt`).
3. Define `DocumentVersion` with `versionNumber` + `content`.
4. Add watch state per user (followed docs + last seen version).

## Phase 3: Persistence (A.2)
1. Implement repositories for users/documents/categories.
2. Load all data at startup into memory.
3. Persist all state only at shutdown.
4. Ensure default admin bootstrap: `medialab` / `medialab_2025`.

## Phase 4: Backend Services
1. `AuthService`: login/session.
2. `DocumentService`: create/edit/delete/search/view with role rules.
3. Version visibility:
   - Simple user: latest only.
   - Author/Admin: latest + up to 2 previous versions.
4. `WatchService`: follow/unfollow + updated-since-last-login logic.
5. `AdminService`: user add/delete/edit and category add/rename/delete with cascading cleanup.

## Phase 5: Terminal Smoke Runner
1. Keep a simple backend `Main` for quick scenario validation.
2. No command-framework expansion; debugging-only utility.

## Phase 6: JavaFX UI (A.3)
1. Login screen + post-login popup for updated followed documents.
2. Main window title: `MediaLab Documents`.
3. Always-visible summary panel with live values:
   - total categories
   - total distinct documents
   - watched docs of logged-in user
4. Center tabs: Documents, Search, Watchlist, Users (admin), Categories (admin).
5. Save state on application exit.

## Phase 7: Quality and Delivery
1. Provide one class with full Javadoc on all public methods (A.4 requirement).
2. Add tests for permissions, versioning, cascades, and persistence round-trip.
3. Prepare final short report (design, JSON schema, assumptions, missing features if any).

## Required Test Scenarios
1. Default admin creation on empty data.
2. Role-based access enforcement.
3. Edit creates new version and preserves history.
4. Delete document/category cleans related watches.
5. User edit updates role/password/categories correctly.
6. Search by title/author/category.
7. Follow/unfollow + new-version notification.
8. Startup-load and shutdown-save consistency.

## Assumptions
- JSON data folder remains `medialab/`.
- Password storage stays plaintext unless explicitly changed.
- `createdAt` is required on document; per-version timestamps are optional and not required.
