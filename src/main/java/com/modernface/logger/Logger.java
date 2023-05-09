package com.modernface.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    Path pathToData;
    String logName;

    public Logger() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.logName = dtf.format(now) + ".log";

        File f = new File(String.valueOf(Paths.get(this.pathToData.toString(), "logs", "general", this.logName)));
        f.createNewFile();
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Files.writeString(Paths.get(this.pathToData.toString(), "logs", "general", this.logName), "This FireSync log started at " + dtf.format(now) + " machine time.\n\n");
    }

    private void writer(String type, String message) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.pathToData.toString(), "logs", "general", this.logName), StandardOpenOption.APPEND)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            writer.write("[" + type +  "] " + dtf.format(now) + " - " + message + "\n");
        } catch (IOException ioe) {}
    }

    public void info(String text) {
        this.writer("INFO", text);
    }

    public void warning(String text) {
        this.writer("WARNING", text);
    }

    public void error(String text) {
        this.writer("ERROR", text);
    }

    public void fatal(String text) {
        this.writer("FATAL", text);
    }
}
