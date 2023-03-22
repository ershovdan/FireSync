package com.modernface.core;

import com.modernface.tools.Compress;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
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

        WebServerChecker ch = new WebServerChecker();
        ch.startWebServer();

        Timeout tm = new Timeout(800, 10000);
        tm.start();
        tm.startLowFreq();


//        URL website = new URL("https://file-examples.com/storage/fe7e2bfeed6401e75b22832/2017/10/file_example_JPG_500kB.jpg");
//        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
//        FileOutputStream fos = new FileOutputStream("a.jpg");
//        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

//        gui.gui();
    }
}
