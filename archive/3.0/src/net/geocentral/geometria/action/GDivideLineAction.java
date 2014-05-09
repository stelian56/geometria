/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;

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
import net.geocentral.geometria.view.GDivideLineDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDivideLineAction implements GLoggable, GFigureAction, GActionWithHelp {

    protected String figureName;

    protected String numeratorString;

    protected String denominatorString;

    protected String p1Label;

    protected String p2Label;

    protected GDocument document;

    protected String addedPointLabel;

    private GFigure figure;

    private GSolid solid;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler, boolean quietMode) {
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
            GDivideLineDialog dialog = new GDivideLineDialog(documentHandler.getOwnerFrame(), this, true);
            dialog.prefill(p1Label, p2Label, null, null);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + p1Label + ", " + p2Label + ", "
                + numeratorString + ", " + denominatorString);
        return true;
    }

    protected void prefill(Set<GSelectable> selection) {
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
        if (selection.size() == 2) {
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
        // Validate numerator
        Double numerator;
        if (numeratorString.length() == 0) {
            logger.info("No numerator");
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForNumerator"));
        }
        GNotepadVariable variable = document.getVariable(numeratorString);
        if (variable != null)
            numerator = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            numerator = GMath.evaluate(numeratorString, variables);
            if (numerator == null) {
                logger.info("Bad numerator: " + numeratorString);
                throw new Exception(
                        GDictionary.get("EnterValidExpressionForNumerator"));
            }
        }
        if (numerator <= 0) {
            logger.info("Numerator must be positive: " + numerator);
            throw new Exception(GDictionary.get("NumeratorMustBePositive"));
        }

        // Validate denominator
        Double denominator;
        if (denominatorString.length() == 0) {
            logger.info("No denominator");
            throw new Exception(
                    GDictionary.get("EnterValidExpressionForDenominator"));
        }
        variable = document.getVariable(denominatorString);
        if (variable != null)
            denominator = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            denominator = GMath.evaluate(denominatorString, variables);
            if (denominator == null) {
                logger.info("Bad denominator: " + denominatorString);
                throw new Exception(
                        GDictionary.get("EnterValidExpressionForDenominator"));
            }
        }
        if (denominator <= 0) {
            logger.info("Denominator must be positive: " + denominator);
            throw new Exception(GDictionary.get("DenominatorMustBePositive"));
        }

        // Validate end points
        if (p1Label.length() == 0 || p2Label.length() == 0) {
            logger.info("No end points: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("EnterEndPoints"));
        }
        GPoint3d p1 = solid.getPoint(p1Label);
        if (p1 == null) {
            logger.info("No point: " + p1Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p1Label));
        }
        GPoint3d p2 = solid.getPoint(p2Label);
        if (p2 == null) {
            logger.info("No point: " + p2Label);
            throw new Exception(GDictionary.get("FigureContainsNoPoint",
                    figureName, p2Label));
        }
        if (p1 == p2) {
            logger.info("Equal end points: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("EndPointsCannotBeEqual"));
        }

        // Validate division point
        Point3d pCoords = new Point3d();
        Point3d scaledP1Coords = new Point3d();
        scaledP1Coords.scale(denominator, p1.coords);
        Point3d scaledP2Coords = new Point3d();
        scaledP2Coords.scale(numerator, p2.coords);
        pCoords.add(scaledP1Coords, scaledP2Coords);
        pCoords.scale(1 / (numerator + denominator));
        GPoint3d p = solid.getPoint(pCoords);
        if (p != null) {
            logger.info("Already divided");
            throw new Exception(GDictionary.get("LineAlreadyDividedInRatio",
                    p1Label + p2Label, numeratorString, denominatorString,
                    figureName));
        }

        // Validate lines
        Collection<GLine> lines = solid.linesThroughPoints(p1Label, p2Label);
        if (lines.isEmpty()) {
            logger.info("No line: " + p1Label + ", " + p2Label);
            throw new Exception(GDictionary.get("NoLinePassesThroughPoints",
                    p1Label, p2Label, figureName));
        }
        // Apply
        GPoint3d divPoint = solid.addPoint(pCoords);
        addedPointLabel = divPoint.getLabel();
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                p1Label, p2Label });
        for (GFace face : faces)
            face.addPoint(divPoint, solid);
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        GPoint3d p = solid.getPoint(addedPointLabel);
        Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                p1Label, p2Label });
        for (GFace face : faces)
            face.removePoint(p);
        solid.removePoint(addedPointLabel);
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info("Divide line " + p1Label + p2Label + " undone");
    }

    public GLoggable clone() {
        GDivideLineAction action = new GDivideLineAction();
        action.figureName = figureName;
        action.numeratorString = numeratorString;
        action.denominatorString = denominatorString;
        action.p1Label = p1Label;
        action.p2Label = p2Label;
        action.addedPointLabel = addedPointLabel;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("DivideLineInRatio",
                p1Label + p2Label, numeratorString, denominatorString,
               figureName);
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
        buf.append("\n<numerator>");
        buf.append(numeratorString);
        buf.append("</numerator>");
        buf.append("\n<denominator>");
        buf.append(denominatorString);
        buf.append("</denominator>");
        buf.append("\n<p1Label>");
        buf.append(p1Label);
        buf.append("</p1Label>");
        buf.append("\n<p2Label>");
        buf.append(p2Label);
        buf.append("</p2Label>");
        buf.append("\n</action>");
    }

    public void setInput(String p1String, String p2String,
            String numeratorString, String denominatorString) {
        logger.info(p1String + ", " + p2String + ", " + numeratorString
                + ", " + denominatorString);
        this.p1Label = p1String.toUpperCase();
        this.p2Label = p2String.toUpperCase();
        this.numeratorString = numeratorString;
        this.denominatorString = denominatorString;
    }

    public String getShortDescription() {
        return GDictionary.get("divideLineInRatio", p1Label + p2Label,
                numeratorString, denominatorString);
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
