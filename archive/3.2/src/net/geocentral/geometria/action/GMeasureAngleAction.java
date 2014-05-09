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

import javax.vecmath.Vector3d;

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
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GMeasureAngleDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GMeasureAngleAction implements GLoggable, GFigureAction,
        GActionWithHelp {

    private String figureName;

    private String variableName;

    private String[] pLabels;

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
            GMeasureAngleDialog dialog = new GMeasureAngleDialog(documentHandler.getOwnerFrame(), this);
            dialog.prefill(pLabels);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + variableName + ", "
                + Arrays.asList(pLabels));
        return true;
    }

    protected void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        pLabels = new String[3];
        if (selection.isEmpty())
            return;
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GStick
                    && element2 instanceof GStick) {
                if ((((GStick)element1).label1)
                        .equals(((GStick)element2).label1)) {
                    pLabels[0] = ((GStick)element1).label1;
                    pLabels[1] = ((GStick)element1).label2;
                    pLabels[2] = ((GStick)element2).label2;
                }
                else if ((((GStick)element1).label1)
                        .equals(((GStick)element2).label2)) {
                    pLabels[0] = ((GStick)element1).label1;
                    pLabels[1] = ((GStick)element1).label2;
                    pLabels[2] = ((GStick)element2).label1;
                }
                else if ((((GStick)element1).label2)
                        .equals(((GStick)element2).label1)) {
                    pLabels[0] = ((GStick)element1).label2;
                    pLabels[1] = ((GStick)element1).label1;
                    pLabels[2] = ((GStick)element2).label2;
                }
                else if ((((GStick)element1).label2
                        .equals(((GStick)element2).label2))) {
                    pLabels[0] = ((GStick)element1).label2;
                    pLabels[1] = ((GStick)element1).label1;
                    pLabels[2] = ((GStick)element2).label1;
                }
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(figureName);
        GSolid solid = figure.getSolid();

        // Validate end points
        GPoint3d[] ps = new GPoint3d[3];
        for (int i = 0; i < 3; i++) {
            if (pLabels[i].length() == 0) {
                logger.info("No end points: " + Arrays.asList(pLabels));
                throw new Exception(GDictionary.get("EnterEndPoints"));
            }
            ps[i] = solid.getPoint(pLabels[i]);
            if (ps[i] == null) {
                logger.info("No point: " + pLabels[i]);
                throw new Exception(GDictionary.get("FigureContainsNoPoint",
                        figureName, pLabels[i]));
            }
        }
        if (ps[0] == ps[1] || ps[0] == ps[2]) {
            logger.info("End points equal: " + Arrays.asList(ps));
            throw new Exception(GDictionary.get("EndPointsCannotBeEqual"));
        }

        // Validate lines
        Collection<GFace> faces = solid.facesThroughPoints(pLabels);
        if (faces.isEmpty()) {
            logger.info("Not in the same face: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("LinesDoNotBelongToSameFace",
                    pLabels[0] + pLabels[1], pLabels[0] + pLabels[2],
                    figureName));
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

        // Validate angle
        Vector3d v1 = new Vector3d(ps[1].coords);
        v1.sub(ps[0].coords);
        Vector3d v2 = new Vector3d(ps[2].coords);
        v2.sub(ps[0].coords);
        double angle = v1.angle(v2);
        if (angle < GMath.EPSILON) {
            logger.info("Null angle: " + angle);
            throw new Exception(GDictionary.get("AngleIsNull"));
        }

        // Apply
        GNotepadVariable variable = new GNotepadVariable(variableName, angle);
        GMeasurement measurement = GMeasurement.newAngle(pLabels, figureName);
        GNotepadRecord record = new GNotepadRecord(variable, measurement);
        GNotepad notepad = document.getNotepad();
        notepad.add(record);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GNotepad notepad = document.getNotepad();
        notepad.removeLastRecord();
        logger.info(figureName + ", " + variableName + ", "
                + Arrays.asList(pLabels));
    }

    public GLoggable clone() {
        GMeasureAngleAction action = new GMeasureAngleAction();
        action.figureName = figureName;
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++)
            action.pLabels[i] = pLabels[i];
        action.variableName = variableName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("MeasureAngleInFigure", variableName,
                pLabels[1] + pLabels[0] + pLabels[2], figureName);
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

        pLabels = new String[3];
        ns = node.getElementsByTagName("p0Label");
        if (ns.getLength() == 0) {
            logger.error("No p0Label");
            throw new Exception();
        }
        pLabels[0] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p1Label");
        if (ns.getLength() == 0) {
            logger.error("No p1Label");
            throw new Exception();
        }
        pLabels[1] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p2Label");
        if (ns.getLength() == 0) {
            logger.error("No p2Label");
            throw new Exception();
        }
        pLabels[2] = ns.item(0).getTextContent();
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
            .append("\n<p0Label>")
            .append(pLabels[0])
            .append("</p0Label>")
            .append("\n<p1Label>")
            .append(pLabels[1])
            .append("</p1Label>")
            .append("\n<p2Label>")
            .append(pLabels[2])
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

    public void setInput(String p0String, String p1String, String p2String,
            String variableName) {
        logger.info(p0String + ", " + p1String + ", " + p2String + ", "
                + variableName);
        this.pLabels[0] = p0String.toUpperCase();
        this.pLabels[1] = p1String.toUpperCase();
        this.pLabels[2] = p2String.toUpperCase();
        this.variableName = variableName;
    }

    public String getShortDescription() {
        return GDictionary.get("measureAngle",
                pLabels[1] + pLabels[0] + pLabels[2]);
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
