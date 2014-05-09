/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GValidateDictionaryReferences {

    public static final Pattern PATTERN = Pattern.compile(
            GDictionary.class.getSimpleName() + ".get\\(\"(.*?)\"");

    public static final String[] LANGUAGES = { "en", "ro", "ru", "pt", "fr"};
    
    public static void main(String[] args) throws Exception {
        String src = args[0];
        File file = new File(src);
        for (String language : LANGUAGES) {
            Map<String, String> dictionary = getDictionary(language);
            validateSources(file, dictionary, language);
        }
    }

    private static Map<String, String> getDictionary(String language)
            throws Exception {
        Map<String, String> dictionary = new TreeMap<String, String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                GDictionary.class.getResourceAsStream(
                "/localized/" + language + "/conf/dictionary.txt")));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() == 0 || line.startsWith("#"))
                continue;
            int pos = line.indexOf('=');
            String key = line.substring(0, pos).trim();
            String value = new String(line.substring(pos + 1).trim().getBytes(),
                    "UTF-8");
            dictionary.put(key, value);
        }
        return dictionary;
    }
    
    private static void validateSources(File file,
            Map<String, String> dictionary, String language) throws Exception {
        if (file.isDirectory()) {
            String[] fs = file.list();
            for (String f : fs)
                validateSources(new File(file, f), dictionary, language);
            return;
        }
        if (!file.getName().endsWith(".java"))
            return;
        StringBuffer buf = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        while ((line = in.readLine()) != null)
            buf.append(line);
        in.close();
        String text = String.valueOf(buf).replaceAll("\\s+", "");
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!dictionary.containsKey(key))
                System.out.println(file.getName() + ": Missing "
                + language.toUpperCase() + " dictionary entry for key " + key);
        }
    }
}
