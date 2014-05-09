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
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;

import org.apache.log4j.Logger;

public class GUndoAction implements GNavigationAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        GUndoable action = documentHandler.getCurrentAction();
        action.undo(documentHandler);
        if (document instanceof GSolution && action instanceof GLoggable) {
            GLog log = ((GSolution)document).getLog();
            log.removeLast();
        }
        documentHandler.toPreviousAction();
        documentHandler.setDocumentModified(true);
        documentHandler.updateActionHandlerStates();
        logger.info(action);
        return true;
    }
}
