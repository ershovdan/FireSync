package com.modernface.core;

import com.modernface.tools.UserStats;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timeout {
    int time;
    int lowFreq;
    public Timeout(int time, int lowFreq) {
        this.time = time;
        this.lowFreq = lowFreq;
    }
    public void start() throws URISyntaxException, SQLException, IOException, ParseException {
        Runnable task = new Runnable() {
            public void run() {
                try {
                    WebServerChecker checker = new WebServerChecker();
                    checker.checkList();
                    checker.forRightMenu();
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
                } catch (Exception exc) {}
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, this.lowFreq, TimeUnit.MILLISECONDS);
    }
}
