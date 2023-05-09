package com.modernface.core;

import com.modernface.logger.Logger;
import com.modernface.tools.GetDbInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timeout {
    int time;
    int lowFreq;
    Path pathToData;
    Connection conn;
    Logger logger;

    public Timeout(int time, int lowFreq , Logger log) throws SQLException, IOException, ParseException {
        this.time = time;
        this.lowFreq = lowFreq;
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.logger = log;
        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        String url = "jdbc:postgresql://" + DBdata.get("host") + ":" + DBdata.get("port") + "/" + DBdata.get("name");
        Properties props = new Properties();
        props.setProperty("user", DBdata.get("user"));
        props.setProperty("password", DBdata.get("password"));
        this.conn = DriverManager.getConnection(url, props);
    }

    public void start() throws URISyntaxException, SQLException, IOException, ParseException {
        Runnable task = new Runnable() {
            public void run() {
                try {
                    WebServerChecker webServerChecker = new WebServerChecker(conn, logger);
                    webServerChecker.checkList();
                    webServerChecker.forRightMenu();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.time, TimeUnit.MILLISECONDS);
    }

    public void startLowFreq() {
        Runnable task = new Runnable() {
            public void run() {
                try {
                    UserStatsChecker userStatsChecker = new UserStatsChecker(conn);
                    userStatsChecker.amountOfConnected();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.lowFreq, TimeUnit.MILLISECONDS);
    }

    public void startLowFreqNetworkUsage() {
        Runnable task = new Runnable() {
            private Path pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");;

            public void run() {
                try {
                    GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(pathToData), "cfg", "db.cfg")));
                    HashMap<String, String> DBdata = dbInfo.getCFG();

                    FileChangesChecker fileChangesChecker = new FileChangesChecker(conn, logger);
                    fileChangesChecker.check();

                    BufferChecker bufferChecker = new BufferChecker(conn, logger);
                    bufferChecker.check();

                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(Files.readString(Path.of("/Users/ershovdan/FireSyncData/db/network_usage_final.txt")));

                    PreparedStatement st = conn.prepareStatement("INSERT INTO \"Network\" (time, data) VALUES (NOW(), " + jsonObject.get("data") + ");");
                    st.execute();
                    st.close();

                    st = conn.prepareStatement("DELETE FROM \"Network\" WHERE time < now() - interval '150 minutes';");
                    st.execute();
                    st.close();

                    NetworkUsageChecker networkUsageChecker = new NetworkUsageChecker(conn, logger);
                    networkUsageChecker.check();
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.lowFreq, TimeUnit.MILLISECONDS);
    }
}
