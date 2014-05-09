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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GDivideAngleDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDivideAngleAction implements GLoggable, GFigureAction, GActionWithHelp {

    protected String figureName;

    protected String numeratorString;

    protected String denominatorString;

    protected String[] pLabels;

    protected GDocument document;

    private GFigure figure;

    private GSolid solid;

    private GFace face;

    private GPoint3d addedPoint;

    private GLine addedLine;

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
            GDivideAngleDialog dialog = new GDivideAngleDialog(documentHandler.getOwnerFrame(), this, true);
            dialog.prefill(pLabels[0], pLabels[1], pLabels[2], null, null);
            dialog.setVisible(true);
            if (!dialog.getResult()) {
                return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        logger.info(figureName + ", " + Arrays.asList(pLabels) + ", " + numeratorString + ", " + denominatorString);
        return true;
    }

    protected void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        pLabels = new String[3];
        if (selection.isEmpty()) {
            return;
        }
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GStick && element2 instanceof GStick) {
                if (((GStick)element1).label1.equals(((GStick)element2).label1)) {
                    pLabels[0] = ((GStick)element1).label1;
                    pLabels[1] = ((GStick)element1).label2;
                    pLabels[2] = ((GStick)element2).label2;
                }
                else if (((GStick)element1).label1.equals(((GStick)element2).label2)) {
                    pLabels[0] = ((GStick)element1).label1;
                    pLabels[1] = ((GStick)element1).label2;
                    pLabels[2] = ((GStick)element2).label1;
                }
                else if (((GStick)element1).label2.equals(((GStick)element2).label1)) {
                    pLabels[0] = ((GStick)element1).label2;
                    pLabels[1] = ((GStick)element1).label1;
                    pLabels[2] = ((GStick)element2).label2;
                }
                else if (((GStick)element1).label2.equals(((GStick)element2).label2)) {
                    pLabels[0] = ((GStick)element1).label2;
                    pLabels[1] = ((GStick)element1).label1;
                    pLabels[2] = ((GStick)element2).label1;
                }
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        // Validate numerator
        if (numeratorString.length() == 0) {
            logger.info("No numerator");
            throw new Exception(GDictionary.get("EnterValidExpressionForNumerator"));
        }
        GNotepadVariable variable = document.getVariable(numeratorString);
        Double numerator;
        if (variable != null) {
            numerator = variable.getValue();
        }
        else {
            List<GNotepadVariable> variables = document.getNotepad().getVariables();
            numerator = GMath.evaluate(numeratorString, variables);
            if (numerator == null) {
                logger.info("Bad numerator: " + numeratorString);
                throw new Exception(GDictionary.get("EnterValidExpressionForNumerator"));
            }
        }
        if (numerator <= 0) {
            logger.info("Numerator must be positive: " + numerator);
            throw new Exception(GDictionary.get("NumeratorMustBePositive"));
        }

        // Validate denominator
        if (denominatorString.length() == 0) {
            logger.info("No denominator");
            throw new Exception(GDictionary.get("EnterValidExpressionForDenominator"));
        }
        variable = document.getVariable(denominatorString);
        Double denominator;
        if (variable != null) {
            denominator = variable.getValue();
        }
        else {
            List<GNotepadVariable> variables = document.getNotepad().getVariables();
            denominator = GMath.evaluate(denominatorString, variables);
            if (denominator == null) {
                logger.info("Bad denominator: " + denominatorString);
                throw new Exception(GDictionary.get("EnterValidExpressionForDenominator"));
            }
        }
        if (denominator <= 0) {
            logger.info("Denominator must be positive: " + denominator);
            throw new Exception(GDictionary.get("DenominatorMustBePositive"));
        }

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
                throw new Exception(GDictionary.get("FigureContainsNoPoint", figureName, pLabels[i]));
            }
        }
        if (ps[0] == ps[1] || ps[0] == ps[2]) {
            logger.info("Equal end points: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }

        // Validate lines
        Collection<GFace> faces = solid.facesThroughPoints(pLabels);
        if (faces.isEmpty()) {
            logger.info("No face: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get(
                "LinesDoNotBelongToSameFace", pLabels[0] + pLabels[1], pLabels[0] + pLabels[2], figureName));
        }
        face = faces.iterator().next();
        GLine line1 = face.lineThroughPoints(pLabels[0], pLabels[1]);
        if (line1 == null) {
            logger.info("No line: " + pLabels[0] + ", " + pLabels[1]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints", pLabels[0], pLabels[1], figureName));
        }
        GLine line2 = face.lineThroughPoints(pLabels[0], pLabels[2]);
        if (line2 == null) {
            logger.info("No line: " + pLabels[0] + ", " + pLabels[2]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints", pLabels[0], pLabels[2], figureName));
        }
        if (line1 == line2) {
            logger.info("No angle: " + line1 + ", " + line2);
            throw new Exception(GDictionary.get("LinesDoNotMakeAngle",
                pLabels[0] + pLabels[1], pLabels[0] + pLabels[2], figureName));
        }

        // Validate division line
        Vector3d v = GMath.divideAngle(ps[0].coords, ps[1].coords, ps[2].coords, numerator / denominator);
        List<GLine> lines = face.linesThroughPoint(pLabels[0]);
        for (GLine line : lines) {
            Vector3d v1 = new Vector3d(solid.getPoint(line.firstLabel()).coords);
            v1.sub(ps[0].coords);
            Vector3d v2 = new Vector3d(solid.getPoint(line.lastLabel()).coords);
            v2.sub(ps[0].coords);
            double epsilon = solid.getEpsilon();
            if (!line.firstLabel().equals(pLabels[0]) && GMath.areCooriented(v1, v, epsilon)
                    || !line.lastLabel().equals(pLabels[0]) && GMath.areCooriented(v2, v, epsilon)) {
                logger.info("Already divided");
                throw new Exception(GDictionary.get("AngleAlreadyDivided",
                    pLabels[1] + pLabels[0] + pLabels[2], numeratorString,
                    denominatorString, figureName));
            }
        }

        // Apply
        Object[] result = face.intersectRay(ps[0].coords, v, solid);
        GLine line = (GLine)result[0];
        Point3d pCoords = (Point3d)result[1];
        GPoint3d p = solid.getPoint(pCoords);
        if (p == null) {
            p = solid.addPoint(pCoords);
            GFace f = line.getFace();
            f.addPoint(p, solid);
            f = line.getTwin().getFace();
            f.addPoint(p, solid);
            addedPoint = p;
        }
        removedLines = new ArrayList<GLine>();
        addedLine = face.addLine(ps[0], p, removedLines, solid);
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        if (addedPoint != null) {
            Collection<GFace> faces = solid.facesThroughPoint(addedPoint.getLabel());
            for (GFace face : faces) {
                face.removePoint(addedPoint);
            }
            solid.removePoint(addedPoint.getLabel());
        }
        face.undoAddLine(addedLine, removedLines);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + Arrays.asList(pLabels) + ", " + numeratorString + ", " + denominatorString);
    }

    public GLoggable clone() {
        GDivideAngleAction action = new GDivideAngleAction();
        action.figureName = figureName;
        action.numeratorString = numeratorString;
        action.denominatorString = denominatorString;
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++) {
            action.pLabels[i] = pLabels[i];
        }
        return action;
    }

    public String toLogString() {
        return GDictionary.get("DivideAngleInRatio", pLabels[1] + pLabels[0] + pLabels[2], numeratorString,
                denominatorString, figureName);
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
        ns = node.getElementsByTagName("numerator");
        if (ns.getLength() == 0) {
            logger.error("No numerator");
            throw new Exception();
        }
        numeratorString = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("denominator");
        if (ns.getLength() == 0) {
            logger.error("No denominator");
            throw new Exception();
        }
        denominatorString = ns.item(0).getTextContent();
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
            .append("\n<numerator>")
            .append(numeratorString)
            .append("</numerator>")
            .append("\n<denominator>")
            .append(denominatorString)
            .append("</denominator>")
            .append("\n<p0Label>")
            .append(pLabels[0])
            .append("</p0Label>")
            .append("\n<p1Label>")
            .append(pLabels[1])
            .append("</p1Label>")
            .append("\n<p2Label>")
            .append(pLabels[2])
            .append("</p2Label>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String p0String, String p1String, String p2String,
            String numeratorString, String denominatorString) {
        logger.info(p0String + ", " + p1String + ", " + p2String + ", " + numeratorString + ", " + denominatorString);
        this.pLabels[0] = p0String.toUpperCase();
        this.pLabels[1] = p1String.toUpperCase();
        this.pLabels[2] = p2String.toUpperCase();
        this.numeratorString = numeratorString;
        this.denominatorString = denominatorString;
    }

    public String getShortDescription() {
        return GDictionary.get("divideAngleInRatio", pLabels[1] + pLabels[0]
               + pLabels[2], numeratorString, denominatorString);
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
