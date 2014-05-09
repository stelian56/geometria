/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GCloneFigureAction implements GLoggable, GFigureAction {

    private String figureName;

    private String cloneName;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        figureName = figure.getName();
        logger.info(figureName);
        GFigure clonedFigure = figure.clone();
        cloneName = document.newFigureName();
        clonedFigure.setName(cloneName);
        document.addFigure(clonedFigure);
        documentHandler.addFigure(clonedFigure);
        document.setSelectedFigure(cloneName);
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        documentHandler.notepadChanged();
        logger.info(cloneName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.removeFigure(cloneName);
        documentHandler.removeFigure(cloneName);
        documentHandler.notepadChanged();
        logger.info(cloneName);
    }

    public GLoggable clone() {
        GCloneFigureAction action = new GCloneFigureAction();
        action.figureName = figureName;
        action.cloneName = cloneName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("CloneFigure", figureName, cloneName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("cloneFigure", figureName);
    }

    public String getFigureName() {
        return figureName;
    }
}
