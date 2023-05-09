package com.modernface.core;

import com.modernface.logger.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class BufferChecker {
    Path pathToData;
    Connection conn;
    Logger logger;

    BufferChecker(Connection con, Logger log) {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.conn = con;
        this.logger = log;
    }

    public void check() throws IOException, ParseException, SQLException {
        File bufferZippedDir = new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped")));

        ArrayList<File> listedFiles = new ArrayList<>(FileUtils.listFilesAndDirs(bufferZippedDir, TrueFileFilter.TRUE, null));

        for (File i : listedFiles) {
            if (i.getName().contains(".json")) {
                String key = i.getName().substring(0, 16);
                String id = i.getName().substring(18);

                if (id.contains(".")) {
                    id = id.substring(0, id.indexOf("."));
                }

                Statement st = this.conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM \"List\" WHERE id = " + id + ";");

                int status = -1;

                while (rs.next()) {
                    status = rs.getInt("status");
                }

                if (status <= 0) {
                    if (i.isDirectory()) {
                        FileUtils.deleteDirectory(i);
                    } else {
                        FileUtils.delete(i);
                    }

                    this.logger.info("share with id=" + id + " was removed");
                }

                rs.close();
                st.close();
            }
        }
    }
}
