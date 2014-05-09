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
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GVolumeCutInRatioCondition implements GPlaneCondition, GFigureCondition {

    private String figureName;

    public double ratio;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() < 1) {
            logger.error("Figure name: " + ns.getLength());
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
        if (document.getFigure(figureName) == null) {
            logger.error(figureName);
            throw new Exception();
        }
        NodeList nns = node.getElementsByTagName("value");
        if (nns.getLength() < 1) {
            logger.error("Value: " + nns.getLength());
            throw new Exception();
        }
        String valueString = nns.item(0).getTextContent();
        Double r = GMath.evaluate(valueString);
        if (r == null) {
            logger.error(valueString);
            throw new Exception();
        }
        ratio = r;
    }

    public void validate(String valueString, GDocument document)
            throws Exception {
        logger.info(valueString);
        GFigure figure = document.getSelectedFigure();
        if (figure == null) {
            logger.info("No figure selected");
            throw new Exception(GDictionary.get("NoFigureSelected"));
        }
        if (valueString.length() == 0)
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForRatio"));
        figureName = figure.getName();
        GNotepadVariable variable = document.getVariable(valueString);
        if (variable != null)
            ratio = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            Double r = GMath.evaluate(valueString, variables);
            if (r == null) {
                logger.info("Bad expression: " + valueString);
                throw new Exception(
                        GDictionary.get("EnterValidExpressionForRatio"));
            }
            ratio = r;
        }
        if (ratio <= 0) {
            logger.info("Ratio must be positive: " + ratio);
            throw new Exception(
                    GDictionary.get("RatioMustBePositive"));
        }
    }

    public boolean verify(Point3d[] cs, GDocument document) {
        logger.info(Arrays.asList(cs));
        GFigure figure = document.getFigure(figureName);
        if (figure == null)
            return false;
        GSolid solid = figure.getSolid();
        Vector3d n = GMath.cross(cs[0], cs[1], cs[2]);
        n.normalize();
        GSolid fragment1 = solid.clone();
        GSolid fragment2 = solid.clone();
        try {
            fragment1.cutOff(cs[0], n);
            n.scale(-1);
            fragment2.cutOff(cs[0], n);
        }
        catch (Exception exception) {
            return false;
        }
        double r = fragment1.computeVolume() / fragment2.computeVolume();
        return Math.abs(r - ratio) < GMath.EPSILON
                || Math.abs(1 / r - ratio) < GMath.EPSILON;
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
        buf.append("\n<type>volumeCutInRatio</type>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n<value>");
        buf.append(String.valueOf(ratio));
        buf.append("</value>");
        buf.append("\n</condition>");
    }

    public String getStringValue() {
        return String.valueOf(ratio);
    }

    public String getFigureName() {
        return figureName;
    }

    public String getDescription() {
        return GDictionary.get("VolumeCutInRatio");
    }
}
