package org.myriad.arcane;

public class Settings {
    public final String PACKAGE_PATH;
    public final String COMMAND_PREFIX;

    public Settings(String package_path, String command_prefix) {
        this.COMMAND_PREFIX = command_prefix;
        this.PACKAGE_PATH = package_path;
    }
}
