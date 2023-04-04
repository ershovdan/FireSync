package com.modernface.tools;

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

public class UserStats {
    Path pathBaseParent;
    Path pathToData;

    public UserStats() throws URISyntaxException {
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }

    public void amountOfConnected() throws IOException, ParseException, SQLException {
        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
        Properties props = new Properties();
        props.setProperty("user", DBdata.get("user"));
        props.setProperty("password", DBdata.get("password"));
        Connection conn = DriverManager.getConnection(url, props);

        int total = 3;

        PreparedStatement st = conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), 1, 24);");
        st.execute();
        st.close();

        st = conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), 2, 25);");
        st.execute();
        st.close();

        st = conn.prepareStatement("INSERT INTO \"Connected\" (time, amount, id) VALUES (NOW(), " + total + ", -1);");
        st.execute();
        st.close();

        st = conn.prepareStatement("DELETE FROM \"Connected\" WHERE time < UNIX_TIMESTAMP(DATE_SUB(NOW() - INTERVAL '1 DAY'))");
        st.execute();
        st.close();

        conn.close();
    }
}
