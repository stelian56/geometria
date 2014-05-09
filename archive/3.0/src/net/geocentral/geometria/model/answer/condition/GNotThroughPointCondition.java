/**
 * Copyright 2000-2010 Geometria Contributors
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
import net.geocentral.geometria.util.GPointSetFactory;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GNotThroughPointCondition implements GCondition {

    private Point3d coords;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("coords");
        if (ns.getLength() != 1) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        String coordsString = ns.item(0).getTextContent();
        coords = GStringUtils.coordsFromString(coordsString);
        if (coords == null) {
            logger.error(coordsString);
            throw new Exception();
        }
    }

    public void validate(String valueString, GDocument document)
            throws Exception {
        logger.info(valueString);
        GFigure figure = document.getSelectedFigure();
        Point3d[] cs = GPointSetFactory.getInstance().fromString(valueString,
                figure);
        if (cs.length < 1) {
            logger.info(cs.length);
            throw new Exception(GDictionary.get("EnterRefPoint"));
        }
        if (cs.length > 1) {
            logger.info(cs.length);
            throw new Exception(GDictionary.get("EnterNoMoreThan1RefPoint"));
        }
        coords = cs[0];
    }

    public boolean verify(Point3d[] cs, GDocument document) {
        logger.info(Arrays.asList(cs));
        Vector3d n = GMath.cross(cs[0], cs[1], cs[2]);
        n.normalize();
        return !GMath.isInPlane(coords, cs[0], n);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<condition>");
        buf.append("\n<type>notThroughPoint</type>");
        buf.append("\n<coords>");
        buf.append(GStringUtils.coordsToString(coords));
        buf.append("</coords>");
        buf.append("\n</condition>");
    }

    public String getStringValue() {
        return GStringUtils.coordsToString(coords);
    }

    public String getDescription() {
        return GDictionary.get("NotThroughPoint");
    }
}
