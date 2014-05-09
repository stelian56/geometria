/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GToggleTransparencyAction implements GUndoable {

    private String figureName;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        figureName = figure.getName();
        figure.toggleTransparency();
        documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + figure.isTransparent());
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getFigure(figureName);
        figure.toggleTransparency();
        logger.info(figureName + ", " + figure.isTransparent());
    }

    public String getShortDescription() {
        return GDictionary.get("toggleTransparency");
    }
}
