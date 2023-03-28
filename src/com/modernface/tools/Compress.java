package com.modernface.tools;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Compress {
    Path pathToDir;
    Path pathBase;
    Path pathBaseParent;
    String key;
    Path pathToData;

    public Compress(String pathToDir, String key) throws URISyntaxException {
        this.pathToDir = Paths.get(pathToDir);
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.key = key;
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
    }

    private Path shortPath(String absPath) {
        return this.pathToDir.relativize(Path.of(absPath));
    }

    private void listf(HashMap<String, String> allFiles, String directoryPath) {
        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();

        for (File file : fList) {
            if (file.isFile()) {
                allFiles.put(file.getName(), shortPath(file.getPath()).toString());
            } else if (file.isDirectory()) {
                listf(allFiles, file.getAbsolutePath());
            }
        }
    }

    private void listfWithoutRec(HashMap<String, String> allFiles, String directoryPath) {
        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();

        for (File file : fList) {
            allFiles.put(file.getName(), shortPath(file.getPath()).toString());
        }
    }

    private long getSizeOfZippedFiles(String operationName) throws IOException {
        HashMap<String, String> files = new HashMap<>();
//        this.listfWithoutRec(files, this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/");
        this.listfWithoutRec(files, String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName)));

        long sum = 0;

        for (Map.Entry<String, String> file : files.entrySet()) {
//            sum += Files.size(Path.of(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/" + file.getKey()));
            sum += Files.size(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName, file.getKey()));
        }

        return sum;
    }

    private long getSizeOfUnzippedFiles() throws IOException {
        return Files.walk(this.pathToDir).mapToLong(p -> p.toFile().length()).sum();
    }

    private void clearPreviousZipped(String operationName) {
        ArrayList<File> files = (ArrayList<File>) FileUtils.listFilesAndDirs(new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped"))), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        String fileName;
        for (File file : files) {
            try {
                fileName = file.getName().substring(16);
                if (fileName.equals(operationName)) {
                    FileUtils.deleteDirectory(file);
                }
                if (fileName.equals(operationName + ".json")) {
                    FileUtils.delete(file);
                }
            } catch (StringIndexOutOfBoundsException ecx) {} catch (IOException e) {
            }
        }
    }

    public void zipAll(String operationName) throws IOException, SQLException, ParseException {
        if (Files.exists(this.pathToDir) &&  Files.isDirectory(this.pathToDir)) {
            ArrayList<File> allFilesList = new ArrayList<>();

            this.clearPreviousZipped(operationName);

            HashMap<String, String> allFiles = new HashMap<>();
            this.listfWithoutRec(allFiles, this.pathToDir.toString());

            for (Map.Entry<String, String> file : allFiles.entrySet()) {
                allFilesList.add(new File(this.pathToDir + "/" + file.getKey()));
            }

            GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
            HashMap<String, String> DBdata = dbInfo.getCFG();

            String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
            Properties props = new Properties();
            props.setProperty("user", DBdata.get("user"));
            props.setProperty("password", DBdata.get("password"));
            Connection conn = DriverManager.getConnection(url, props);

            PreparedStatement st = conn.prepareStatement("UPDATE \"List\" SET status = 4 WHERE id = " + operationName.substring(2) + ";");
            st.execute();
            st.close();

            File progressFile = new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "zip_progress", this.key + operationName + ".txt")));
            progressFile.createNewFile();
            Files.writeString(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "zip_progress", this.key + operationName + ".txt"), "0");

            new File(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName))).mkdir();

            long size = this.getSizeOfUnzippedFiles();
            long sum = 0;

            for (int i = 0; i < allFilesList.toArray().length; i++) {
                File file = allFilesList.get(i);;

                if (file.isDirectory()) {
                    ZipFile zip = new ZipFile(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName, Integer.toHexString(i) + ".zip")));
                    zip.addFolder(file);
                    sum += Files.walk(Path.of(file.getPath())).mapToLong(p -> p.toFile().length()).sum();
                } else {
                    ZipFile zip = new ZipFile(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName, Integer.toHexString(i) + ".zip")));
                    zip.addFile(file);
                    sum += Files.walk(Path.of(file.getPath())).mapToLong(p -> p.toFile().length()).sum();
                }

                Files.writeString(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "zip_progress", this.key + operationName + ".txt"), Integer.toHexString((int) (((double) sum/size) * 100)));
            }

            Files.writeString(Paths.get(String.valueOf(this.pathToData), "db", "right_menu", "zip_progress", this.key + operationName + ".txt"), "100");

            allFiles.clear();

            JSONArray allFilesStr = new JSONArray();

            this.listf(allFiles, this.pathToDir.toString());
            for (Map.Entry<String, String> file : allFiles.entrySet()) {
                allFilesStr.add(file.getValue());
            }

            JSONObject json = new JSONObject();
            StringWriter out = new StringWriter();
            allFilesStr.writeJSONString(out);
            json.put("all_files", allFilesStr);
            json.put("zippedSize", getSizeOfZippedFiles(operationName));
            json.put("unzippedSize", size);
            Files.writeString(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + operationName + ".json"), json.toJSONString());

            st = conn.prepareStatement("UPDATE \"List\" SET status = 1 WHERE id = " + operationName.substring(2) + ";");
            st.execute();
            st.close();

            conn.commit();
            conn.close();
        } else {
            throw new ZipException("Not exists or is not a directory");
        }
    }

    public void smallZip(String operationName, String path, String outKey) throws IOException, ParseException, SQLException {
        GetDbInfo dbInfo = new GetDbInfo(String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "db.cfg")));
        HashMap<String, String> DBdata = dbInfo.getCFG();

        String url = "jdbc:postgresql://" + DBdata.get("host") + "/" + DBdata.get("name");
        Properties props = new Properties();
        props.setProperty("user", DBdata.get("user"));
        props.setProperty("password", DBdata.get("password"));
        Connection conn = DriverManager.getConnection(url, props);

        Path filePath = Paths.get(String.valueOf(this.pathToDir) + "/s", path);

        File requestedFile = new File(String.valueOf(Paths.get(String.valueOf(this.pathToDir), path)));

        if (requestedFile.isFile()) {
            ZipFile zip = new ZipFile(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "fast_zipped", outKey + ".zip")));
            zip.addFile(requestedFile);

            System.out.println(outKey);

            PreparedStatement st = conn.prepareStatement("UPDATE \"Files\" SET status = 2 WHERE out_key = '" + outKey + "';");
            st.execute();
            st.close();
        } else {
            ZipFile zip = new ZipFile(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "fast_zipped", outKey + ".zip")));
            zip.addFolder(requestedFile);

            PreparedStatement st = conn.prepareStatement("UPDATE \"Files\" SET status = 2 WHERE out_key = '" + outKey + "';");
            st.execute();
            st.close();
        }

        conn.close();
    }
}
