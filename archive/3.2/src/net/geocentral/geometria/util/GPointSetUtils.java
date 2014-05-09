/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.vecmath.Point3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GStick;

import org.apache.log4j.Logger;

public class GPointSetUtils {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GPointSetUtils() {}
    
    public static Point3d[] fromString(String s, String figureName,
            GDocument document) throws Exception {
        GFigure figure = document.getFigure(figureName);
        return fromString(s, figure);
    }
    
    public static Point3d[] fromString(String s, GFigure figure) throws Exception {
        String[] tokens = s.split(",");
        Point3d[] coords = new Point3d[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
            tokens[i] = tokens[i].replace("[", "").replace("]", "");
        }
        if (tokens.length == 1 && tokens[0].length() == 0)
            return new Point3d[0];
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].matches(GLabelFactory.LABEL_PATTERN)) {
                if (figure == null) {
                    logger.info("No point: " + tokens[i]);
                    throw new Exception(
                        GDictionary.get("NoSuchPoint", tokens[i]));
                }
                GPoint3d p = figure.getSolid().getPoint(tokens[i]);
                if (p == null) {
                    logger.info("No point in figure: " + tokens[i] + ", "
                            + figure);
                    throw new Exception(GDictionary.get(
                        "NoSuchPointInFigure", tokens[i], figure.getName()));
                }
                coords[i] = new Point3d(p.coords);
            }
            else {
                coords[i] = GStringUtils.coordsFromString(tokens[i]);
                if (coords[i] == null) {
                    logger.info("Bad coordinates: " + tokens[i]);
                    throw new Exception(
                            GDictionary.get("BadCoordinates", tokens[i]));
                }
            }
        }
        return coords;
    }

    public static String[] fromSelection(GDocument document) {
        GFigure figure = document.getSelectedFigure();
        if (figure == null)
            return new String[0];
        Set<String> labels = new LinkedHashSet<String>();
        for (GSelectable element : figure.getSolid().getSelection()) {
            if (element instanceof GFace) {
                for (int i = 0; i < 3; i++) {
                    String label = ((GFace) element).labelAt(i);
                    labels.add(label);
                }
            }
            else if (element instanceof GStick) {
                labels.add(((GStick) element).label1);
                labels.add(((GStick) element).label2);
            }
            else if (element instanceof GPoint3d)
                labels.add(((GPoint3d) element).getLabel());
        }
        return labels.toArray(new String[labels.size()]);
    }
}
