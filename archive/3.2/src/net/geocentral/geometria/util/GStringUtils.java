/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;

import org.apache.log4j.Logger;

public class GStringUtils {

    public static final Pattern COORDS_PATTERN = Pattern.compile(
      "\\s*([0-9\\+\\-\\.E]+)\\s+([0-9\\+\\-\\.E]+)\\s+([0-9\\+\\-\\.E]+)\\s*");
    private final static Random random = new Random();
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
    public static Point3d coordsFromString(String stringCoords) {
        Matcher matcher = COORDS_PATTERN.matcher(stringCoords);
        if (!matcher.matches())
            return null;
        try {
            double x = Double.valueOf(matcher.group(1));
            double y = Double.valueOf(matcher.group(2));
            double z = Double.valueOf(matcher.group(3));
            return new Point3d(x, y, z);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String coordsToString(Point3d coords) {
        return coordsToString(coords, false);
    }

    public static String coordsToString(Point3d coords, boolean bracket) {
        StringBuffer buf = new StringBuffer();
        if (bracket) {
            buf.append('[');
        }
        buf.append(coords.x).append(' ').append(coords.y).append(' ').append(coords.z);
        if (bracket) {
            buf.append(']');
        }
        return String.valueOf(buf);
    }

    public static String stackTraceToString(Exception exception) {
        Writer writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        exception.printStackTrace(out);
        return writer.toString();
    }
    
    public static String fromXml(String str) {
        str = str.replace("&lt;", "<");
        str = str.replace("&gt;", ">");
        str = str.replace("&quot;", "\"");
        str = str.replace("&#39;", "'");
        str = str.replace("&amp;", "&");
        return str;
    }

    public static String toXml(String str) {
        str = str.replace("&", "&amp;");
        str = str.replace("<", "&lt;");
        str = str.replace(">", "&gt;");
        str = str.replace("\"", "&quot;");
        str = str.replace("'", "&#39;");
        return str;
    }

    public static String encode(String value) {
        logger.info(value);
        StringBuffer buf = new StringBuffer();
        int salt = random.nextInt((int)9e5) + (int)1e5;
        for (char c : String.valueOf(salt).toCharArray()) {
            buf.append((char)((int)'K' + Integer.valueOf(String.valueOf(c))));
        }
        byte[] bytes = value.getBytes();
        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
            bytes[byteIndex] += salt;
        }
        String encoded = GBase64Utils.encode(bytes);
        buf.append(encoded);
        return buf.toString();
    }
    
    public static String decode(String value) {
        logger.info(value);
        StringBuffer saltBuf = new StringBuffer();
        String encodedSalt = value.substring(0, 6);
        for (char c : encodedSalt.toCharArray()) {
            saltBuf.append(String.valueOf((int)c - (int)'K'));
        }
        int salt = Integer.valueOf(saltBuf.toString());
        String encodedValue = value.substring(6);
        byte[] bytes = GBase64Utils.decode(encodedValue);
        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
            bytes[byteIndex] -= salt;
        }
        String decoded = new String(bytes);
        return decoded;
    }
}
