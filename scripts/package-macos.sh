#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INPUT_DIR="$ROOT_DIR/target/jpackage-input"
DIST_DIR="$ROOT_DIR/dist"

cd "$ROOT_DIR"

VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
VERSION="${VERSION%-SNAPSHOT}"

mvn clean package
mkdir -p "$INPUT_DIR" "$DIST_DIR"
mvn dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory="$INPUT_DIR"
cp "$ROOT_DIR/target/docflow.jar" "$INPUT_DIR/"

jpackage \
    --type dmg \
    --name "DocFlow" \
    --app-version "$VERSION" \
    --vendor "DocFlow" \
    --description "Document management desktop application" \
    --input "$INPUT_DIR" \
    --main-jar "docflow.jar" \
    --main-class "com.docflow.Launcher" \
    --mac-package-identifier "com.docflow.documentmanager" \
    --mac-package-name "DocFlow" \
    --dest "$DIST_DIR"

echo "Created $DIST_DIR/DocFlow-$VERSION.dmg"
