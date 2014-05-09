/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GUndefinedItemException;
import net.geocentral.geometria.view.GLayAngleDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GLayAngleAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String angleString;

    private String pLabel1;

    private String pLabel2;

    private String[] fLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private GFace face;

    private List<GPoint3d> addedPoints;

    private List<GLine> addedLines;

    private List<GLine> removedLines;

    private String helpId;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        document = documentHandler.getActiveDocument();
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
            GNotepadRecord record = document.getNotepad().getSelectedRecord();
            if (record != null)
                angleString = record.getVariable().getName();
            try {
                validateApply();
            }
            catch (Exception exception) {
                GLayAngleDialog dialog = new GLayAngleDialog(documentHandler
                        .getOwnerFrame(), this);
                dialog.prefill(pLabel1, pLabel2, angleString);
                dialog.setVisible(true);
                if (!dialog.getResult())
                    return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + angleString + ", " + pLabel1 + ", "
                + pLabel2);
        return true;
    }

    private void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        if (selection.isEmpty())
            return;
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            String label;
            GStick s;
            if (element1 instanceof GPoint3d
                    && element2 instanceof GStick) {
                label = ((GPoint3d)element1).getLabel();
                s = (GStick)element2;
            }
            else if (element1 instanceof GStick
                    && element2 instanceof GPoint3d) {
                label = ((GPoint3d)element2).getLabel();
                s = (GStick)element1;
            }
            else
                return;
            if (label.equals(s.label1)) {
                pLabel1 = label;
                pLabel2 = s.label2;
            }
            else if (label.equals(s.label2)) {
                pLabel1 = label;
                pLabel2 = s.label1;
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        // Validate angle
        if (angleString.length() == 0) {
            logger.info("No angle");
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForAngle"));
        }
        GNotepadVariable variable = document.getVariable(angleString);
        Double angle;
        if (variable != null)
            angle = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            angle = GMath.evaluate(angleString, variables);
            if (angle == null) {
                logger.info("Bad expression: " + angleString);
                throw new Exception(
                        GDictionary.get("EnterValidExpressionForAngle"));
            }
        }
        if (angle <= 0 || angle >= Math.PI) {
            logger.info("Angle must be between 0 and pi: " + angle);
            throw new Exception(GDictionary.get("AngleMustBeBetween", "0",
                    " \u03C0"));
        }

        // Validate end points
        if (pLabel1.length() == 0 || pLabel2.length() == 0) {
            logger.info("No end points: " + pLabel1 + ", " + pLabel2);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        GPoint3d p1 = solid.getPoint(pLabel1);
        if (p1 == null) {
            logger.info("No point: " + pLabel1);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, pLabel1));
        }
        GPoint3d p2 = solid.getPoint(pLabel2);
        if (p2 == null) {
            logger.info("No point: " + pLabel2);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, pLabel2));
        }
        if (p1 == p2) {
            logger.info("Equal points: " + p1 + ", " + p2);
            throw new Exception(
                    GDictionary.get("PointsRefNoLine", pLabel1, pLabel2));
        }

        // Validate line
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                pLabel1, pLabel2 });
        Iterator<GFace> it = faces.iterator();
        if (faces.size() > 1) {
            if (fLabels == null) {
                String[] fLabelStrings = new String[2];
                for (int i = 0; i < 2; i++) {
                    GFace face = it.next();
                    StringBuffer buf = new StringBuffer();
                    buf.append(face.labelAt(0)).append(face.labelAt(1)).append(
                            face.labelAt(2));
                    fLabelStrings[i] = String.valueOf(buf);
                }
                throw new GUndefinedItemException(fLabelStrings);
            }
            else
                face = solid.facesThroughPoints(fLabels).iterator().next();
        }
        else
            face = it.next();
        GLine line1 = face.lineThroughPoints(pLabel1, pLabel2);
        if (line1 == null) {
            logger.info("No line: " + pLabel1 + ", " + pLabel2);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                pLabel1, pLabel2, figureName));
        }

        addedPoints = new ArrayList<GPoint3d>();
        addedLines = new ArrayList<GLine>();
        removedLines = new ArrayList<GLine>();
        Vector3d v = new Vector3d(p2.coords);
        v.sub(p1.coords);
        Vector3d n = face.getNormal(solid, solid.getGCenter());
        Vector3d[] vs = GMath.layAngle(v, n, angle);
        boolean outOfFace = true;
        for (int i = 0; i < 2; i++) {
            Object[] result = face.intersectRay(p1.coords, vs[i], solid);
            if (result == null)
                continue;
            outOfFace = false;
            GLine line = (GLine)result[0];
            Point3d pCoords = (Point3d)result[1];
            GPoint3d p = solid.getPoint(pCoords);
            if (p == null) {
                p = solid.addPoint(pCoords);
                GFace f = line.getFace();
                f.addPoint(p, solid);
                f = line.getTwin().getFace();
                f.addPoint(p, solid);
                addedPoints.add(p);
            }
            else {
                GLine l = face.lineThroughPoints(pLabel1, p.getLabel());
                if (l != null)
                    continue;
            }
            List<GLine> rLines = new ArrayList<GLine>();
            addedLines.add(face.addLine(p1, p, rLines, solid));
            removedLines.addAll(rLines);
        }
        if (addedLines.isEmpty()) {
            if (outOfFace) {
                logger.info("Outside figure: " + pLabel1 + ", " + pLabel2
                        + ", " + angle);
                throw new Exception(GDictionary.get("AngleIsOutsideFigure",
                    figureName));
            }
            else {
                logger.info("Already drawn: " + pLabel1 + ", " + pLabel2
                        + ", " + angle);
                throw new Exception(GDictionary.get("AngleAlreadyDrawnInFigure",
                    figureName));
            }
        }
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        for (GPoint3d p : addedPoints) {
            Collection<GFace> faces = solid.facesThroughPoint(p.getLabel());
            for (GFace face : faces)
                face.removePoint(p);
            solid.removePoint(p.getLabel());
        }
        face.undoAddLines(addedLines, removedLines);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + angleString + ", " + pLabel1 + ", "
                + pLabel2 + ", ");
    }

    public GLoggable clone() {
        GLayAngleAction action = new GLayAngleAction();
        action.figureName = figureName;
        if (fLabels != null) {
            action.fLabels = fLabels;
            for (int i = 0; i < 3; i++)
                action.fLabels[i] = fLabels[i];
        }
        action.angleString = angleString;
        action.pLabel1 = pLabel1;
        action.pLabel2 = pLabel2;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("LayAngleOffLineInFigure", angleString,
            pLabel1 + pLabel2, figureName);
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
        ns = node.getElementsByTagName("fLabel1");
        if (ns.getLength() > 0) {
            fLabels = new String[3];
            fLabels[0] = ns.item(0).getTextContent();
            ns = node.getElementsByTagName("fLabel2");
            if (ns.getLength() == 0) {
                logger.error("No fLabel2");
                throw new Exception();
            }
            fLabels[1] = ns.item(0).getTextContent();
            ns = node.getElementsByTagName("fLabel3");
            if (ns.getLength() == 0) {
                logger.error("No fLabel3");
                throw new Exception();
            }
            fLabels[2] = ns.item(0).getTextContent();
        }
        ns = node.getElementsByTagName("angle");
        if (ns.getLength() == 0) {
            logger.error("No angle");
            throw new Exception();
        }
        angleString = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p1Label");
        if (ns.getLength() == 0) {
            logger.error("No p1Label");
            throw new Exception();
        }
        pLabel1 = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p2Label");
        if (ns.getLength() == 0) {
            logger.error("No p2Label");
            throw new Exception();
        }
        pLabel2 = ns.item(0).getTextContent();
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
            .append("\n<angle>")
            .append(angleString)
            .append("</angle>");
        if (fLabels != null) {
            buf.append("\n<fLabel1>")
                .append(fLabels[0])
                .append("</fLabel1>")
                .append("\n<fLabel2>")
                .append(fLabels[1])
                .append("</fLabel2>")
                .append("\n<fLabel3>")
                .append(fLabels[2])
                .append("</fLabel3>");
        }
        buf.append("\n<p1Label>")
            .append(pLabel1)
            .append("</p1Label>")
            .append("\n<p2Label>")
            .append(pLabel2)
            .append("</p2Label>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String p1String, String p2String, String angleString,
            String fLabelsString) {
        logger.info(p1String + ", " + p2String + ", " + angleString + ", "
                + fLabelsString);
        pLabel1 = p1String.toUpperCase();
        pLabel2 = p2String.toUpperCase();
        this.angleString = angleString;
        if (fLabelsString != null) {
            fLabels = new String[3];
            StringBuffer buf = new StringBuffer();
            buf.append("(").append(GLabelFactory.LABEL_PATTERN).append(")")
                .append("(").append(GLabelFactory.LABEL_PATTERN)
                .append(")").append("(")
                .append(GLabelFactory.LABEL_PATTERN).append(")");
            Pattern pattern = Pattern.compile(String.valueOf(buf));
            Matcher matcher = pattern.matcher(fLabelsString);
            matcher.matches();
            for (int i = 0; i < 3; i++)
                fLabels[i] = matcher.group(i + 1);
        }
    }

    public String getShortDescription() {
        return
            GDictionary.get("layAngleOffLine", angleString, pLabel1 + pLabel2);
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
