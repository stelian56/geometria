/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GSolidFactory;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GPrismNAction implements GLoggable, GFigureAction {

    private String figureName;

    private int sideCount;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        if (!quietMode) {
            String input = GGraphicsFactory.getInstance().showInputDialog( 
                GDictionary.get("EnterNumberOfSides"));
            if (input == null)
                return false;
            try {
                sideCount = Integer.parseInt(input);
            }
            catch (Exception exception) {
                documentHandler.error(GDictionary.get("BadSideCount", input));
                return false;
            }
            if (sideCount < 3) {
                documentHandler.error(GDictionary.get("BadSideCount", input));
                return false;
            }
        }
        GSolid solid = GSolidFactory.getInstance().newPrism(sideCount);
        GFigure figure = documentHandler.newFigure(solid);
        figureName = figure.getName();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        documentHandler.notepadChanged();
        logger.info(figureName + ", " + sideCount);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.removeFigure(figureName);
        documentHandler.removeFigure(figureName);
        documentHandler.notepadChanged();
        logger.info(figureName + ", " + sideCount);
    }

    public GLoggable clone() {
        GPrismNAction action = new GPrismNAction();
        action.figureName = figureName;
        action.sideCount = sideCount;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("CreatePrism", String.valueOf(sideCount),
                figureName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        sideCount = Integer.valueOf(
            node.getElementsByTagName("sideCount").item(0).getTextContent());
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<sideCount>");
        buf.append(String.valueOf(sideCount));
        buf.append("</sideCount>");
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("createPrism", String.valueOf(sideCount));
    }

    public String getFigureName() {
        return figureName;
    }
}
