package com.modernface.tools;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Compress {
    Path pathToDir;
    String key;
    Path pathToData;
    int id;

    public Compress(String pathToDir, String key, int id) throws URISyntaxException {
        this.pathToDir = Paths.get(pathToDir);
        this.key = key;
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.id = id;
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

    public void initJSON(String pathToJson) throws IOException {
        String json = "{\"all_files\": {}, \"zippedSize\": 0, \"unzippedSize\": 0, \"time\": 0}";
        Files.writeString(Path.of(pathToJson), json);
    }

    public void zip(String path, String fileID) throws IOException, ParseException, SQLException {
        Path filePath = Paths.get(String.valueOf(this.pathToDir), path);

        File requestedFile = new File(String.valueOf(filePath));

        if (requestedFile.isFile()) {
            ZipFile zip = new ZipFile(String.valueOf(Paths.get(String.valueOf(this.pathToData), "buffer", "zipped", this.key + "op" + this.id, fileID + ".zip")));
            zip.addFile(requestedFile);
        }
    }
}
