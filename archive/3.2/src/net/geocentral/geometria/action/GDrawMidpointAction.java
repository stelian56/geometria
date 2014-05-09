/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Set;

import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GDivideLineDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDrawMidpointAction extends GDivideLineAction {

    private String comments;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        document = documentHandler.getActiveDocument();
        if (silent) {
            try {
                validateApply();
            }
            catch (Exception exception) {
                logger.error(GStringUtils.stackTraceToString(exception));
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
                GDivideLineDialog dialog = new GDivideLineDialog(
                        documentHandler.getOwnerFrame(), this, false);
                dialog.prefill(p1Label, p2Label, numeratorString,
                        denominatorString);
                dialog.setVisible(true);
                if (!dialog.getResult())
                    return false;
            }
            solid.clearSelection();
        }
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        return true;
    }

    protected void prefill(Set<GSelectable> selection) {
        super.prefill(selection);
        numeratorString = "1";
        denominatorString = "1";
    }

    public GLoggable clone() {
        GDrawMidpointAction action = new GDrawMidpointAction();
        action.figureName = figureName;
        action.numeratorString = numeratorString;
        action.denominatorString = denominatorString;
        action.p1Label = p1Label;
        action.p2Label = p2Label;
        action.addedPointLabel = addedPointLabel;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("DrawMidpointOfLine", p1Label + p2Label,
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
        numeratorString = "1";
        denominatorString = "1";
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

    public String getShortDescription() {
        return GDictionary.get("drawMidpointOfLine", p1Label + p2Label);
    }
}
