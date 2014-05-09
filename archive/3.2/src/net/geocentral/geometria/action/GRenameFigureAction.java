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
import net.geocentral.geometria.model.answer.GLineSetAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GRenameFigureAction implements GLoggable, GFigureAction {

    public static final String FIGURE_NAME_PATTERN = "[^\\>\\<\\&\\%\\s]+";

    private String oldFigureName;

    private String newFigureName;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure;
        if (silent) {
            figure = document.getFigure(oldFigureName);
        }
        else {
            figure = document.getSelectedFigure();
            oldFigureName = figure.getName();
            newFigureName = GGraphicsFactory.getInstance().showInputDialog(
                        GDictionary.get("RenameFigureInput", oldFigureName));
            if (newFigureName == null) {
                return false;
            }
            newFigureName = newFigureName.trim();
            if (newFigureName.length() == 0) {
                return false;
            }
            if (!newFigureName.matches(FIGURE_NAME_PATTERN)) {
                logger.error(newFigureName);
                GGraphicsFactory.getInstance().showErrorDialog(GDictionary.get("BadFigureName", newFigureName));
                return false;
            }
            if (document.getFigure(newFigureName) != null) {
                logger.error(newFigureName);
                GGraphicsFactory.getInstance().showErrorDialog(
                        GDictionary.get("DuplicateFigureName", newFigureName));
                return false;
            }
        }
        document.renameFigure(figure, newFigureName);
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        document.setSelectedFigure(newFigureName);
        documentHandler.renameFigure(oldFigureName, newFigureName);
        document.getNotepad().figureRenamed(this);
        documentHandler.notepadChanged();
        if (document instanceof GProblem) {
            GAnswer answer = ((GProblem)document).getAnswer();
            if (answer instanceof GConditionPlaneAnswer) {
                ((GConditionPlaneAnswer)answer).figureRenamed(this);
            }
            else if (answer instanceof GLineSetAnswer) {
                ((GLineSetAnswer)answer).figureRenamed(this);
            }
        }
        logger.info(oldFigureName + ", " + newFigureName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(newFigureName);
        document.renameFigure(figure, oldFigureName);
        document.setSelectedFigure(oldFigureName);
        documentHandler.renameFigure(newFigureName, oldFigureName);
        document.getNotepad().renameFigureUndone(this);
        documentHandler.notepadChanged();
        if (document instanceof GProblem) {
            GAnswer answer = ((GProblem)document).getAnswer();
            if (answer instanceof GConditionPlaneAnswer) {
                ((GConditionPlaneAnswer)answer).renameFigureUndone(this);
            }
            if (answer instanceof GLineSetAnswer) {
                ((GLineSetAnswer)answer).renameFigureUndone(this);
            }
        }
        logger.info(oldFigureName + ", " + newFigureName);
    }

    public GLoggable clone() {
        GRenameFigureAction action = new GRenameFigureAction();
        action.oldFigureName = oldFigureName;
        action.newFigureName = newFigureName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("RenameFigure", oldFigureName, newFigureName);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("oldFigureName");
        if (ns.getLength() == 0) {
            logger.error("No old figure name");
            throw new Exception();
        }
        oldFigureName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("newFigureName");
        if (ns.getLength() == 0) {
            logger.error("No new figure name");
            throw new Exception();
        }
        newFigureName = ns.item(0).getTextContent();
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
            .append("\n<oldFigureName>")
            .append(oldFigureName)
            .append("</oldFigureName>")
            .append("\n<newFigureName>")
            .append(newFigureName)
            .append("</newFigureName>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("renameFigure", oldFigureName, newFigureName);
    }

    public String getFigureName() {
        return newFigureName;
    }

    public String getOldFigureName() {
        return oldFigureName;
    }

    public String getNewFigureName() {
        return newFigureName;
    }
}
