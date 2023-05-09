package com.modernface.core;

import com.modernface.logger.Logger;
import com.modernface.tools.Compress;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class FileChangesChecker {
    Path pathToData;
    Connection conn;
    Logger logger;

    FileChangesChecker(Connection con, Logger log) {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.conn = con;
        this.logger = log;
    }

    public void check() throws IOException, ParseException, SQLException, URISyntaxException {
        File bufferZippedDir = new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped")));

        ArrayList<File> listedFiles = new ArrayList<>(FileUtils.listFilesAndDirs(bufferZippedDir, TrueFileFilter.TRUE, null));

        for (File i : listedFiles) {
            if (i.getName().contains(".json")) {
                String id = i.getName().substring(18, i.getName().indexOf("."));
                String key = i.getName().substring(0, 16);

                Statement st = this.conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM \"List\" WHERE id = " + id + ";");

                String path = "";
                String name = "";
                int status = 0;
                while (rs.next()) {
                    path = rs.getString("path");
                    status = rs.getInt("status");
                    name = rs.getString("name");
                }

                if (status == 1 || status == 4) {
                    PreparedStatement stp = this.conn.prepareStatement("UPDATE \"Other\" SET value_str = '" + name + "' WHERE type = 'now_operating_total';");
                    stp.execute();
                    stp.close();

                    JSONObject json = (JSONObject) JSONValue.parse(Files.readString(Path.of(i.getPath())));
                    JSONObject filesFromJSON = (JSONObject) json.get("all_files");

                    Compress compress = new Compress(path, key, Integer.parseInt(id));

                    long now = System.currentTimeMillis();

                    ArrayList<String> filesFromJSONPaths = new ArrayList<>();
                    for (Object j : filesFromJSON.keySet()) {
                        File f = new File(String.valueOf(Paths.get(path, j.toString())));
                        long lastTime = (long) json.get("time");

                        if (f.lastModified() > lastTime) {
                            FileUtils.delete(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", key + "op" + id, (String) filesFromJSON.get(j)))));
                            compress.zip(f.getAbsolutePath(), (String) filesFromJSON.get(j));
                        }

                        filesFromJSONPaths.add(j.toString());
                    }

                    Collection<File> preFileReaded = FileUtils.listFiles(new File(path), null, true);
                    ArrayList<String> fileReaded = new ArrayList<>();

                    for (File j : preFileReaded) {
                        fileReaded.add(j.toString().substring(path.length() + 1));
                    }

                    ArrayList<File> lastIdIntList = (ArrayList<File>) FileUtils.listFiles(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", key + "op" + id))), null, true);
                    int lastIdInt = lastIdIntList.size();

                    if (status == 4) {
                        for (File k : lastIdIntList) {
                            FileUtils.delete(k);
                        }
                    }

                    JSONObject jsonPaths = new JSONObject();
                    JSONObject finalJSON = new JSONObject();

                    int diff = 0;
                    for (String j : fileReaded) {
                        if (!filesFromJSONPaths.contains(j)) {
                            compress.zip(j, Integer.toHexString(lastIdInt));
                            jsonPaths.put(j, Integer.toHexString(lastIdInt));

                            lastIdInt++;
                            diff++;
                        }
                    }

                    for (String j : filesFromJSONPaths) {
                        if (!fileReaded.contains(j)) {
                            FileUtils.delete(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", key + "op" + id, (String) filesFromJSON.get(j)))));
                        }
                    }

                    long sizeZipped = 0;
                    long sizeUnzipped = FileUtils.sizeOfDirectory(new File(path));

                    for (File j : lastIdIntList) {
                        sizeZipped += FileUtils.sizeOf(j);
                    }

                    finalJSON.put("all_files", jsonPaths);
                    finalJSON.put("unzippedSize", sizeUnzipped);
                    finalJSON.put("zippedSize", sizeZipped);
                    finalJSON.put("time", now);

                    if (status == 4) {
                        st = this.conn.createStatement();
                        st.executeUpdate("UPDATE \"List\" SET \"status\" = 1 WHERE \"id\" = " + id + ";");
                        st.close();
                    }

                    Files.writeString(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", key + "op" + id + ".json"), finalJSON.toJSONString());

                    this.logger.info("share with id=" + id + " was updated");
                }
            }

            PreparedStatement stp = conn.prepareStatement("UPDATE \"Other\" SET value_str = '' WHERE type = 'now_operating_total';");
            stp.execute();
            stp.close();
        }
    }
}
