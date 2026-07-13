package com.docflow.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

final class DataDirectory {

    private static final String DEVELOPMENT_DIRECTORY = "medialab";
    private static final String APPLICATION_DIRECTORY = "DocFlow";

    private DataDirectory() {}

    static Path resolve() {
        if (System.getProperty("jpackage.app-path") == null) {
            return Paths.get(DEVELOPMENT_DIRECTORY);
        }

        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (osName.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", APPLICATION_DIRECTORY);
        }
        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Paths.get(appData, APPLICATION_DIRECTORY);
            }
            return Paths.get(userHome, "AppData", "Roaming", APPLICATION_DIRECTORY);
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Paths.get(xdgDataHome, APPLICATION_DIRECTORY);
        }
        return Paths.get(userHome, ".local", "share", APPLICATION_DIRECTORY);
    }
}
