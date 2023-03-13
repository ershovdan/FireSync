package com.modernface.core;

import com.modernface.tools.Compress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Random;

import org.json.simple.JSONObject;

public class WebServerChecker {
    Path pathBase;
    Path pathBaseParent;

    public WebServerChecker() throws URISyntaxException {
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
    }

    public void startWebServer() throws IOException, InterruptedException {
        String cmd = "/Users/ershovdan/Programming/FireSync/out/production/web-server/env/bin/python /Users/ershovdan/Programming/FireSync/out/production/web-server/fs_web_server/manage.py runserver";
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);



        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while ((line=buf.readLine())!=null) {
            System.out.println(line);
        }
    }

    public void checkList() throws SQLException, URISyntaxException, IOException {
        String url = "jdbc:sqlite:" + this.pathBaseParent + "/db/operations.db";
        Connection con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT lastID FROM WebServerChecker ORDER BY lastID DESC LIMIT 1;");
        int lastID = 0;
        while (rs.next()) {
            lastID = rs.getInt("lastID");
        }

        System.out.println(lastID);

        rs = stmt.executeQuery("SELECT * FROM List;");
        while (rs.next()) {
            if (rs.getInt("id") > lastID) {
                System.out.println(">");

                PreparedStatement pstmt = con.prepareStatement("INSERT INTO WebServerChecker(lastID) VALUES(?);");
                pstmt.setInt(1, lastID + 1);
                pstmt.executeUpdate();

                Random ran = new Random();
                String key = Integer.toHexString(ran.nextInt(2147483647)) + Integer.toHexString(ran.nextInt(2147483647));
                Compress compress = new Compress(rs.getString("path"), ("0".repeat(16 - key.length()) + key));
                compress.zipAll("op" + rs.getInt("id"));
            }
        }

        con.close();
    }
}
