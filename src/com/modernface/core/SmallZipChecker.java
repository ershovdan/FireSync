package com.modernface.core;

import com.modernface.tools.Compress;
import com.modernface.tools.GetDbInfo;
import org.json.simple.parser.ParseException;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class SmallZipChecker {
    Path pathBaseParent;
    Path pathToData;

    SmallZipChecker() throws URISyntaxException {
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }

    public void check() throws SQLException, IOException, ParseException, URISyntaxException {
        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
        Properties props = new Properties();
        props.setProperty("user", DBdata.get("user"));
        props.setProperty("password", DBdata.get("password"));
        Connection conn = DriverManager.getConnection(url, props);

        ArrayList<String> resultDictID = new ArrayList<>();
        HashMap<String, HashMap<String, String>> resultDict = new HashMap<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM public.\"Files\" WHERE status = 1;");
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                String path = rs.getString("path");
                String outKey = rs.getString("out_key");

                resultDictID.add(outKey);
                HashMap<String, String> IdInfo = new HashMap<>();

                Statement st2 = conn.createStatement();
                ResultSet rs2 = st2.executeQuery("SELECT * FROM public.\"List\" WHERE id = " + id + ";");

                String key = "";
                String pathToDir = "";

                try {
                    while (rs2.next()) {
                        key = rs2.getString("key");
                        pathToDir = rs2.getString("path");
                    }
                } catch (PSQLException exc) {}

                IdInfo.put("key", key);
                IdInfo.put("pathToDir", pathToDir);
                IdInfo.put("id", String.valueOf(id));
                IdInfo.put("path", path);
                resultDict.put(outKey, IdInfo);

                rs2.close();
                st2.close();
            }
        } catch (PSQLException exc) {}

        rs.close();
        st.close();
        conn.close();

        for (String i : resultDict.keySet()) {
            HashMap<String, String> dict = resultDict.get(i);

            Compress compress = new Compress(dict.get("pathToDir"), dict.get("key"));
            compress.smallZip("op" + dict.get("id"), dict.get("path"), i);
        }
    }
}
