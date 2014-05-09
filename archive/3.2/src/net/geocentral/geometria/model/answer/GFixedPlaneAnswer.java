/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import java.util.Arrays;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetUtils;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GFixedPlaneAnswer implements GAnswer {

    private Point3d[] coords;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GFixedPlaneAnswer() {}

    public GFixedPlaneAnswer(Point3d[] coords) {
        logger.info(Arrays.asList(coords));
        this.coords = new Point3d[3];
        for (int i = 0; i < 3; i++)
            this.coords[i] = new Point3d(coords[i]);
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("coords");
        if (ns.getLength() != 3) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        coords = new Point3d[3];
        for (int i = 0; i < 3; i++) {
            String coordsString = ns.item(i).getTextContent();
            if (document.isLocked()) {
                String decodedCoordsString = GStringUtils.decode(coordsString);
                if (decodedCoordsString == null) {
                    logger.error(String.format("Unencoded fixed plane answer item %s in locked problem", coordsString));
                    throw new Exception();
                }
                coordsString = decodedCoordsString;
            }
            coords[i] = GStringUtils.coordsFromString(coordsString);
            if (coords[i] == null) {
                logger.error(coordsString);
                throw new Exception();
            }
        }
        if (GMath.areCollinear(coords, GMath.EPSILON)) {
            logger.error("Collinear: " + Arrays.asList(coords));
            throw new Exception();
        }
    }

    public boolean validate(String valueString, String figureName,
            GDocument document) {
        Point3d[] cs;
        try {
            cs = GPointSetUtils.fromString(valueString, figureName, document);
        }
        catch (Exception exception) {
            return false;
        }
        GFigure figure = document.getFigure(figureName);
        double epsilon = figure == null ? GMath.EPSILON 
                : figure.getSolid().getEpsilon();
        return verify(cs, epsilon);
    }

    public boolean verify(Point3d[] cs, double epsilon) {
        logger.info(Arrays.asList(cs) + ", " + epsilon);
        Vector3d v1 = GMath.cross(coords[0], coords[1], coords[2]);
        Vector3d v2 = GMath.cross(cs[0], cs[1], cs[2]);
        v1.normalize();
        v2.normalize();
        return GMath.areCollinear(v1, v2, epsilon);
    }

    public void serialize(StringBuffer buf, boolean lock) {
        logger.info("");
        buf.append("\n<answer>");
        buf.append("\n<type>fixedPlane</type>");
        for (int i = 0; i < 3; i++) {
            buf.append("\n<coords>");
            String coordsString = GStringUtils.coordsToString(coords[i]);
            if (lock) {
                coordsString = GStringUtils.encode(coordsString);
            }
            buf.append(coordsString);
            buf.append("</coords>");
        }
        buf.append("\n</answer>");
    }

    public Point3d[] getCoords() {
        Point3d[] cs = new Point3d[3];
        for (int i = 0; i < 3; i++)
            cs[i] = new Point3d(coords[i]);
        return cs;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 3; i++) {
            buf.append(GStringUtils.coordsToString(coords[i], true));
            if (i < 2)
                buf.append(" , ");
        }
        return String.valueOf(buf);
    }
}
