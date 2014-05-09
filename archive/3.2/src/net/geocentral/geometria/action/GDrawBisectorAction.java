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
import net.geocentral.geometria.view.GDivideAngleDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GDrawBisectorAction extends GDivideAngleAction {

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
                GDivideAngleDialog dialog = new GDivideAngleDialog(
                        documentHandler.getOwnerFrame(), this, false);
                dialog.prefill(pLabels[0], pLabels[1], pLabels[2],
                        numeratorString, denominatorString);
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
        return true;
    }

    protected void prefill(Set<GSelectable> selection) {
        logger.info(selection);
        super.prefill(selection);
        numeratorString = "1";
        denominatorString = "1";
    }

    public GLoggable clone() {
        GDrawBisectorAction action = new GDrawBisectorAction();
        action.figureName = figureName;
        action.numeratorString = numeratorString;
        action.denominatorString = denominatorString;
        action.pLabels = new String[3];
        for (int i = 0; i < 3; i++)
            action.pLabels[i] = pLabels[i];
        return action;
    }

    public String toLogString() {
        return GDictionary.get("DrawBisectorOfAngle", pLabels[1] + pLabels[0] +pLabels[2], figureName);
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

    public String getShortDescription() {
        return GDictionary.get("drawBisectorOfAngle",
                pLabels[1] + pLabels[0] + pLabels[2]);
    }
}
