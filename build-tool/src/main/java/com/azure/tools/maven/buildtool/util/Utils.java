package com.azure.tools.maven.buildtool.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Utils {
    private static final ResourceBundle strings = ResourceBundle.getBundle("strings");

    private Utils() {
        // no-op
    }

    public static String getString(String key) {
        return strings.getString(key);
    }

    public static String getString(String key, String... parameters) {
        return MessageFormat.format(getString(key), parameters);
    }
}
