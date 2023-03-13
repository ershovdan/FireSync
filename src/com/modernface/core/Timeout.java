package com.modernface.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timeout {
    int time;
    public Timeout(int time) {
        this.time = time;
    }
    public void start() throws URISyntaxException, SQLException, IOException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
        executor.schedule(() -> {
            try {
                this.start();
            } catch (Exception exc) {}
        }, this.time, TimeUnit.MILLISECONDS);

        WebServerChecker checker = new WebServerChecker();
        checker.checkList();
        checker = null;
    }
}
