/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import net.geocentral.geometria.model.GOptions;

import org.apache.log4j.Logger;

public class GDictionary {

    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");
    
    private static Map<String, String> dictionary;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public static void init() throws Exception {
        logger.info("");
        dictionary = new TreeMap<String, String>();
        GOptions options = GOptionsManager.getInstance().getOptions();
        String file = String.format("/dictionary/%s/dictionary.txt", options.getLanguage());
        BufferedReader in =
            new BufferedReader(new InputStreamReader(GDictionary.class.getResourceAsStream(file), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            int pos = line.indexOf('=');
            String key = line.substring(0, pos).trim();
            String value = line.substring(pos + 1).trim();
            dictionary.put(key, value);
        }
        initUIManager();
    }
    
    public static String get(String key, String... parameters) {
        String value;
        Matcher keyMatcher = PLACEHOLDER_PATTERN.matcher(key);
        if (keyMatcher.matches()) {
            key = keyMatcher.group(1);
        }
        value = dictionary.get(key);
        if (value == null) {
            return "${" + key + "}";
        }
        value = value.trim();
        if (value.length() == 0 || parameters.length == 0) {
            return value;
        }
        StringBuffer buf = new StringBuffer();
        Matcher valueMatcher = PLACEHOLDER_PATTERN.matcher(value);
        int parameterIndex = 0;
        while (valueMatcher.find()) {
            if (parameterIndex == parameters.length) {
                break;
            }
            int replacementIndex = Integer.valueOf(valueMatcher.group(1)) - 1;
            valueMatcher.appendReplacement(buf, parameters[replacementIndex]);
            parameterIndex++;
        }
        valueMatcher.appendTail(buf);
        return String.valueOf(buf);
    }

    private static void initUIManager() {
        logger.info("");
        String prefix = "UIManager.";
        for (String key : dictionary.keySet()) {
            if (key.startsWith(prefix)) {
                String value = dictionary.get(key);
                key = key.substring(prefix.length());
                UIManager.put(key, value);
            }
        }
    }
}
