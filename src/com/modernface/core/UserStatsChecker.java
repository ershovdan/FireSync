package com.modernface.core;

import com.modernface.tools.Compress;
import com.modernface.tools.GetDbInfo;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

public class UserStatsChecker {
    Path pathBaseParent;
    Path pathToData;
    Connection conn;

    public UserStatsChecker(Connection con) throws URISyntaxException {
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.conn = con;
    }

    public void amountOfConnected() throws IOException, ParseException, SQLException {
        int total = 3;

        PreparedStatement st = this.conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), 1, 24);");
        st.execute();
        st.close();

        st = this.conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), 2, 25);");
        st.execute();
        st.close();

        st = this.conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), " + total + ", -1);");
        st.execute();
        st.close();

        st = this.conn.prepareStatement("DELETE FROM \"Connected\" WHERE time < UNIX_TIMESTAMP(DATE_SUB(NOW() - INTERVAL '1 DAY'))");
        st.execute();
        st.close();
    }
}
