/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GSolidFactory;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GPrismAction implements GLoggable, GFigureAction {

    private String figureName;

    private Integer sideCount;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GPrismAction() {
    }
    
    public GPrismAction(String parameter) {
        sideCount = Integer.valueOf(parameter);
    }
    
    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent + ", " + sideCount);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        if (!silent && sideCount == null) {
            String input = GGraphicsFactory.getInstance().showInputDialog(GDictionary.get("EnterNumberOfSides"));
            if (input == null) {
                return false;
            }
            try {
                sideCount = Integer.parseInt(input);
            }
            catch (Exception exception) {
                documentHandler.error(GDictionary.get("BadSideCount", input));
                return false;
            }
            if (sideCount < 3) {
                documentHandler.error(GDictionary.get("BadSideCount", input));
                return false;
            }
        }
        GSolid solid = GSolidFactory.getInstance().newPrism(sideCount);
        GFigure figure = documentHandler.newFigure(solid);
        figureName = figure.getName();
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        documentHandler.notepadChanged();
        logger.info(figureName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.removeFigure(figureName);
        documentHandler.removeFigure(figureName);
        documentHandler.notepadChanged();
        logger.info(figureName);
    }

    public GLoggable clone() {
        GPrismAction action = new GPrismAction();
        action.figureName = figureName;
        action.sideCount = sideCount;
        return action;
    }

    public String toLogString() {
        String figureType = GDictionary.get("prism");
        return GDictionary.get("Create", figureType, figureName);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("sideCount");
        if (ns.getLength() < 1) {
            logger.error("No side count");
            throw new Exception();
        }
        sideCount = Integer.valueOf(ns.item(0).getTextContent());
        ns = node.getElementsByTagName("comments");
        if (ns.getLength() > 0) {
            String s = ns.item(0).getTextContent();
            comments = GStringUtils.fromXml(s);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>")
            .append("\n<className>")
            .append(this.getClass().getSimpleName())
            .append("</className>")
            .append("\n<sideCount>")
            .append(String.valueOf(sideCount))
            .append("</sideCount>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        String figureType = GDictionary.get("prism");
        return GDictionary.get("create", figureType, figureName);
    }

    public String getFigureName() {
        return figureName;
    }
}
