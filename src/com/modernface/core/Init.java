package com.modernface.core;

import com.modernface.tools.Compress;
import com.modernface.tools.GetDbInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sqlite.core.DB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class Init {
    Path pathBase;
    Path pathBaseParent;

    Init() throws URISyntaxException {
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
    }

    public void initDB() throws SQLException, IOException, ParseException {
        GetDbInfo dbInfo = new GetDbInfo(this.pathBaseParent + "/cfg/db.cfg");
        HashMap<String, String> DBdata = dbInfo.getCFG();

        String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
        Properties props = new Properties();
        props.setProperty("user", DBdata.get("user"));
        props.setProperty("password", DBdata.get("password"));
        Connection conn = DriverManager.getConnection(url, props);


        PreparedStatement st = conn.prepareStatement("" +
            "CREATE TABLE IF NOT EXISTS public.\"List\"\n" +
            "(\n" +
            "    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 0 MINVALUE 0 MAXVALUE 2147483647 CACHE 1 ),\n" +
            "    name text COLLATE pg_catalog.\"default\",\n" +
            "    path text COLLATE pg_catalog.\"default\" NOT NULL,\n" +
            "    status integer NOT NULL,\n" +
            "    key text COLLATE pg_catalog.\"default\",\n" +
            "    CONSTRAINT \"List_pkey\" PRIMARY KEY (id)\n" +
            ")\n"
        );
        st.execute();
        st.close();

        st = conn.prepareStatement("" +
            "CREATE TABLE IF NOT EXISTS public.\"lastID\"\n" +
            "(\n" +
            "    \"lastID\" integer NOT NULL,\n" +
            "    CONSTRAINT \"lastID_pkey\" PRIMARY KEY (\"lastID\")\n" +
            ")\n"
        );
        st.execute();
        st.close();

        conn.close();
    }

    public void initDirs() throws IOException {
        ArrayList<File> dirs = new ArrayList<>();
        dirs.add(new File(this.pathBaseParent + "/buffer"));
        dirs.add(new File(this.pathBaseParent + "/buffer/zipped"));
        dirs.add(new File(this.pathBaseParent + "/db"));
        dirs.add(new File(this.pathBaseParent + "/db/right_menu"));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        ArrayList<File> files = new ArrayList<>();
        files.add(new File(this.pathBaseParent + "/db/right_menu/total_shares.txt"));

        for (File file : files) {
            if (!file.exists()) {
                file.createNewFile();
            }
        }
    }
}