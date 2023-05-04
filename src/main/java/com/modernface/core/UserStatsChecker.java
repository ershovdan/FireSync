package com.modernface.core;

import com.modernface.tools.Compress;
import com.modernface.tools.GetDbInfo;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
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
        Statement st = this.conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT id FROM \"List\";");

        int total_amount = 0;
        while (rs.next()) {
            int id = rs.getInt("id");

            Statement st2 = this.conn.createStatement();
            ResultSet rs2 = st2.executeQuery("SELECT COUNT(*) FROM \"ConnectedBuffer\" WHERE id = " + id + " AND time > NOW() - INTERVAL '10 SECONDS';");

            int amount = 0;
            while (rs2.next()) {
                amount = rs2.getInt(1);
            }

            PreparedStatement pst2 = conn.prepareStatement("INSERT INTO \"Connected\" (time, id, amount) VALUES (NOW(), " + id + ", " + amount + ");");
            pst2.execute();
            pst2.close();

            total_amount += amount;
        }

        PreparedStatement pst2 = conn.prepareStatement("INSERT INTO \"Connected\" (time, id, amount) VALUES (NOW(), " + "-1" + ", " + total_amount + ");");
        pst2.execute();
        pst2.close();

        PreparedStatement pst = this.conn.prepareStatement("DELETE FROM \"ConnectedBuffer\" WHERE id > -1;");
        pst.execute();
        pst.close();

        pst = this.conn.prepareStatement("DELETE FROM \"Connected\" WHERE time < NOW() - INTERVAL '120 MINUTES'");
        pst.execute();
        pst.close();
    }
}
