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

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;

import org.apache.log4j.Logger;

public class GSelectAllAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        GSolid solid = figure.getSolid();
        solid.selectAll();
        figure.repaint();
        Set<GSelectable> selection = solid.getSelection();
        logger.info(selection);
        return true;
    }
}
