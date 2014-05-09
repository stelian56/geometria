/**
 * Copyright 2000-2010 Geometria Contributors
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
import net.geocentral.geometria.view.GDivideLineDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDrawMidpointAction extends GDivideLineAction {

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
                exception.printStackTrace(System.err);
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
        if (!quietMode)
            documentHandler.setDocumentModified(true);
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

    public String getShortDescription() {
        return GDictionary.get("drawMidpointOfLine", p1Label + p2Label);
    }
}
