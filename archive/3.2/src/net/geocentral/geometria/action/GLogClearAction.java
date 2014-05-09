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

import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GLogClearAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        if (GGraphicsFactory.getInstance().showYesNoDialog(GDictionary.get(
                "CannotUndoProceed")) != JOptionPane.YES_OPTION) {
            return false;
        }
        documentHandler.clearUndoableActions();
        GSolution document = documentHandler.getMasterSolution();
        GProblem problem = document.getProblem();
        // Make a new master document
        GSolution doc = new GSolution(problem);
        doc.importFigures(problem);
        documentHandler.setActiveDocument(doc);
        documentHandler.setMasterSolution(doc);
        documentHandler.documentChanged();
        // Master document modified since it's no longer 'document', but 'doc'
        documentHandler.setDocumentModified(true);
        logger.info("");
        return true;
    }
}
