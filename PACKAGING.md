# Packaging DocFlow

Native packages include Java and JavaFX, so users do not need to install a
Java runtime.

## macOS

Requirements on the build machine:

- A JDK that provides `jpackage`
- Maven

Build the DMG:

```bash
./scripts/package-macos.sh
```

The package is written to `dist/`. Install it by opening the DMG and dragging
DocFlow into Applications.

Packaged application data is stored in:

```text
~/Library/Application Support/DocFlow
```

The generated application is unsigned. For public distribution, it should be
signed with an Apple Developer ID certificate and notarized to avoid Gatekeeper
warnings.
