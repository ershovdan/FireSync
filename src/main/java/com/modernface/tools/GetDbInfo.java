package com.modernface.tools;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GetDbInfo {
    String pathToCfg;

    public GetDbInfo(String pathToDbCfg) {
        this.pathToCfg = pathToDbCfg;
    }

    public HashMap<String, String> getCFG() throws IOException, ParseException {
        HashMap<String, String> data = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(this.pathToCfg));

        data.put("name", (String) jsonObject.get("name"));
        data.put("port", (String) jsonObject.get("port"));
        data.put("user", (String) jsonObject.get("user"));
        data.put("password", (String) jsonObject.get("password"));
        data.put("host", (String) jsonObject.get("host"));

        return data;
    }
}
