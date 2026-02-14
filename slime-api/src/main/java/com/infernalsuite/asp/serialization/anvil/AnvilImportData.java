package com.infernalsuite.asp.serialization.anvil;

import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.io.File;

public class AnvilImportData {

    // Fields for the data
    private final File worldDir;
    private final String worldName;
    private final SlimeLoader loader;

    public AnvilImportData(File worldDir, String worldName, SlimeLoader loader) {
        this.worldDir = worldDir;
        this.worldName = worldName;
        this.loader = loader;
    }

    public static AnvilImportData legacy(File worldDir, String worldName, SlimeLoader loader) {
        return new AnvilImportData(worldDir, worldName, loader);
    }

    // Getters
    public File getWorldDir() {
        return worldDir;
    }

    public String getWorldName() {
        return worldName;
    }

    public SlimeLoader getLoader() {
        return loader;
    }
}
