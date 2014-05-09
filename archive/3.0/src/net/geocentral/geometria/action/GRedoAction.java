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
import net.geocentral.geometria.model.GSolution;

import org.apache.log4j.Logger;

public class GRedoAction implements GNavigationAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler, boolean quietMode) {
        logger.info(quietMode);
        GUndoable action = documentHandler.toNextAction();
        action.execute(documentHandler, quietMode);
        GDocument document = documentHandler.getActiveDocument();
        if (document instanceof GSolution && action instanceof GLoggable)
            ((GSolution)document).getLog().add((GLoggable)action);
        documentHandler.setDocumentModified(true);
        logger.info(action);
        return true;
    }
}