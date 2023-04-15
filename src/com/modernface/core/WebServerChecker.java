package com.modernface.core;

import com.modernface.tools.Compress;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import com.modernface.tools.GetDbInfo;
import com.modernface.tools.GetMainInfo;
import org.json.simple.parser.ParseException;

public class WebServerChecker {
    Path pathToData;
    Connection conn;

    public WebServerChecker(Connection con) throws URISyntaxException, SQLException {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.conn = con;
    }

    public void forRightMenu() throws IOException, SQLException, ParseException {
        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        Statement st = this.conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"Files\" WHERE status > 0;");
        int active_shares = rs.getInt(1);
        PreparedStatement pst = this.conn.prepareStatement("UPDATE \"Other\" SET value_int = " + active_shares + " WHERE type = 'active_shares';");
        pst.execute();
        st.close();
    }

    public void startWebServer() throws IOException, InterruptedException, ParseException {
        GetMainInfo getMainInfo = new GetMainInfo();

        String cmd = "lsof -t -i tcp:" + getMainInfo.getCFG("web_server_port") + "  | xargs kill -9";
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);

        cmd = this.pathToData + "/web_server/env/bin/python " + this.pathToData + "/web_server/fs_web_server/manage.py runserver 0.0.0.0:" + getMainInfo.getCFG("web_server_port");
        run = Runtime.getRuntime();
        pr = run.exec(cmd);
    }

    public void checkList() throws SQLException, URISyntaxException, IOException, ParseException {
        Statement st = this.conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM \"List\" WHERE status = 5;");

        while (rs.next()) {
            String path = rs.getString("path");
            int id = rs.getInt("id");

            Random ran = new Random();
            String key = Integer.toHexString(ran.nextInt(2147483647)) + Integer.toHexString(ran.nextInt(2147483647));
            key = "0".repeat(16 - key.length()) + key;

            st = this.conn.createStatement();
            st.executeUpdate("UPDATE \"List\" SET \"key\" = '" + key + "' WHERE \"id\" = " + id + ";");
            st.close();

            String name = key + "op" + id;
            File dir = new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", name)));
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", name  + ".json")));
            if(!file.exists()) {
                file.createNewFile();
            }

            Compress compress = new Compress(path, key, id);
            compress.initJSON(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", name  + ".json")));

            st = this.conn.createStatement();
            st.executeUpdate("UPDATE \"List\" SET \"status\" = 4 WHERE \"id\" = " + id + ";");
            st.close();
        }
    }
}
