package com.modernface.tools;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class GetMainInfo {
    String pathToCfg;
    Path pathToData;

    public GetMainInfo() {
        this.pathToData = Paths.get(System.getProperty("user.home"), "FireSyncData");
        this.pathToCfg = String.valueOf(Paths.get(String.valueOf(this.pathToData), "cfg", "main.cfg"));
    }

    public HashMap<String, String> getFullCFG() throws IOException, ParseException {
        HashMap<String, String> data = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(this.pathToCfg));

        data.put("web_server_ip", (String) jsonObject.get("web_server_ip"));
        data.put("web_server_port", (String) jsonObject.get("web_server_port"));

        return data;
    }

    public String getCFG(String key) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(this.pathToCfg));

        try {
            return String.valueOf(jsonObject.get(key));
        } catch (Exception exc) {
            return "";
        }
    }
}
