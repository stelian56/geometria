/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GHamiltonianCycleCondition implements GLineSetCondition, GFigureCondition {

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

    public void validate(String valueString, GDocument document) throws Exception {
        logger.info(valueString);
        GFigure figure = document.getSelectedFigure();
        if (figure == null) {
            logger.info("No figure selected");
            throw new Exception(GDictionary.get("NoFigureSelected"));
        }
        figureName = figure.getName();
    }

    public boolean verify(List<GPoint3d[]> lines, GDocument document) {
        logger.info(lines);
        GFigure figure = document.getFigure(figureName);
        if (figure == null) {
            return false;
        }
        GSolid solid = figure.getSolid();
        Set<GPoint3d> vertices = new HashSet<GPoint3d>();
        for (Iterator<GPoint3d> iterator = solid.pointIterator(); iterator.hasNext();) {
            GPoint3d p = iterator.next();
            if (p.isVertex()) {
                vertices.add(p);
            }
        }
        Map<GPoint3d, Integer> psOccurrence = new HashMap<GPoint3d, Integer>();
        for (GPoint3d[] line : lines) {
            for (int pIndex = 0; pIndex < 2; pIndex++) {
                GPoint3d p = line[pIndex];
                if (!vertices.contains(p)) {
                    return false;
                }
                Integer pOccurrence = psOccurrence.get(p);
                if (pOccurrence == null) {
                    pOccurrence = 0;
                }
                else if (pOccurrence > 1) {
                    return false;
                }
                psOccurrence.put(p, pOccurrence + 1);
            }
        }
        if (psOccurrence.size() != vertices.size()) {
            return false;
        }
        for (Entry<GPoint3d, Integer> entry : psOccurrence.entrySet()) {
            if (entry.getValue() != 2) {
                return false;
            }
        }
        solid.selectLines(lines);
        figure.repaint();
        return true;
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<condition>");
        buf.append("\n<type>hamiltonianCycle</type>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n</condition>");
    }

    public String getStringValue() {
        throw new UnsupportedOperationException();
    }

    public String getFigureName() {
        return figureName;
    }

    public String getDescription() {
        return GDictionary.get("HamiltonianCycle");
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
}
