package com.modernface.core;

import com.modernface.tools.GetMainInfo;
import org.json.simple.parser.ParseException;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class NetworkUsageChecker {
    Path pathToData;

    NetworkUsageChecker() {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }

    private int getInfoIndexes(String row) {
        try {
            row = row.substring(16);
            row = row.replaceAll("\\s+","").toLowerCase();
            row = row.substring(row.indexOf("b") + 1);
            row = row.substring(0, row.lastIndexOf("b")) + row.substring(row.lastIndexOf("b") + 1);
            row = row.substring(0, row.indexOf("b"));
        } catch (Exception exc) {
            return 0;
        }

        
        int data = 0;
        
        switch (row.substring(row.length() - 1)) {
            case "k" -> {
                data = (int) Double.parseDouble(row.substring(0, row.length() - 1));
            }
            case "m" -> {
                data = ((int) Double.parseDouble(row.substring(0, row.length() - 1))) * 1024;
            }
            case "g" -> {
                data = ((int) Double.parseDouble(row.substring(0, row.length() - 1))) * 1024 * 1024;
            }
            default -> {
                data = (int) Double.parseDouble(row) / 1024;
            }
        }

        return (int) data;
    }

    private void updateShellFile() throws IOException, ParseException {
        GetMainInfo getMainInfo = new GetMainInfo();

        String command = "iftop -i en0 -P -t -s10 -f \"src port " + getMainInfo.getCFG("web_server_port") + "\" 2> /dev/null| grep \"Total send rate:\" > " + Paths.get(this.pathToData.toString(), "db", "network_usage.txt");

        Files.writeString(Paths.get(String.valueOf(Paths.get(this.pathToData.toString(), "scripts", "network_usage.sh"))), command);
    }

    public void check() throws IOException, ParseException {
        this.updateShellFile();

        ProcessBuilder pr = new ProcessBuilder("sh", Paths.get(this.pathToData.toString(), "scripts", "network_usage.sh").toString());
        Process p = pr.start();

        String str = Files.readString(Paths.get(String.valueOf(this.pathToData), "db", "network_usage.txt"));

        Path fin = Paths.get(String.valueOf(this.pathToData), "db", "network_usage_final.txt");
        Files.writeString(fin, "{\"data\": \"" + String.valueOf(this.getInfoIndexes(str)) + "\", \"key\": \"" + "" + "\"}");
    }
}
