package com.modernface.core;

import com.modernface.logger.Logger;
import com.modernface.tools.Compress;
import com.modernface.tools.GetDbInfo;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Init {
    Path pathBase;
    Path pathBaseParent;
    Path pathToData;

    Init() throws URISyntaxException {
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }

    public void initDB(Logger logger) throws SQLException, IOException, ParseException {
        logger.info("DB init started");

        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        Connection conn = null;

        try {
            String url = "jdbc:postgresql://" + DBdata.get("host") + ":" + DBdata.get("port") + "/" + DBdata.get("name");
            Properties props = new Properties();
            props.setProperty("user", DBdata.get("user"));
            props.setProperty("password", DBdata.get("password"));
            conn = DriverManager.getConnection(url, props);
        } catch (Exception exc) {
            logger.error("can't connect to DB");
            return;
        }


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
            "CREATE TABLE IF NOT EXISTS public.\"Connected\"\n" +
            "(\n" +
            "    \"time\" timestamp without time zone NOT NULL,\n" +
            "    amount integer,\n" +
            "    id integer,\n" +
            "    CONSTRAINT \"Connected_pkey\" PRIMARY KEY (\"time\")\n" +
            ")"
        );
        st.execute();
        st.close();

        st = conn.prepareStatement("" +
            "CREATE TABLE IF NOT EXISTS public.\"Other\"\n" +
            "(\n" +
            "    type text COLLATE pg_catalog.\"default\" NOT NULL,\n" +
            "    value_int integer,\n" +
            "    value_str text COLLATE pg_catalog.\"default\",\n" +
            "    CONSTRAINT \"Other_pkey\" PRIMARY KEY (type)\n" +
            ")"
        );
        st.execute();
        st.close();

        st = conn.prepareStatement("" +
            "CREATE TABLE IF NOT EXISTS public.\"ConnectedBuffer\"\n" +
            "(\n" +
            "    \"time\" timestamp without time zone,\n" +
            "    id integer NOT NULL,\n" +
            "    CONSTRAINT \"ConnectedBuffer_pkey\" PRIMARY KEY (id)\n" +
            ")"
        );
        st.execute();
        st.close();

        st = conn.prepareStatement("" +
            "CREATE TABLE IF NOT EXISTS public.\"Network\"\n" +
            "(\n" +
            "    \"time\" timestamp without time zone NOT NULL,\n" +
            "    data integer,\n" +
            "    CONSTRAINT \"Network_pkey\" PRIMARY KEY (\"time\")\n" +
            ")"
        );
        st.execute();
        st.close();

        try {
            st = conn.prepareStatement("INSERT INTO \"Other\" (type, value_int, value_str) VALUES ('active_shares', 0, '');");
            st.execute();
            st.close();
        } catch (Exception exc) {}

        try {
            st = conn.prepareStatement("INSERT INTO \"Other\" (type, value_int, value_str) VALUES ('now_operating_total', 0, '');");
            st.execute();
            st.close();
        } catch (Exception exc) {}

        try {
            st = conn.prepareStatement("INSERT INTO \"Other\" (type, value_int, value_str) VALUES ('net_interface', 0, 'eth0');");
            st.execute();
            st.close();
        } catch (Exception exc) {}

        st = conn.prepareStatement("DELETE FROM \"Connected\" WHERE amount > -1;");
        st.execute();
        st.close();

        conn.close();

        logger.info("DB init completed");
    }

    public void initDirs(boolean isSimplified) throws IOException {
        ArrayList<File> dirs = new ArrayList<>();
        dirs.add(new File(String.valueOf(this.pathToData)));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "right_menu"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "logs"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "logs", "general"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "zip_progress"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "status"))));
        dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "scripts"))));

        boolean createWebserver = false;
        if (!(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "web_server"))).exists())) {
            dirs.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "web_server"))));
            createWebserver = true;
        }

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        ArrayList<File> files = new ArrayList<>();
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "total_shares.txt"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "main.cfg"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "version.cfg"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "network_usage.txt"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "network_usage_final.txt"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "scripts", "network_usage.sh"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "scripts", "db-init.sh"))));
        files.add(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "logs", "fs.journal"))));

        for (File file : files) {
            if (!file.exists()) {
                file.createNewFile();
            }
        }

        if (Files.readString(Paths.get(String.valueOf(pathToData), "cfg", "db.cfg")).equals("")) {
            Files.writeString(Paths.get(String.valueOf(pathToData), "cfg", "db.cfg"), "" +
                "{\"name\": \"postgres\", \"port\": \"5433\", \"user\": \"postgres\", \"password\": \"none\", \"host\": \"localhost\"}" +
            "");
        }

        if (Files.readString(Paths.get(String.valueOf(pathToData), "cfg", "main.cfg")).equals("")) {
            Files.writeString(Paths.get(String.valueOf(pathToData), "cfg", "main.cfg"), "" +
                "{\"web_server_port\": \"8000\"}" +
            "");
        }

        if (Files.readString(Paths.get(String.valueOf(pathToData), "scripts", "db-init.sh")).equals("")) {
            Files.writeString(Paths.get(String.valueOf(pathToData), "scripts", "db-init.sh"), "" +
                "port=$1\n" +
                "password=$2\n" +
                "\n" +
                "# optional\n" +
                "docker pull postgres\n" +
                "\n" +
                "docker run -d -p $port:5432 --name FireSyncPostgres -e POSTGRES_PASSWORD=$password postgres\n" +
                "\n" +
                "cd ~/\n" +
                "mkdir \"FireSyncData\" > /dev/null\n" +
                "cd FireSyncData\n" +
                "mkdir \"cfg\" > /dev/null\n" +
                "cd cfg\n" +
                "touch db.cfg\n" +
                "\n" +
                "echo \"\" >> db.cfg\n" +
                "echo \"{\\\"name\\\": \\\"postgres\\\", \\\"port\\\": \\\"${port}\\\", \\\"user\\\": \\\"postgres\\\", \\\"password\\\": \\\"${password}\\\", \\\"host\\\": \\\"localhost\\\"}\" >> db.cfg\n" +
            "\n");
        }

        Files.writeString(Paths.get(String.valueOf(pathToData), "cfg", "version.cfg"), "" +
            "{\"version\": \"1.1.1\"}" +
        "");

        if (!(isSimplified) && createWebserver) {
            System.out.println("create ws");
        }
    }
}