package com.modernface.core;

import com.modernface.tools.Compress;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Init {
    Path pathBase;
    Path pathBaseParent;

    Init() throws URISyntaxException {
        this.pathBase = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        this.pathBaseParent = Paths.get(Compress.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
    }

    public void initDirs() {
        ArrayList<File> dirs = new ArrayList<>();
        dirs.add(new File(this.pathBaseParent + "/buffer"));
        dirs.add(new File(this.pathBaseParent + "/buffer/zipped"));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println(dir.getPath());
            }
        }
    }
}
