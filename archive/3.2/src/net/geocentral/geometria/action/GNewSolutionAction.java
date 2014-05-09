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
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GNewSolutionAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GDocument document = documentHandler.getActiveDocument();
        GProblem problem;
        if (document instanceof GProblem) {
            problem = (GProblem)document;
        }
        else {
            problem = ((GSolution)document).getProblem();
        }
        if (!documentHandler.onCloseDocument()) {
            return false;
        }
        documentHandler.clearUndoableActions();
        GSolution doc = new GSolution(problem);
        doc.setPrime(true);
        doc.importFigures(problem);
        documentHandler.setActiveDocument(doc);
        documentHandler.setMasterSolution(doc);
        documentHandler.documentChanged();
        documentHandler.setDocumentModified(false);
        String filePath = documentHandler.getProblemPath();
        documentHandler.setTitle(GDictionary.get("UntitledSolution"));
        logger.info(filePath);
        return true;
    }
}
