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

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GLayDistanceDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GLayDistanceAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String distanceString;

    private String[] pLabels;

    private GDocument document;

    private List<String> addedPointLabels;

    private GFigure figure;

    private GSolid solid;

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
                distanceString = record.getVariable().getName();
            try {
                validateApply();
            }
            catch (Exception exception) {
                GLayDistanceDialog dialog = new GLayDistanceDialog(
                        documentHandler.getOwnerFrame(), this);
                dialog.prefill(pLabels, distanceString);
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
        logger.info(figureName + ", " + distanceString + ", "
                + Arrays.asList(pLabels));
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
            else if (element1 instanceof GStick
                    && element2 instanceof GPoint3d) {
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
        // Validate distance
        if (distanceString.length() == 0) {
            logger.info("No distance");
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForDistance"));
        }
        GNotepadVariable variable = document.getVariable(distanceString);
        Double distance;
        if (variable != null)
            distance = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            distance = GMath.evaluate(distanceString, variables);
            if (distance == null) {
                logger.info("Bad expression: " + distanceString);
                throw new Exception(
                        GDictionary.get("EnterValidExpressionForDistance"));
            }
        }
        if (distance <= 0) {
            logger.info("Distance must be positive: " + distance);
            throw new Exception(GDictionary.get("DistanceMustBePositive"));
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
                throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, pLabels[i]));
            }
        }
        if (ps[1] == ps[2]) {
            logger.info("Equal points: " + Arrays.asList(ps));
            throw new Exception(GDictionary.get("PointsRefNoLine", pLabels[1],
                  pLabels[2]));
        }

        // Validate line
        if (solid.linesThroughPoints(pLabels[1], pLabels[2]).isEmpty()) {
            logger.info("No line: " + pLabels[1] + ", " + pLabels[2]);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                pLabels[1], pLabels[2], figureName));
        }

        // Validate added point
        List<Point3d> pCoords = GMath.intersectSphere(ps[1].coords,
                ps[2].coords, ps[0].coords, distance);
        if (pCoords.isEmpty()) {
            logger.info("No point at distance: " + pLabels[1] + ", "
                    + pLabels[2] + ", " + distanceString + ", " + pLabels[0]);
            throw new Exception(
                    GDictionary.get("LineContainsNoPointAtDistance",
                pLabels[1] + pLabels[2], distanceString, pLabels[0]));
        }
        boolean alreadyLaid = true;
        for (Point3d pCs : pCoords)
            alreadyLaid &= solid.getPoint(pCs) != null;
        if (alreadyLaid) {
            logger.info("Already laid: " + pLabels[1] + ", "
                    + pLabels[2] + ", " + distanceString + ", " + pLabels[0]);
            throw new Exception(GDictionary.get("DistanceAlreadyLaidOff",
                distanceString, pLabels[0], pLabels[1] + pLabels[2],
                figureName));
        }

        // Apply
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                pLabels[1], pLabels[2] });
        addedPointLabels = new ArrayList<String>();
        for (Point3d pCs : pCoords) {
            if (solid.getPoint(pCs) == null) {
                GPoint3d p = solid.addPoint(pCs);
                addedPointLabels.add(p.getLabel());
                for (GFace face : faces)
                    face.addPoint(p, solid);
            }
        }
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        for (String pLabel : addedPointLabels) {
            GPoint3d p = solid.getPoint(pLabel);
            Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                    pLabels[1], pLabels[2] });
            for (GFace face : faces)
                face.removePoint(p);
            solid.removePoint(pLabel);
        }
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + distanceString + ", "
                + Arrays.asList(pLabels));
    }

    public GLoggable clone() {
        GLayDistanceAction action = new GLayDistanceAction();
        action.figureName = figureName;
        action.distanceString = distanceString;
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++)
            action.pLabels[i] = pLabels[i];
        action.addedPointLabels = new ArrayList<String>();
        for (String label : addedPointLabels)
            action.addedPointLabels.add(label);
        return action;
    }

    public String toLogString() {
        return GDictionary.get("LayDistanceOffPointToLine", distanceString,
                pLabels[0], pLabels[1] + pLabels[2], figureName);
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
        ns = node.getElementsByTagName("distance");
        if (ns.getLength() == 0) {
            logger.error("No distance");
            throw new Exception();
        }
        distanceString = ns.item(0).getTextContent();
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
            .append("\n<distance>")
            .append(distanceString)
            .append("</distance>")
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
            String distanceString) {
        logger.info(p0String + ", " + p1String + ", " + p2String + ", "
                + distanceString);
        pLabels[0] = p0String.toUpperCase();
        pLabels[1] = p1String.toUpperCase();
        pLabels[2] = p2String.toUpperCase();
        this.distanceString = distanceString;
    }

    public String getShortDescription() {
        return GDictionary.get("layDistanceOffPointToLine", distanceString, pLabels[0],
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
