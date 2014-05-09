/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;

import org.apache.log4j.Logger;

public class GLogPlayAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GSolution masterSolution = documentHandler.getMasterSolution();
        GLog log = masterSolution.getLog();
        log.startPlaying();
        GSolution document = new GSolution(masterSolution);
        documentHandler.setActiveDocument(document);
        documentHandler.documentChanged();
        logger.info("");
        return true;
    }
}
