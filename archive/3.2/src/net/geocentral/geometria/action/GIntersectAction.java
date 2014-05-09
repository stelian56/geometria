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

import javax.vecmath.Point3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GIntersectDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GIntersectAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String[] pLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private String intersectionLabel;

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
            try {
                validateApply();
            }
            catch (Exception exception) {
                GIntersectDialog dialog = new GIntersectDialog(documentHandler
                        .getOwnerFrame(), this);
                dialog.prefill(pLabels);
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
        logger.info(figureName + ", " + Arrays.asList(pLabels));
        return true;
    }

    private void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        pLabels = new String[4];
        if (selection.isEmpty())
            return;
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GStick && element2 instanceof GStick) {
                pLabels[0] = ((GStick)element1).label1;
                pLabels[1] = ((GStick)element1).label2;
                pLabels[2] = ((GStick)element2).label1;
                pLabels[3] = ((GStick)element2).label2;
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        GPoint3d[] ps = new GPoint3d[4];
        // Validate end points
        for (int i = 0; i < 4; i++) {
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
        if (ps[0] == ps[1]) {
            logger.info("Equal points: " + ps[0] + ", " + ps[1]);
            throw new Exception(GDictionary.get("PointsRefNoLine", pLabels[0],
                    pLabels[1]));
        }
        if (ps[2] == ps[3]) {
            logger.info("Equal points: " + ps[2] + ", " + ps[3]);
            throw new Exception(GDictionary.get("PointsRefNoLine", pLabels[2],
                    pLabels[3]));
        }
        // Validate lines
        Collection<GFace> faces = solid.facesThroughPoints(pLabels);
        if (faces.isEmpty()) {
            logger.info("Do not intersect: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("LinesDoNotIntersect",
                pLabels[0] + pLabels[1], pLabels[2] + pLabels[3], figureName));
        }
        GFace face = faces.iterator().next();
        GLine line1 = face.lineThroughPoints(pLabels[0], pLabels[1]);
        if (line1 == null) {
            logger.info("No line: " + pLabels[0] + ", " + pLabels[1]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                    pLabels[0], pLabels[1], figureName));
        }
        GLine line2 = face.lineThroughPoints(pLabels[2], pLabels[3]);
        if (line2 == null) {
            logger.info("No line: " + pLabels[2] + ", " + pLabels[3]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                    pLabels[2], pLabels[3], figureName));
        }
        // Find intersection point
        Point3d intersectionCoords = GMath.intersect(ps[0].coords,
                ps[1].coords, ps[2].coords, ps[3].coords, solid.getEpsilon());
        if (intersectionCoords == null) {
            logger.info("Do not intersect: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("LinesDoNotIntersect",
                    pLabels[0] + pLabels[1], pLabels[2] + pLabels[3], figureName));
        }
        // Validate intersection point
        if (!face.covers(intersectionCoords, solid)) {
            logger.info("Intersect outside: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("LinesIntersectOutsideFigure",
                pLabels[0] + pLabels[1], pLabels[2] + pLabels[3],
                figureName));
        }
        GPoint3d p = solid.getPoint(intersectionCoords);
        if (p != null) {
            logger.info("Already intersected: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("LinesAlreadyIntersected",
                pLabels[0] + pLabels[1], pLabels[2] + pLabels[3], figureName));
        }
        // Apply
        p = solid.addPoint(intersectionCoords);
        intersectionLabel = p.getLabel();
        line1.addPoint(p, solid);
        line2.addPoint(p, solid);
        faces = solid.facesThroughPoints(new String[] {
                pLabels[0], pLabels[1] });
        for (GFace f : faces)
            if (f != face)
                f.addPoint(p, solid);
        faces = solid.facesThroughPoints(new String[] {
                pLabels[2], pLabels[3] });
        for (GFace f : faces)
            if (f != face)
                f.addPoint(p, solid);
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(figureName);
        GSolid solid = figure.getSolid();
        GPoint3d p = solid.getPoint(intersectionLabel);
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                pLabels[0], pLabels[1] });
        faces.addAll(solid.facesThroughPoints(new String[] {
                pLabels[2], pLabels[3] }));
        for (GFace face : faces)
            face.removePoint(p);
        solid.removePoint(intersectionLabel);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + Arrays.asList(pLabels));
    }

    public GLoggable clone() {
        GIntersectAction action = new GIntersectAction();
        action.figureName = figureName;
        action.pLabels = new String[4];
        for (int i = 0; i < 4; i++)
            action.pLabels[i] = pLabels[i];
        return action;
    }

    public String toLogString() {
        return GDictionary.get("IntersectLinesInFigure",
                pLabels[0] + pLabels[1], pLabels[2] + pLabels[3],
                figureName);
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
        pLabels = new String[4];
        ns = node.getElementsByTagName("p11Label");
        if (ns.getLength() == 0) {
            logger.error("No p11Label");
            throw new Exception();
        }
        pLabels[0] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p12Label");
        if (ns.getLength() == 0) {
            logger.error("No p12Label");
            throw new Exception();
        }
        pLabels[1] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p21Label");
        if (ns.getLength() == 0) {
            logger.error("No p21Label");
            throw new Exception();
        }
        pLabels[2] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("p22Label");
        if (ns.getLength() == 0) {
            logger.error("No p22Label");
            throw new Exception();
        }
        pLabels[3] = ns.item(0).getTextContent();
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
            .append("\n<p11Label>")
            .append(pLabels[0])
            .append("</p11Label>")
            .append("\n<p12Label>")
            .append(pLabels[1])
            .append("</p12Label>")
            .append("\n<p21Label>")
            .append(pLabels[2])
            .append("</p21Label>")
            .append("\n<p22Label>")
            .append(pLabels[3])
            .append("</p22Label>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String p11String, String p12String, String p21String,
            String p22String) {
        logger.info(p11String + ", " + p12String + ", " + p21String + ", "
                + p22String);
        pLabels[0] = p11String.toUpperCase();
        pLabels[1] = p12String.toUpperCase();
        pLabels[2] = p21String.toUpperCase();
        pLabels[3] = p22String.toUpperCase();
    }

    public String getShortDescription() {
        logger.info("");
        return GDictionary.get("intersectLines", pLabels[0] + pLabels[1], pLabels[2] + pLabels[3]);
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
