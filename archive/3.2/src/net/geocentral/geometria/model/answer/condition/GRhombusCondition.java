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

import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GRhombusCondition implements GPlaneCondition, GFigureCondition {

    private String figureName;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() < 1) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
        if (document.getFigure(figureName) == null) {
            logger.error(figureName);
            throw new Exception();
        }
    }

    public void validate(String valueString, GDocument document)
            throws Exception {
        logger.info("");
        GFigure figure = document.getSelectedFigure();
        if (figure == null) {
            logger.info("No figure selected");
            throw new Exception(GDictionary.get("NoFigureSelected"));
        }
        figureName = figure.getName();
    }

    public boolean verify(Point3d[] cs, GDocument document) {
        logger.info(Arrays.asList(cs));
        GFigure figure = document.getFigure(figureName);
        if (figure == null)
            return false;
        GSolid solid = figure.getSolid();
        Vector3d n = GMath.cross(cs[0], cs[1], cs[2]);
        n.normalize();
        GSolid fragment = solid.clone();
        try {
            fragment.cutOff(cs[0], n);
        }
        catch (Exception exception) {
            return false;
        }
        int faceCount = fragment.faceCount();
        GFace face = fragment.faceAt(faceCount - 1);
        return face.isRhombus(fragment);
    }

    public void figureRenamed(GRenameFigureAction action) {
        logger.info("");
        if (action.getOldFigureName().equals(figureName))
            figureName = action.getNewFigureName();
    }

    public void renameFigureUndone(GRenameFigureAction action) {
        logger.info("");
        if (action.getNewFigureName().equals(figureName))
            figureName = action.getOldFigureName();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<condition>");
        buf.append("\n<type>rhombus</type>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n</condition>");
    }

    public String getDescription() {
        return GDictionary.get("Rhombus");
    }

    public String getStringValue() {
        return "Not implemented";
    }

    public String getFigureName() {
        return figureName;
    }
}
