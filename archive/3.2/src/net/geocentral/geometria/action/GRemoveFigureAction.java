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
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.model.answer.GConditionPlaneAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GRemoveFigureAction implements GLoggable {

    private String figureName;

    private GFigure figure;

    private int figureIndex;
    
    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        if (silent) {
            figure = document.getFigure(figureName);
        }
        else {
            figure = document.getSelectedFigure();
            figureName = figure.getName();
        }
        if (document instanceof GProblem) {
            GAnswer answer = ((GProblem)document).getAnswer();
            if (answer instanceof GConditionPlaneAnswer && ((GConditionPlaneAnswer)answer).isFigureReferenced(figureName)) {
                logger.info(figureName);
                documentHandler.error(GDictionary.get("CannotRemoveFigureRefInAnswer", figureName));
                return false;
            }
        }
        figureIndex = document.removeFigure(figureName);
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        documentHandler.removeFigure(figureName);
        document.getNotepad().figureRemoved(this);
        documentHandler.documentChanged();
        logger.info(figureName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.addFigure(figure, figureIndex);
        document.setSelectedFigure(figureName);
        documentHandler.addFigure(figure, figureIndex);
        document.getNotepad().removeFigureUndone(this);
        documentHandler.notepadChanged();
        logger.info(figureName);
    }

    public GLoggable clone() {
        GRemoveFigureAction action = new GRemoveFigureAction();
        action.figureName = figureName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("RemoveFigure", figureName);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
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
            .append("\n<figureName>")
            .append(figureName)
            .append("</figureName>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("removeFigure", figureName);
    }

    public String getFigureName() {
        return figureName;
    }
}
