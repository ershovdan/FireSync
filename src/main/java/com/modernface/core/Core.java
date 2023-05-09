package com.modernface.core;

import com.modernface.logger.Journal;
import com.modernface.logger.Logger;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;


public class Core {
    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException, ParseException {
        Init init = new Init();
        init.initDirs(Arrays.asList(args).contains("--sinit"));

        Logger logger = new Logger();

        init.initDB(logger);

        if ((args.length == 0) || Arrays.asList(args).contains("--sinit")) {
            WebServerChecker ch = new WebServerChecker(null, logger);
            ch.startWebServer();

            Timeout tm = new Timeout(800, 10000, logger);
            tm.start();
            logger.info("high freq schedule started");
            tm.startLowFreq();
            tm.startLowFreqNetworkUsage();
            logger.info("low freq schedule started");

            if (Arrays.asList(args).contains("--sinit")) {
                new Journal("simplified initialization");
            } else {
                new Journal("default");
            }
        } else {
            if (Arrays.asList(args).contains("--nogui")) {

            }
        }
    }
}
