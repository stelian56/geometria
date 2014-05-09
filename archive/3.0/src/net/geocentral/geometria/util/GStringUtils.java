/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;

public class GStringUtils {

    public static final Pattern COORDS_PATTERN = Pattern.compile(
      "\\s*([0-9\\+\\-\\.E]+)\\s+([0-9\\+\\-\\.E]+)\\s+([0-9\\+\\-\\.E]+)\\s*");

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
}
