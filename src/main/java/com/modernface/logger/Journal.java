package com.modernface.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {
    Path pathToData;

    public Journal(String mode) {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.pathToData.toString(), "logs", "fs.journal"), StandardOpenOption.APPEND)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            writer.write("FireSync started. Time: " + dtf.format(now) + ". Mode: " + mode + "\n");
        } catch (IOException ioe) {}
    }
}
