package it.frafol.cleanstaffchat.hytale.objects;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextFile {

    private final YamlFile yamlFile;

    private static final List<TextFile> list = new ArrayList<>();

    public TextFile(Path path, String fileName, String internal) throws IOException {

        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        Path configPath = path.resolve(fileName);

        if (!Files.exists(configPath)) {
            try (InputStream in = this.getClass().getResourceAsStream("/" + internal)) {
                if (in == null) {
                    throw new IOException("Resource not found in JAR: " + internal);
                }
                Files.copy(in, configPath);
            }
        }

        yamlFile = new YamlFile(configPath.toFile());
        yamlFile.load();
        list.add(this);
    }

    public YamlFile getConfig() {
        return yamlFile;
    }

    public void reload() {
        try {
            yamlFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadAll() {
        list.forEach(TextFile::reload);
    }
}