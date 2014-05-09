/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import java.util.List;

import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.condition.GCondition;
import net.geocentral.geometria.model.answer.condition.GFigureCondition;
import net.geocentral.geometria.model.answer.condition.GHamiltonianCycleCondition;
import net.geocentral.geometria.model.answer.condition.GLineSetCondition;
import net.geocentral.geometria.util.GLineSetUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GLineSetAnswer implements GAnswer {

    private GLineSetCondition[] conditions;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLineSetAnswer() {
        conditions = new GLineSetCondition[1];
    }

    public GLineSetAnswer(GLineSetCondition[] conditions) {
        logger.info(conditions.length);
        this.conditions = conditions;
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("condition");
        if (ns.getLength() != 1) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            NodeList nns = n.getElementsByTagName("type");
            if (nns.getLength() > 0) {
                String type = nns.item(0).getTextContent();
                if (type.equals("hamiltonianCycle"))
                    conditions[i] = new GHamiltonianCycleCondition();
                else {
                    logger.error(type);
                    throw new Exception();
                }
                conditions[i].make(n, document);
            }
        }
    }
    
    public boolean validate(String valueString, String figureName, GDocument document) {
        List<GPoint3d[]> lines;
        try {
            lines = GLineSetUtils.fromString(valueString, figureName, document);
        }
        catch (Exception exception) {
            return false;
        }
        return verify(lines, document);
    }

    public boolean verify(List<GPoint3d[]> lines, GDocument document) {
        logger.info(lines);
        for (GLineSetCondition condition : conditions) {
            if (condition != null && !condition.verify(lines, document))
                return false;
        }
        return true;
    }

    public void serialize(StringBuffer buf, boolean lock) {
        logger.info("");
        buf.append("\n<answer>");
        buf.append("\n<type>lineSet</type>");
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i] == null) {
                buf.append("\n<condition/>");
            }
            else {
                conditions[i].serialize(buf);
            }
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
