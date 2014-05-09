/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import javax.swing.JOptionPane;

import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GLogCutAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        if (GGraphicsFactory.getInstance().showYesNoDialog(
                GDictionary.get("CannotUndoProceed")) != JOptionPane.YES_OPTION) {
            return false;
        }
        GSolution document = documentHandler.getMasterSolution();
        documentHandler.setActiveDocument(document);
        GLog log = document.getLog();
        log.stopPlaying();
        GLoggable action = log.actionAtCurrentPos();
        int index = log.indexOf(action);
        for (int i = log.size() - 1; i > index; i--) {
            GLoggable a = log.actionAt(i);
            a.undo(documentHandler);
            log.removeLast();
        }
        documentHandler.removeLoggableFollowing(action);
        documentHandler.documentChanged();
        documentHandler.setDocumentModified(true);
        logger.info("");
        return true;
    }
}
