/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;

import org.apache.log4j.Logger;

public class GLineSetUtils {

    private static final Pattern pattern = Pattern.compile(
            String.format("(%s)(%s)", GLabelFactory.LABEL_PATTERN, GLabelFactory.LABEL_PATTERN));
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GLineSetUtils() {}
    
    public static List<GPoint3d[]> fromString(String s, String figureName, GDocument document) throws Exception {
        GFigure figure = document.getFigure(figureName);
        return fromString(s, figure);
    }
    
    public static List<GPoint3d[]> fromString(String s, GFigure figure) throws Exception {
        String[] tokens = s.split(",");
        List<GPoint3d[]> lines = new ArrayList<GPoint3d[]>();
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
            tokens[i] = tokens[i].replace("[", "").replace("]", "");
        }
        if (tokens.length == 1 && tokens[0].length() == 0)
            return lines;
        GSolid solid = figure.getSolid();
        String figureName = figure.getName();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Matcher matcher = pattern.matcher(token);
            if (!matcher.matches()) {
                logger.info(String.format("No such segment: %s in figure %s", token, figureName));
                throw new Exception(GDictionary.get("NoSuchLineInFigure", token, figureName));
            }
            String p1Label = matcher.group(1);
            String p2Label = matcher.group(2);
            if (solid.getPoint(p1Label) == null) {
                logger.info(String.format("No such point: %s in figure %s", p1Label, figureName));
                throw new Exception(GDictionary.get("NoSuchPointInFigure", p1Label, figureName));
            }
            if (solid.getPoint(p2Label) == null) {
                logger.info(String.format("No such point: %s in figure %s", p2Label, figureName));
                throw new Exception(GDictionary.get("NoSuchPointInFigure", p2Label, figureName));
            }
            if (p1Label.equals(p2Label)) {
                logger.info(String.format("No such segment: %s in figure %s", token, figureName));
                throw new Exception(GDictionary.get("NoSuchLineInFigure", token, figureName));
            }
            if (solid.linesThroughPoints(p1Label, p2Label).isEmpty()) {
                logger.info(String.format("No such segment: %s in figure %s", token, figureName));
                throw new Exception(GDictionary.get("NoSuchLineInFigure", token, figureName));
            }
            GPoint3d[] ps = { solid.getPoint(p1Label), solid.getPoint(p2Label) };
            lines.add(ps);
        }
        return lines;
    }

    public static String[] fromSelection(GDocument document) {
        GFigure figure = document.getSelectedFigure();
        if (figure == null)
            return new String[0];
        List<String> stringValues = new ArrayList<String>();
        for (GSelectable element : figure.getSolid().getSelection()) {
            if (element instanceof GStick) {
                GStick stick = (GStick)element;
                String stringValue = String.format("%s%s", stick.label1, stick.label2);
                stringValues.add(stringValue);
            }
        }
        return stringValues.toArray(new String[stringValues.size()]);
    }
}
