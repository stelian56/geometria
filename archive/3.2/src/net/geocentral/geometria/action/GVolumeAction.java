/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Arrays;

import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GMeasurement;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GVolumeDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GVolumeAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String variableName;

    private String helpId;

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
            try {
                validateApply();
            }
            catch (Exception exception) {
                return false;
            }
        }
        else {
            GFigure figure = document.getSelectedFigure();
            figureName = figure.getName();
            GSolid solid = figure.getSolid();
            GVolumeDialog dialog = new GVolumeDialog(documentHandler.getOwnerFrame(), this);
            dialog.setVisible(true);
            if (!dialog.getResult()) {
                return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + variableName);
        return true;
    }

    public void validateApply() throws Exception {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(figureName);
        GSolid solid = figure.getSolid();
        // Validate variable
        if (variableName.length() == 0) {
            logger.info("No variable");
            throw new Exception(GDictionary.get("EnterVariable"));
        }
        if (Arrays.asList(GVariable.RESERVED).contains(
                variableName.toLowerCase())) {
            logger.info("Reserved variable: " + variableName);
            throw new Exception(GDictionary.get("ReservedVariable",
                    variableName));
        }
        if (!variableName.matches(GLabelFactory.VARIABLE_NAME_PATTERN)) {
            logger.info("Bad variable: " + variableName);
            throw new Exception(GDictionary.get("InvalidVariable",
                    variableName));
        }
        if (document.getVariable(variableName) != null) {
            logger.info("Duplicate variable: " + variableName);
            throw new Exception(
                    GDictionary.get("DuplicateVariable", variableName));
        }
        // Apply
        double volume = solid.computeVolume();
        GNotepadVariable variable = new GNotepadVariable(variableName, volume);
        GMeasurement expression = GMeasurement.newVolume(figureName);
        GNotepadRecord record = new GNotepadRecord(variable, expression);
        GNotepad notepad = document.getNotepad();
        notepad.add(record);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GNotepad notepad = document.getNotepad();
        notepad.removeLastRecord();
        logger.info(figureName + ", " + variableName);
    }

    public GLoggable clone() {
        GVolumeAction action = new GVolumeAction();
        action.figureName = figureName;
        action.variableName = variableName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("MeasureVolumeOfFigure", variableName, figureName);
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
        ns = node.getElementsByTagName("variableName");
        if (ns.getLength() == 0) {
            logger.error("No variable name");
            throw new Exception();
        }
        variableName = ns.item(0).getTextContent();
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
            .append("</figureName>")
            .append("\n<variableName>")
            .append(variableName)
            .append("</variableName>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String variableName) {
        logger.info(variableName);
        this.variableName = variableName;
    }

    public String getShortDescription() {
        return GDictionary.get("measureVolume", figureName);
    }

    public String getFigureName() {
        return figureName;
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
