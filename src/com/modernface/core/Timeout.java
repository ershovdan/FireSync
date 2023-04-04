package com.modernface.core;

import com.modernface.tools.GetDbInfo;
import com.modernface.tools.UserStats;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timeout {
    int time;
    int lowFreq;
    Path pathToData;
    public Timeout(int time, int lowFreq) {
        this.time = time;
        this.lowFreq = lowFreq;
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }
    public void start() throws URISyntaxException, SQLException, IOException, ParseException {
        Runnable task = new Runnable() {
            public void run() {
                try {
                    WebServerChecker webServerChecker = new WebServerChecker();
                    webServerChecker.checkList();
                    webServerChecker.forRightMenu();

                    SmallZipChecker smallZipChecker = new SmallZipChecker();
                    smallZipChecker.check();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.time, TimeUnit.MILLISECONDS);
    }

    public void startLowFreq() throws URISyntaxException, SQLException, IOException, ParseException {
        Runnable task = new Runnable() {
            public void run() {
                try {
                    UserStats userStats = new UserStats();
                    userStats.amountOfConnected();

                    NetworkUsageChecker networkUsageChecker = new NetworkUsageChecker();
                    networkUsageChecker.check();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.lowFreq, TimeUnit.MILLISECONDS);
    }

    public void startLowFreqNetworkUsage() throws URISyntaxException, SQLException, IOException, ParseException {
        Runnable task = new Runnable() {
            private Path pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");;

            public void run() {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(pathToData), "cfg", "db.cfg")));
                    HashMap<String, String> DBdata = dbInfo.getCFG();

                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(Files.readString(Path.of("/Users/ershovdan/FireSyncData/db/network_usage_final.txt")));

                    System.out.println(jsonObject.get("data"));

                    String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
                    Properties props = new Properties();
                    props.setProperty("user", DBdata.get("user"));
                    props.setProperty("password", DBdata.get("password"));
                    Connection conn = DriverManager.getConnection(url, props);

                    PreparedStatement st = conn.prepareStatement("INSERT INTO \"Network\" (time, data) VALUES (NOW(), " + jsonObject.get("data") + ");");
                    st.execute();
                    st.close();

                    st = conn.prepareStatement("DELETE FROM \"Network\" WHERE time < now() - interval '150 minutes';");
                    st.execute();
                    st.close();

                    conn.close();

                    NetworkUsageChecker networkUsageChecker = new NetworkUsageChecker();
                    networkUsageChecker.check();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.lowFreq, TimeUnit.MILLISECONDS);
    }
}
