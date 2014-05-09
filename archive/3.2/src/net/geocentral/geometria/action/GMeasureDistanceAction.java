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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GMeasurement;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GMeasureDistanceDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GMeasureDistanceAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String p1Label;

    private String p2Label;

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
            Set<GSelectable> selection = solid.getSelection();
            prefill(selection);
            GMeasureDistanceDialog dialog = new GMeasureDistanceDialog(
                    documentHandler.getOwnerFrame(), this);
            dialog.prefill(p1Label, p2Label);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + p1Label + ", " + p2Label + ", "
                + variableName);
        return true;
    }

    private void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        if (selection.isEmpty())
            return;
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 1) {
            GSelectable element = it.next();
            if (element instanceof GStick) {
                p1Label = ((GStick)element).label1;
                p2Label = ((GStick)element).label2;
            }
        }
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GPoint3d
                    && element2 instanceof GPoint3d) {
                p1Label = ((GPoint3d)element1).getLabel();
                p2Label = ((GPoint3d)element2).getLabel();
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        // Validate end points
        if (p1Label.length() == 0 || p2Label.length() == 0) {
            logger.info("No end points: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(figureName);
        GSolid solid = figure.getSolid();
        GPoint3d p1 = solid.getPoint(p1Label);
        if (p1 == null) {
            logger.info("No point: " + p1Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p1Label));
        }
        GPoint3d p2 = solid.getPoint(p2Label);
        if (p2 == null) {
            logger.info("No point: " + p2Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p2Label));
        }
        if (p1 == p2) {
            logger.info("Equal points: " + p1 + ", " + p2);
            throw new Exception(GDictionary.get("EndPointsCannotBeEqual"));
        }
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                p1Label, p2Label });
        if (faces.isEmpty()) {
            logger.info("Not in the same face: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("PointsDoNotBelongToSameFace",
                    p1Label, p2Label, figureName));
        }

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
            throw new Exception(
                    GDictionary.get("InvalidVariable", variableName));
        }
        if (document.getVariable(variableName) != null) {
            logger.info("Duplicate variable: " + variableName);
            throw new Exception(GDictionary.get("DuplicateVariable",
                    variableName));
        }

        // Apply
        double distance = p1.coords.distance(p2.coords);
        GNotepadVariable variable =
            new GNotepadVariable(variableName, distance);
        String[] labels = {
                p1Label, p2Label };
        GMeasurement measurement = GMeasurement.newDistance(labels, figureName);
        GNotepadRecord record = new GNotepadRecord(variable, measurement);
        GNotepad notepad = document.getNotepad();
        notepad.add(record);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GNotepad notepad = document.getNotepad();
        notepad.removeLastRecord();
        logger.info(figureName + ", " + p1Label + ", " + p2Label + ", "
                + variableName);
    }

    public GLoggable clone() {
        GMeasureDistanceAction action = new GMeasureDistanceAction();
        action.figureName = figureName;
        action.p1Label = p1Label;
        action.p2Label = p2Label;
        action.variableName = variableName;
        return action;
    }

    public String toLogString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GDictionary.get("MeasureDistanceInFigure", variableName,
                p1Label + p2Label, figureName));
        return String.valueOf(buf);
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
            logger.error("No figureName");
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p1Label");
        if (ns.getLength() == 0) {
            logger.error("No p1Label");
            throw new Exception();
        }
        p1Label = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p2Label");
        if (ns.getLength() == 0) {
            logger.error("No p2Label");
            throw new Exception();
        }
        p2Label = ns.item(0).getTextContent();
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
            .append("\n<p1Label>")
            .append(p1Label)
            .append("</p1Label>")
            .append("\n<p2Label>")
            .append(p2Label)
            .append("</p2Label>")
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

    public void setInput(String p1String, String p2String,
            String variableName) {
        logger.info(p1String + ", " + p2String + ", " + variableName);
        p1Label = p1String.toUpperCase();
        p2Label = p2String.toUpperCase();
        this.variableName = variableName;
    }

    public String getShortDescription() {
        return GDictionary.get("measureDistance", p1Label + p2Label);
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
