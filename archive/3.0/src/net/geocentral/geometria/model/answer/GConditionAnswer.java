/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import javax.vecmath.Point3d;

import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.condition.GCondition;
import net.geocentral.geometria.model.answer.condition.GEquilateralTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GFigureCondition;
import net.geocentral.geometria.model.answer.condition.GIsoscellesTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughLineCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughPointCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToLineCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GParallelogramCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToLineCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GRectangleCondition;
import net.geocentral.geometria.model.answer.condition.GRectangularTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GRhombusCondition;
import net.geocentral.geometria.model.answer.condition.GSquareCondition;
import net.geocentral.geometria.model.answer.condition.GThroughLineCondition;
import net.geocentral.geometria.model.answer.condition.GThroughNoEdgeCondition;
import net.geocentral.geometria.model.answer.condition.GThroughNoVertexCondition;
import net.geocentral.geometria.model.answer.condition.GThroughPointCondition;
import net.geocentral.geometria.model.answer.condition.GVolumeCutInRatioCondition;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GConditionAnswer implements GAnswer {

    private GCondition[] conditions;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GConditionAnswer() {
        conditions = new GCondition[3];
    }

    public GConditionAnswer(GCondition[] conditions) {
        logger.info(conditions.length);
        this.conditions = conditions;
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("condition");
        if (ns.getLength() != 3) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            NodeList nns = n.getElementsByTagName("type");
            if (nns.getLength() > 0) {
                String type = nns.item(0).getTextContent();
                if (type.equals("throughPoint"))
                    conditions[i] = new GThroughPointCondition();
                else if (type.equals("throughLine"))
                    conditions[i] = new GThroughLineCondition();
                else if (type.equals("notThroughPoint"))
                    conditions[i] = new GNotThroughPointCondition();
                else if (type.equals("notThroughLine"))
                    conditions[i] = new GNotThroughLineCondition();
                else if (type.equals("throughNoVertex"))
                    conditions[i] = new GThroughNoVertexCondition();
                else if (type.equals("throughNoEdge"))
                    conditions[i] = new GThroughNoEdgeCondition();
                else if (type.equals("parallelToLine"))
                    conditions[i] = new GParallelToLineCondition();
                else if (type.equals("parallelToPlane"))
                    conditions[i] = new GParallelToPlaneCondition();
                else if (type.equals("perpendicularToLine"))
                    conditions[i] = new GPerpendicularToLineCondition();
                else if (type.equals("perpendicularToPlane"))
                    conditions[i] = new GPerpendicularToPlaneCondition();
                else if (type.equals("volumeCutInRatio"))
                    conditions[i] = new GVolumeCutInRatioCondition();
                else if (type.equals("isoscellesTriangle"))
                    conditions[i] = new GIsoscellesTriangleCondition();
                else if (type.equals("rectangularTriangle"))
                    conditions[i] = new GRectangularTriangleCondition();
                else if (type.equals("equilateralTriangle"))
                    conditions[i] = new GEquilateralTriangleCondition();
                else if (type.equals("parallelogram"))
                    conditions[i] = new GParallelogramCondition();
                else if (type.equals("rhombus"))
                    conditions[i] = new GRhombusCondition();
                else if (type.equals("rectangle"))
                    conditions[i] = new GRectangleCondition();
                else if (type.equals("square"))
                    conditions[i] = new GSquareCondition();
                else {
                    logger.error(type);
                    throw new Exception();
                }
                conditions[i].make(n, document);
            }
        }
    }

    public boolean validate(String valueString, String figureName,
            GDocument document) {
        Point3d[] cs;
        try {
            cs = GPointSetFactory.getInstance().fromString(
                valueString, figureName, document);
        }
        catch (Exception exception) {
            return false;
        }
        if (GMath.areCollinear(cs, GMath.EPSILON))
            return false;
        return verify(cs, document);
    }

    public boolean verify(Point3d[] coords, GDocument document) {
        logger.info(coords);
        for (GCondition condition : conditions) {
            if (condition != null && !condition.verify(coords, document))
                return false;
        }
        return true;
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<answer>");
        buf.append("\n<type>conditionPlane</type>");
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i] == null)
                buf.append("\n<condition/>");
            else
                conditions[i].serialize(buf);
        }
        buf.append("\n</answer>");
    }

    public void figureRenamed(GRenameFigureAction action) {
        logger.info("");
        for (GCondition condition : conditions) {
            if (condition instanceof GFigureCondition)
                ((GFigureCondition)condition).figureRenamed(action);
        }
    }

    public void renameFigureUndone(GRenameFigureAction action) {
        logger.info("");
        for (GCondition condition : conditions) {
            if (condition instanceof GFigureCondition)
                ((GFigureCondition)condition).renameFigureUndone(action);
        }
    }

    public GCondition[] getConditions() {
        return conditions;
    }

    public String toString() {
        return "Not implemented";
    }

    public boolean isFigureReferenced(String figureName) {
        for (GCondition condition : conditions) {
            if (condition instanceof GFigureCondition && ((GFigureCondition)
                    condition).getFigureName().equals(figureName))
                return true;
        }
        return false;
    }
}
