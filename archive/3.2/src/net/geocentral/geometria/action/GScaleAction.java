/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GScaleDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GScaleAction implements GLoggable, GFigureAction, GActionWithHelp {

    private String figureName;

    private String factorString;

    private String p1Label;

    private String p2Label;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private double factor;

    private String helpId;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info("");
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
            GScaleDialog dialog = new GScaleDialog(documentHandler
                    .getOwnerFrame(), this);
            dialog.prefill(p1Label, p2Label);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
            solid.clearSelection();
        }
        document.getNotepad().update();
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + factorString + ", " + p1Label + ", "
                + p2Label);
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
        else if (selection.size() == 2) {
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
        figure = document.getFigure(figureName);
        solid = figure.getSolid();

        // Validate factor
        if (factorString.length() == 0) {
            logger.info("No factor");
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForScalingFactor"));
        }
        GNotepadVariable variable = document.getVariable(factorString);
        if (variable != null)
            factor = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            Double f = GMath.evaluate(factorString, variables);
            if (f == null) {
                logger.info("Bad factor: " + factorString);
                throw new Exception(
                    GDictionary.get("EnterValidExpressionForScalingFactor"));
            }
            factor = f;
        }
        if (factor <= 0) {
            logger.info("Factor must be positive: " + factor);
            throw new Exception(GDictionary.get("ScalingFactorMustBePositive"));
        }

        // Validate end points
        if (p1Label.length() == 0 || p2Label.length() == 0) {
            logger.info("No end points: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        GPoint3d p1 = solid.getPoint(p1Label);
        GPoint3d p2 = solid.getPoint(p2Label);
        if (p1 == null) {
            logger.info("No point: " + p1Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p1Label));
        }
        if (p2 == null) {
            logger.info("No point: " + p2Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p2Label));
        }
        if (p1 == p2) {
            logger.info("Equal points: " + p1 + ", " + p2);
            throw new Exception(GDictionary.get("Ref2DistinctPoints"));
        }

        // Apply
        Vector3d v = new Vector3d(p2.coords);
        v.sub(p1.coords);
        solid.scale(new Point3d(0, 0, 0), v, factor);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GPoint3d p1 = solid.getPoint(p1Label);
        GPoint3d p2 = solid.getPoint(p2Label);
        Vector3d v = new Vector3d(p2.coords);
        v.sub(p1.coords);
        solid.scale(new Point3d(0, 0, 0), v, 1 / factor);
        document.getNotepad().update();
        document.setSelectedFigure(figureName);
        figure.repaint();
        logger.info(figureName + ", " + factorString + ", " + p1Label + ", "
                + p2Label);
    }

    public GLoggable clone() {
        GScaleAction action = new GScaleAction();
        action.figureName = figureName;
        action.factorString = factorString;
        action.p1Label = p1Label;
        action.p2Label = p2Label;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("ScaleFigureByFactor", figureName,
                factorString, p1Label + p2Label);
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
        ns = node.getElementsByTagName("factor");
        if (ns.getLength() == 0) {
            logger.error("No factor");
            throw new Exception();
        }
        factorString = ns.item(0).getTextContent();
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
            .append("\n<factor>")
            .append(factorString)
            .append("</factor>")
            .append("\n<p1Label>")
            .append(p1Label)
            .append("</p1Label>")
            .append("\n<p2Label>")
            .append(p2Label)
            .append("</p2Label>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String p1String, String p2String,
            String factorString) {
        logger.info(p1String + ", " + p2String + ", " + factorString);
        p1Label = p1String.toUpperCase();
        p2Label = p2String.toUpperCase();
        this.factorString = factorString;
    }

    public String getShortDescription() {
        return GDictionary.get("scaleFigureByFactor", figureName, factorString);
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
