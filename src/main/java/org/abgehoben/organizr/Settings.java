package org.abgehoben.organizr;

import org.abgehoben.organizr.enums.MetadataSource;
import org.abgehoben.organizr.enums.MovingScheme;
import org.abgehoben.organizr.enums.SortingScheme;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.abgehoben.organizr.main.*;

public class Settings {
    // Default values
    public boolean includeSubdirs = true;
    public boolean useSymlinks = false;
    public boolean keepDate = true;
    public boolean splitArtists = true;
    public boolean useEmbeddedTitle = false;
    public MovingScheme movingScheme = MovingScheme.COPY_FILES;
    public SortingScheme sortingScheme = SortingScheme.ARTIST_ALBUM;
    public MetadataSource metadataSource = MetadataSource.FILE_METADATA; //Not implemented yet

    public String inputDir;
    public String outputDir;

    public Boolean darkMode = false;

    public static Settings loadSettings() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> tag.getClassName().equals(Settings.class.getName()));

        Yaml yaml = new Yaml(new Constructor(Settings.class, loaderOptions));
        if (!Files.exists(CONFIG_PATH)) {
            return createSettingsFile(yaml);
        } else {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                return yaml.loadAs(input, Settings.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static Settings createSettingsFile(Yaml yaml) {
        try {
            Files.createDirectories(APP_DIR);
            Files.createFile(CONFIG_PATH);
            Writer writer = Files.newBufferedWriter(CONFIG_PATH);
            Settings defaults = new Settings();
            yaml.dump(defaults, writer);
            return defaults;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create settings file", e);
        }
    }

    public void saveSettings() {
        Yaml yaml = new Yaml();
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            yaml.dump(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    public boolean isReady() {
        return inputDir != null && !inputDir.isEmpty() &&
               outputDir != null && !outputDir.isEmpty();
    }

    public Path getInputPath() {
        return Path.of(inputDir);
    }
    public Path getOutputPath() {
        return Path.of(outputDir);
    }

    public Settings getSettings() {
        Settings copy = new Settings();
        copy.inputDir = inputDir;
        copy.outputDir = outputDir;
        copy.includeSubdirs = includeSubdirs;
        copy.useSymlinks = useSymlinks;
        copy.keepDate = keepDate;
        copy.splitArtists = splitArtists;
        copy.useEmbeddedTitle = useEmbeddedTitle;
        copy.movingScheme = movingScheme;
        copy.sortingScheme = sortingScheme;
        copy.metadataSource = metadataSource;
        return copy;
    }
}
