/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Arrays;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.view.GShearDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GShearAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String[] pLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private Vector3d v1;

    private Vector3d v2;

    private Point3d p0;

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
            GShearDialog dialog = new GShearDialog(documentHandler
                    .getOwnerFrame(), this);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
            solid.clearSelection();
        }
        document.getNotepad().update();
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + Arrays.asList(pLabels));
        return true;
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();

        // Validate end points
        GPoint3d[] ps = new GPoint3d[3];
        for (int i = 0; i < 3; i++) {
            if (pLabels[i].length() == 0) {
                logger.info("No reference points");
                throw new Exception(GDictionary.get("EnterRefPoints"));
            }
            ps[i] = solid.getPoint(pLabels[i]);
            if (ps[i] == null) {
                logger.info("No point: " + pLabels[i]);
                throw new Exception(GDictionary.get("FigureContainsNoPoint",
                        figureName, pLabels[i]));
            }
        }
        if (GMath.areCollinear(new Point3d[] {
              ps[0].coords, ps[1].coords, ps[2].coords }, solid.getEpsilon())) {
            logger.info("Collinear points: " + Arrays.asList(pLabels));
            throw new Exception(GDictionary.get("PointsAreCollinear",
                    pLabels[0], pLabels[1], pLabels[2]));
        }

        // Apply
        v1 = new Vector3d(ps[1].coords);
        v1.sub(ps[0].coords);
        v2 = new Vector3d(ps[2].coords);
        v2.sub(ps[0].coords);
        p0 = new Point3d(ps[0].coords);
        solid.shear(p0, v1, v2);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.undoShear(p0, v1, v2);
        document.getNotepad().update();
        document.setSelectedFigure(figureName);
        figure.repaint();
        logger.info(figureName + Arrays.asList(pLabels));
    }

    public GLoggable clone() {
        GShearAction action = new GShearAction();
        action.figureName = figureName;
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++)
            action.pLabels[i] = pLabels[i];
        return action;
    }

    public String toLogString() {
        return GDictionary.get("ShearFigureAlong", figureName,
                pLabels[0] + pLabels[1], pLabels[2]);
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
        logger.info("");
    }

    public void setInput(String p0String, String p1String, String p2String) {
        logger.info(p0String + ", " + p1String + ", " + p2String);
        pLabels = new String[3];
        pLabels[0] = p0String.toUpperCase();
        pLabels[1] = p1String.toUpperCase();
        pLabels[2] = p2String.toUpperCase();
    }

    public String getShortDescription() {
        return GDictionary.get("shearFigure", figureName);
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
