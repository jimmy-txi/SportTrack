package fr.utc.miage.sporttrack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SporttrackApplication {

    public static void main(String[] args) {
        try {
            Path dbDir = Paths.get("/opt/sporttrack/data/db");
            Files.createDirectories(dbDir);

            Path dbFile = dbDir.resolve("app.db");
            if (Files.notExists(dbFile)) {
                Files.createFile(dbFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier/fichier SQLite", e);
        }

        SpringApplication.run(SporttrackApplication.class, args);
    }
}