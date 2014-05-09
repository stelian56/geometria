/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import java.util.Arrays;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetUtils;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GParallelToLineCondition implements GPlaneCondition {

    private Point3d[] coords;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("coords");
        if (ns.getLength() != 2) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        coords = new Point3d[2];
        for (int i = 0; i < 2; i++) {
            String coordsString = ns.item(i).getTextContent();
            coords[i] = GStringUtils.coordsFromString(coordsString);
            if (coords == null) {
                logger.error(coordsString);
                throw new Exception();
            }
        }
    }

    public void validate(String valueString, GDocument document)
            throws Exception {
        logger.info(valueString);
        GFigure figure = document.getSelectedFigure();
        Point3d[] cs = GPointSetUtils.fromString(valueString, figure);
        if (cs.length != 2) {
            logger.info(cs.length);
            throw new Exception(GDictionary.get("Enter2RefPoints"));
        }
        coords = cs;
    }

    public boolean verify(Point3d[] cs, GDocument document) {
        logger.info(Arrays.asList(cs));
        Vector3d n = GMath.cross(cs[0], cs[1], cs[2]);
        n.normalize();
        Vector3d v = new Vector3d(coords[1]);
        v.sub(coords[0]);
        v.normalize();
        return Math.abs(v.dot(n)) < GMath.EPSILON;
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<condition>");
        buf.append("\n<type>parallelToLine</type>");
        for (int i = 0; i < coords.length; i++) {
            buf.append("\n<coords>");
            buf.append(GStringUtils.coordsToString(coords[i]));
            buf.append("</coords>");
        }
        buf.append("\n</condition>");
    }

    public String getStringValue() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < coords.length; i++) {
            buf.append(GStringUtils.coordsToString(coords[i], true));
            if (i < coords.length - 1)
                buf.append(" , ");
        }
        return String.valueOf(buf);
    }

    public String getDescription() {
        return GDictionary.get("ParallelToLine");
    }
}
