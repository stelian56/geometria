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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.view.GDrawLineDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDrawLineAction implements GLoggable, GFigureAction,
        GActionWithHelp {

    private String figureName;

    private String p1Label;

    private String p2Label;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private GFace face;

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
                GDrawLineDialog dialog = new GDrawLineDialog(documentHandler
                        .getOwnerFrame(), this);
                dialog.prefill(p1Label, p2Label);
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
        logger.info(figureName + ", " + p1Label + ", " + p2Label);
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
            if (element1 instanceof GPoint3d && element2 instanceof GPoint3d) {
                p1Label = ((GPoint3d)element1).getLabel();
                p2Label = ((GPoint3d)element2).getLabel();
            }
        }
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        // Validate end points
        if (p1Label.length() == 0 || p2Label.length() == 0) {
            logger.info("No end points: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        GPoint3d p1 = solid.getPoint(p1Label);
        if (p1 == null) {
            logger.info("No point: " + p1Label);
            throw new Exception(GDictionary.get(
                    "FigureContainsNoPoint", figureName, p1Label));
        }
        GPoint3d p2 = solid.getPoint(p2Label);
        if (p2 == null) {
            logger.info("No point: " + p2Label);
            throw new Exception(GDictionary.get(
                    "FigureContainsNoPoint", figureName, p2Label));
        }
        if (p1 == p2) {
            logger.info("Equal points: " + p1 + ", " + p2);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                p1Label, p2Label });
        if (faces.isEmpty()) {
            logger.info("No face: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("PointsDoNotBelongToSameFace",
                p1Label, p2Label, figureName));
        }
        Collection<GLine> lines = solid.linesThroughPoints(p1Label, p2Label);
        if (!lines.isEmpty()) {
            logger.info("Already drawn: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("LineAlreadyDrawn",
                    p1Label + p2Label, figureName));
        }
        // Apply
        face = faces.iterator().next();
        removedLines = new ArrayList<GLine>();
        addedLine = face.addLine(p1, p2, removedLines, solid);
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        face.undoAddLine(addedLine, removedLines);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + p1Label + ", " + p2Label);
    }

    public GLoggable clone() {
        GDrawLineAction action = new GDrawLineAction();
        action.figureName = figureName;
        action.p1Label = p1Label;
        action.p2Label = p2Label;
        return action;
    }

    public String toLogString() {
        return
            GDictionary.get("DrawLineInFigure", p1Label + p2Label, figureName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
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
        buf.append("\n<p1Label>");
        buf.append(p1Label);
        buf.append("</p1Label>");
        buf.append("\n<p2Label>");
        buf.append(p2Label);
        buf.append("</p2Label>");
        buf.append("\n</action>");
    }

    public void setInput(String p1String, String p2String) {
        logger.info(p1String + ", " + p2String);
        this.p1Label = p1String.toUpperCase();
        this.p2Label = p2String.toUpperCase();
    }

    public String getShortDescription() {
        return GDictionary.get("drawLine", p1Label + p2Label);
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
