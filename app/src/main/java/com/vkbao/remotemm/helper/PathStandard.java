package com.vkbao.remotemm.helper;

import java.util.Arrays;

public class PathStandard {
    public static String standardize(String path) {
        path = path.trim();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String concatPath(String oldPath, String path) {
        oldPath = standardize(oldPath);
        String newPath = "";
        if (oldPath.isEmpty()) {
            newPath = path;
        } else {
            newPath = oldPath + "/" + path;
        }
        return newPath;
    }

    public static String getBackPath(String path) {
        path = standardize(path);

        String[] parts = path.split("/");
        String newPath = "";

        if (parts.length > 1) {
            newPath = String.join("/", Arrays.copyOf(parts, parts.length - 1));
        }
        return newPath;
    }
}
