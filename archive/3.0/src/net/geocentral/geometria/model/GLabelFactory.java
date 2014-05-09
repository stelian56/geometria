/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class GLabelFactory {

    public static final String VARIABLE_CHARSET = "a-zA-Z\\p{InCyrillic}_";
    
    public static final String VARIABLE_NAME_PATTERN =
        "[" + VARIABLE_CHARSET + "][0-9" + VARIABLE_CHARSET + "]*";

    public static final String LABEL_PATTERN = "[A-Z][0-9]*";

    private final Pattern pattern = Pattern.compile("([A-Z])([0-9]*)");

    private static GLabelFactory instance;
    
    private Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GLabelFactory() {}
    
    public static GLabelFactory getInstance() {
        if (instance == null)
            instance = new GLabelFactory();
        return instance;
    }
    
    public String createLabel(Collection<String> labels) {
        logger.info("");
        String[] labelList = new String[labels.size()];
        labels.toArray(labelList);
        Arrays.sort(labelList, new LabelComparator());
        String label = nextLabel(labelList[labelList.length - 1]);
        logger.info(label);
        return label;
    }

    public String nextLabel(String label) {
        if (label == null)
            return "A";
        Matcher matcher = pattern.matcher(label);
        matcher.matches();
        char prefix = matcher.group(1).charAt(0);
        String suffix = matcher.groupCount() == 1 ? "" : matcher.group(2);
        if (prefix != 'Z')
            return ++prefix + suffix;
        if (suffix.length() == 0)
            suffix = "1";
        else
            suffix = String.valueOf(Integer.parseInt(suffix) + 1);
        return "A" + suffix;
    }

    public void validateLabel(String label) throws Exception {
        if (!label.matches(LABEL_PATTERN)) {
            logger.error(label);
            throw new Exception();
        }
    }
}

class LabelComparator implements Comparator<String> {

    public int compare(String s1, String s2) {
        if (s1.equals(s2))
            return 0;
        if (s1.length() < s2.length())
            return -1;
        if (s2.length() < s1.length())
            return 1;
        if (s1.length() == 1)
            return s1.compareTo(s2);
        String suffix1 = s1.substring(1);
        String suffix2 = s2.substring(1);
        if (suffix1.equals(suffix2))
            return s1.compareTo(s2);
        else
            return suffix1.compareTo(suffix2);
    }
}
