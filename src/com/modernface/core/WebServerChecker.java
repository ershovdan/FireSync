package com.modernface.core;

import com.modernface.tools.Compress;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PSQLException;
import org.sqlite.SQLiteException;

public class WebServerChecker {
    Path pathBase;
    Path pathBaseParent;
    Properties props = new Properties();

    public WebServerChecker() throws URISyntaxException, SQLException {
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();

        this.props.setProperty("user", "fsync");
        this.props.setProperty("password", "");
    }


    public void forRightMenu() throws IOException, SQLException {

        String url = "jdbc:sqlite:" + this.pathBaseParent + "/db/operations.db";
        Connection con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM List WHERE status > 0;");
        Files.writeString(Paths.get(this.pathBaseParent + "/db/right_menu/total_shares.txt"), String.valueOf(rs.getInt(1)));

        rs = stmt.executeQuery("SELECT * FROM List WHERE status > 0;");

        while (rs.next()) {
            File file = new File(this.pathBaseParent + "/db/right_menu/status/" + rs.getString("key") + "op" + rs.getInt("id") + ".txt");
            try {
                Files.delete(Path.of(this.pathBaseParent + "/db/right_menu/status/" + rs.getString("key") + "op" + rs.getInt("id") + ".txt"));
            } catch (Exception exc) {}
            
            file.createNewFile();
            Files.writeString(Path.of(this.pathBaseParent + "/db/right_menu/status/" + rs.getString("key") + "op" + rs.getInt("id") + ".txt"), String.valueOf(rs.getInt("status")));

        }

        con.close();
    }

    public void startWebServer() throws IOException, InterruptedException {
        String cmd = "lsof -t -i tcp:8000 | xargs kill -9";
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);

        cmd = this.pathBaseParent + "/web-server/env/bin/python " + this.pathBaseParent + "web-server/fs_web_server/manage.py runserver";
        run = Runtime.getRuntime();
        pr = run.exec(cmd);
    }

    public void checkList() throws SQLException, URISyntaxException, IOException, ParseException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/FireSync", this.props);

        int lastID = 0;

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT \"lastID\" FROM \"lastID\";");
        while (rs.next()) {
            lastID = rs.getInt("lastID");
        }
        rs.close();
        st.close();

        HashMap<String, String> namesForZip = new HashMap<>();
        ArrayList<Integer> indexes = new ArrayList<>();

        int id;

        st = conn.createStatement();
        rs = st.executeQuery("SELECT * FROM public.\"List\";");
        try {
            while (rs.next()) {
                id = rs.getInt("id");
                if (id > lastID) {
                    String path = rs.getString("path");
                    int newID = lastID + 1;
                    if (newID > lastID) {
                        Statement pst = conn.createStatement();
                        pst.executeUpdate("UPDATE \"lastID\" SET \"lastID\" = " + id + " WHERE \"lastID\" = " + lastID + ";");
                        pst.close();
                    }

                    Random ran = new Random();
                    String key = Integer.toHexString(ran.nextInt(2147483647)) + Integer.toHexString(ran.nextInt(2147483647));
                    key = "0".repeat(16 - key.length()) + key;

                    Statement pst = conn.createStatement();
                    st.executeUpdate("UPDATE \"List\" SET \"key\" = '" + key + "' WHERE \"id\" = " + id + ";");
                    pst.close();

                    namesForZip.put(key, path);
                    indexes.add(id);
                }
            }
        } catch (PSQLException exc) {}
        rs.close();
        st.close();
        conn.close();

        int counter = 0;
        for (Map.Entry<String, String> zipName : namesForZip.entrySet()) {
            Compress compress = new Compress(zipName.getValue(), zipName.getKey());
            compress.zipAll("op" + indexes.get(counter));
            counter++;
        }
    }
}
