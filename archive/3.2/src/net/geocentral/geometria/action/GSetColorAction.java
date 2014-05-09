/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Color;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GSetColorAction implements GUndoable {

    private String figureName;

    private Color wasBaseColor;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        wasBaseColor = figure.getBaseColor();
        Color baseColor = GGraphicsFactory.getInstance().showColorChooser(
                figure.getBaseColor());
        if (baseColor != null) {
            figure.setBaseColor(baseColor);
            figureName = figure.getName();
            documentHandler.setDocumentModified(true);
            return true;
        }
        logger.info(figureName + ", " + wasBaseColor + ", " + baseColor);
        return false;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GFigure figure =
            documentHandler.getActiveDocument().getFigure(figureName);
        figure.setBaseColor(wasBaseColor);
        logger.info(figureName + ", " + wasBaseColor + ", "
                + figure.getBaseColor());
    }

    public String getShortDescription() {
        return GDictionary.get("setColor");
    }
}
