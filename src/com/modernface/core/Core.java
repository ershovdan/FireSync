package com.modernface.core;

import com.modernface.tools.Compress;
import com.sun.jdi.connect.spi.Connection;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


class Core {
    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException, ParseException {
        Init init = new Init();
        init.initDirs();
        init.initDB();

        if (args.length == 0) {
            WebServerChecker ch = new WebServerChecker(null);
            ch.startWebServer();

            Timeout tm = new Timeout(800, 10000);
            tm.start();
            tm.startLowFreq();
            tm.startLowFreqNetworkUsage();
        } else {
            if (args[0].equals("--nogui")) {

            }
        }
    }
}
