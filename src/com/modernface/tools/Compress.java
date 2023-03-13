package com.modernface.tools;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Compress {
    Path pathToDir;
    Path pathBase;
    Path pathBaseParent;
    String key;

    public Compress(String pathToDir, String key) throws URISyntaxException {
        this.pathToDir = Paths.get(pathToDir);
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        this.key = key;
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
        this.listfWithoutRec(files, this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/");

        long sum = 0;

        for (Map.Entry<String, String> file : files.entrySet()) {
            sum += Files.size(Path.of(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/" + file.getKey()));
        }

        return sum;
    }

    private long getSizeOfUnzippedFiles() throws IOException {
        return Files.walk(this.pathToDir).mapToLong(p -> p.toFile().length()).sum();
    }

    private void clearPreviousZipped(String operationName) {
        ArrayList<File> files = (ArrayList<File>) FileUtils.listFilesAndDirs(new File(this.pathBaseParent + "/buffer/zipped/"), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
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

    public void zipAll(String operationName) throws IOException {
        if (Files.exists(this.pathToDir) &&  Files.isDirectory(this.pathToDir)) {
            ArrayList<File> allFilesList = new ArrayList<>();

            this.clearPreviousZipped(operationName);

            HashMap<String, String> allFiles = new HashMap<>();
            this.listfWithoutRec(allFiles, this.pathToDir.toString());

            for (Map.Entry<String, String> file : allFiles.entrySet()) {
                allFilesList.add(new File(this.pathToDir + "/" + file.getKey()));
            }

            System.out.println(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/");

            new File(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/").mkdir();

            int counter = 0;
            for (File file : allFilesList) {
                new File(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/" + Integer.toHexString(counter) + ".zip").createNewFile();
                if (file.isDirectory()) {
                    new ZipFile(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/" + Integer.toHexString(counter) + ".zip").addFolder(file);
                } else {
                    new ZipFile(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + "/" + Integer.toHexString(counter) + ".zip").addFile(file);
                }
                counter++;
            }

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
            json.put("unzippedSize", getSizeOfUnzippedFiles());
            Files.writeString(Path.of(this.pathBaseParent + "/buffer/zipped/" + this.key + operationName + ".json"), json.toJSONString());
        } else {
            throw new ZipException("Not exists or is not a directory");
        }
    }

    public void unzipAll(String operationName, String pathToExtract) throws ZipException, URISyntaxException {
        if (Files.exists(pathToDir) &&  Files.isDirectory(pathToDir)) {
            HashMap<String, String> files = new HashMap<>();
            listfWithoutRec(files, this.pathBaseParent + "/buffer/zipped/" + operationName + "/");

            new File(this.pathBaseParent + "/" + operationName).mkdir();

            for (String file : files.keySet()) {
                new ZipFile(this.pathBaseParent + "/buffer/zipped/" + operationName + "/" + file).extractAll(pathToExtract);
            }

        } else {
            throw new ZipException("Not exists or is not a directory");
        }
    }
}
