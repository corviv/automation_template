package com.github.corviv;

import java.io.File;
import java.io.FileFilter;

public class FileUtils {

    public static String getFileNameByPrefix(String path, String prefix) {
        File dir = new File(path);
        File[] fileName = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(prefix);
            }
        });
        if (fileName.length == 0) {
            throw new RuntimeException();
        }
        return fileName[0].toString();
    }

    public static String getAppVersion() {
        return WiniumSession.appVersion = "1.0";
    }
}
