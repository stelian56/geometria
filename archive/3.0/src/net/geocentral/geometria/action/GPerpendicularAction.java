/**
 * Copyright 2000-2010 Geometria Contributors
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GUndefinedItemException;
import net.geocentral.geometria.view.GPerpendicularDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GPerpendicularAction implements GLoggable, GFigureAction,
        GActionWithHelp {

    private String figureName;

    private String[] pLabels;

    private String[] fLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private GFace face;

    private List<GPoint3d> addedPoints;

    private GLine addedLine;

    private List<GLine> removedLines;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        document = documentHandler.getActiveDocument();
        if (quietMode) {
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
            try {
                validateApply();
            }
            catch (Exception exception) {
                GPerpendicularDialog dialog = new GPerpendicularDialog(
                        documentHandler.getOwnerFrame(), this);
                dialog.prefill(pLabels[0], pLabels[1], pLabels[2]);
                dialog.setVisible(true);
                if (!dialog.getResult())
                    return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + Arrays.asList(pLabels));
        return true;
    }

    private void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        pLabels = new String[3];
        if (selection.isEmpty())
            return;
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            GPoint3d p;
            GStick s;
            if (element1 instanceof GPoint3d && element2 instanceof GStick) {
                p = (GPoint3d)element1;
                s = (GStick)element2;
            }
            else if (element1 instanceof GStick && element2 instanceof GPoint3d) {
                p = (GPoint3d)element2;
                s = (GStick)element1;
            }
            else
                return;
            pLabels[0] = p.getLabel();
            pLabels[1] = s.label1;
            pLabels[2] = s.label2;
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        double epsilon = solid.getEpsilon();

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
        if (ps[1] == ps[2]) {
            logger.info("Equal end points: " + ps[1] + ", " + ps[2]);
            throw new Exception(GDictionary.get("EndPointsCannotBeEqual"));
        }

        // Validate line
        Collection<GFace> faces = solid.facesThroughPoints(pLabels);
        if (faces.isEmpty()) {
            logger.info("Not in the same face: " + Arrays.asList(pLabels));
            throw new Exception(
                    GDictionary.get("PointLineDoNotBelongToSameFace",
                    pLabels[0], pLabels[1] + pLabels[2], figureName));
        }
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
        GLine line1 = face.lineThroughPoints(pLabels[1], pLabels[2]);
        if (line1 == null) {
            logger.info("No line: " + pLabels[1] + ", " + pLabels[2]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                    pLabels[1], pLabels[2], figureName));
        }

        if (line1.contains(pLabels[0])) {
            // Raised perpendicular
            // Validate perpendicular
            Vector3d v = new Vector3d(ps[2].coords);
            v.sub(ps[1].coords);
            List<Point3d> iPoints = face.intersectPlane(ps[0].coords, v, solid);
            if (iPoints.size() < 2) {
                logger.info("Outside figure: " + Arrays.asList(pLabels));
                throw new Exception(
                        GDictionary.get("PerpendicularIsOutsideFigure",
                        pLabels[0], pLabels[1] + pLabels[2], figureName));
            }
            GPoint3d p1 = solid.getPoint(iPoints.get(0));
            GPoint3d p2 = solid.getPoint(iPoints.get(1));
            if (p1 != null && p2 != null && face.lineThroughPoints(
                    p1.getLabel(), p2.getLabel()) != null) {
                logger.info("Already drawn: " + Arrays.asList(pLabels));
                throw new Exception(
                        GDictionary.get("PerpendicularAlreadyDrawn",
                        pLabels[0], pLabels[1] + pLabels[2], figureName));
            }
            // Apply
            addedPoints = new ArrayList<GPoint3d>();
            if (p1 == null) {
                GPoint3d p = solid.addPoint(iPoints.get(0));
                addedPoints.add(p);
                p1 = p;
            }
            if (p2 == null) {
                GPoint3d p = solid.addPoint(iPoints.get(1));
                addedPoints.add(p);
                p2 = p;
            }
            for (GPoint3d p : addedPoints) {
                for (int i = 0; i < face.sideCount(); i++) {
                    GLine line = face.lineAt(i);
                    GPoint3d pp1 = solid.getPoint(line.firstLabel());
                    GPoint3d pp2 = solid.getPoint(line.lastLabel());
                    if (GMath.isBetween(p.coords, pp1.coords, pp2.coords,
                            epsilon)) {
                        GFace f = line.getFace();
                        f.addPoint(p, solid);
                        f = line.getTwin().getFace();
                        f.addPoint(p, solid);
                        break;
                    }
                }
            }
            removedLines = new ArrayList<GLine>();
            addedLine = face.addLine(p1, p2, removedLines, solid);
        }
        else {
            // Dropped perpendicular
            // Validate perpendicular
            Point3d pr0 = GMath.project(ps[0].coords, ps[1].coords,
                    ps[2].coords);
            GPoint3d p = solid.getPoint(pr0);
            Vector3d v = new Vector3d(pr0);
            v.sub(ps[0].coords);
            Object[] result = face.intersectRay(ps[0].coords, v, solid);
            if (result == null) {
                logger.info("Outside figure: " + Arrays.asList(pLabels));
                throw new Exception(
                        GDictionary.get("PerpendicularIsOutsideFigure",
                        pLabels[0], pLabels[1] + pLabels[2], figureName));
            }
            Point3d pCoords = (Point3d)result[1];
            p = solid.getPoint(pCoords);
            if (p != null) {
                Collection<GLine> lines = solid.linesThroughPoints(
                        p.getLabel(), pLabels[0]);
                if (!lines.isEmpty()) {
                    logger.info("Already drawn: " + Arrays.asList(pLabels));
                    throw new Exception(
                            GDictionary.get("PerpendicularAlreadyDrawn",
                            pLabels[0], pLabels[1] + pLabels[2], figureName));
                }
            }
            // Apply
            GLine line = (GLine)result[0];
            if (p == null) {
                p = solid.addPoint(pCoords);
                GFace f = line.getFace();
                f.addPoint(p, solid);
                f = line.getTwin().getFace();
                f.addPoint(p, solid);
                addedPoints = new ArrayList<GPoint3d>();
                addedPoints.add(p);
            }
            removedLines = new ArrayList<GLine>();
            addedLine = face.addLine(ps[0], p, removedLines, solid);
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
        face.undoAddLine(addedLine, removedLines);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info("Perpendicular through " + pLabels[0] + " to " + pLabels[1]
                + pLabels[2] + " in figure " + figureName + " undone");
        logger.info(figureName + ", " + Arrays.asList(pLabels));
    }

    public GLoggable clone() {
        GPerpendicularAction action = new GPerpendicularAction();
        action.figureName = figureName;
        if (fLabels != null) {
            action.fLabels = fLabels;
            for (int i = 0; i < 3; i++)
                action.fLabels[i] = fLabels[i];
        }
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++)
            action.pLabels[i] = pLabels[i];
        return action;
    }

    public String toLogString() {
        if (fLabels != null) {
            String s = fLabels[0] + fLabels[1] + fLabels[2];
            return GDictionary.get("DrawPerpendicularInFace", pLabels[0],
                    pLabels[1] + pLabels[2], s, figureName);
        }
        else {
            return GDictionary.get("DrawPerpendicularInFigure", pLabels[0],
                    pLabels[1] + pLabels[2], figureName);
        }
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
        if (fLabels != null) {
            buf.append("\n<fLabel1>");
            buf.append(fLabels[0]);
            buf.append("</fLabel1>");
            buf.append("\n<fLabel2>");
            buf.append(fLabels[1]);
            buf.append("</fLabel2>");
            buf.append("\n<fLabel3>");
            buf.append(fLabels[2]);
            buf.append("</fLabel3>");
        }
        buf.append("\n<p0Label>");
        buf.append(pLabels[0]);
        buf.append("</p0Label>");
        buf.append("\n<p1Label>");
        buf.append(pLabels[1]);
        buf.append("</p1Label>");
        buf.append("\n<p2Label>");
        buf.append(pLabels[2]);
        buf.append("</p2Label>");
        buf.append("\n</action>");
    }

    public void setInput(String p0String, String p1String, String p2String,
            String fLabelsString) {
        logger.info(p0String + ", " + p1String + ", " + p2String + ", "
                + fLabelsString);
        this.pLabels[0] = p0String.toUpperCase();
        this.pLabels[1] = p1String.toUpperCase();
        this.pLabels[2] = p2String.toUpperCase();
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
        return GDictionary.get("drawPerpendicular", pLabels[0],
                pLabels[1] + pLabels[2]);
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
