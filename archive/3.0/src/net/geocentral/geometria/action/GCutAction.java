/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GCamera;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.view.GCutDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GCutAction implements GLoggable, GActionWithHelp {

    private String figureName;

    private String[] childFigureNames;

    private String[] pLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

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
                GCutDialog dialog = new GCutDialog(documentHandler
                        .getOwnerFrame(), this);
                dialog.prefill(pLabels[0], pLabels[1], pLabels[2]);
                dialog.setVisible(true);
                if (!dialog.getResult())
                    return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(childFigureNames[1]);
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + Arrays.asList(pLabels) + ", "
                + Arrays.asList(childFigureNames));
        return true;
    }

    private void prefill(Set<GSelectable> selection) {
        logger.info("");
        GDocument document = GDocumentHandler.getInstance().getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        GSolid solid = figure.getSolid();
        pLabels = new String[3];
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GStick
                    && element2 instanceof GStick) {
                pLabels[0] = ((GStick)element1).label1;
                pLabels[1] = ((GStick)element1).label2;
                pLabels[2] = ((GStick)element2).label1;
                Collection<GFace> faces = solid.facesThroughPoints(pLabels);
                if (!faces.isEmpty())
                    pLabels[2] = ((GStick)element2).label2;
            }
            else if (element1 instanceof GPoint3d
                    && element2 instanceof GStick
                    || element2 instanceof GPoint3d
                    && element1 instanceof GStick) {
                GPoint3d p;
                GStick s;
                if (element1 instanceof GPoint3d
                        && element2 instanceof GStick) {
                    p = (GPoint3d)element1;
                    s = (GStick)element2;
                    pLabels = new String[] {
                            p.getLabel(), s.label1, s.label2 };
                }
                else {
                    p = (GPoint3d)element2;
                    s = (GStick)element1;
                    pLabels = new String[] {
                            p.getLabel(), s.label1, s.label2 };
                }
            }
        }
        else if (selection.size() == 3) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            GSelectable element3 = it.next();
            if (element1 instanceof GPoint3d
                    && element2 instanceof GPoint3d
                    && element3 instanceof GPoint3d)
                pLabels = new String[] {
                    ((GPoint3d)element1).getLabel(),
                    ((GPoint3d)element2).getLabel(),
                    ((GPoint3d)element3).getLabel() };
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();

        // Validate reference points
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
        Collection<GFace> faces = solid.facesThroughPoints(pLabels);
        if (!faces.isEmpty()) {
            logger.info("No face: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("PointsBelongToSameFace",
                    pLabels[0], pLabels[1], pLabels[2]));
        }

        // Apply
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GFigure[] childFigures = new GFigure[2];
        childFigureNames = new String[2];
        Vector3d v1 = new Vector3d(ps[1].coords);
        v1.sub(ps[0].coords);
        Vector3d v2 = new Vector3d(ps[2].coords);
        v2.sub(ps[0].coords);
        Vector3d n = new Vector3d();
        n.cross(v1, v2);
        n.normalize();
        if (n.z < - GMath.EPSILON)
            n.scale(-1);
        else if (n.z < GMath.EPSILON) {
            if (n.y < - GMath.EPSILON)
                n.scale(-1);
            else if (n.y < GMath.EPSILON) {
                if (n.x < - GMath.EPSILON)
                    n.scale(-1);
            }
        }
        GSolid[] childSolids = new GSolid[2];
        for (int i = 0; i < 2; i++) {
            childSolids[i] = solid.clone();
            childSolids[i].cutOff(pLabels[0], n);
            n.scale(-1);
            childSolids[i].makeConfig();
            double zoomFactor =
                childSolids[i].getBoundingSphere().getRadius()
                / solid.getBoundingSphere().getRadius();
            childFigures[i] =
                documentHandler.newFigure(childSolids[i], zoomFactor);
            childFigures[i].setTransparent(figure.isTransparent());
            childFigures[i].setLabelled(figure.isLabelled());
            GCamera camera = figure.getCamera();
            GCamera c = childFigures[i].getCamera();
            c.setAttitude(new Matrix3d(camera.getAttitude()));
            c.setInitialAttitude(new Matrix3d(camera.getInitialAttitude()));
            Color baseColor = figure.getBaseColor();
            childFigures[i].setBaseColor(new Color(baseColor.getRGB()));
            childFigureNames[i] = childFigures[i].getName();
        }
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        for (int i = 0; i < 2; i++) {
            documentHandler.removeFigure(childFigureNames[i]);
            document.removeFigure(childFigureNames[i]);
        }
        document.setSelectedFigure(figureName);
        logger.info("Cut figure " + figureName + " into figures "
                + childFigureNames[0] + ", " + childFigureNames[1] + " undone");
        logger.info(figureName);
    }

    public GLoggable clone() {
        GCutAction action = new GCutAction();
        action.figureName = figureName;
        action.childFigureNames = childFigureNames.clone();
        action.pLabels = pLabels.clone();
        return action;
    }

    public String toLogString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GDictionary.get("CutFigureThroughPoints", figureName,
                pLabels[1], pLabels[0], pLabels[2]));
        return String.valueOf(buf);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
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

    public void setInput(String p0String, String p1String, String p2String) {
        logger.info(p0String + ", " + p1String + ", " + p2String);
        this.pLabels[0] = p0String.toUpperCase();
        this.pLabels[1] = p1String.toUpperCase();
        this.pLabels[2] = p2String.toUpperCase();
    }

    public String getShortDescription() {
        return GDictionary.get("cutFigure", figureName);
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
