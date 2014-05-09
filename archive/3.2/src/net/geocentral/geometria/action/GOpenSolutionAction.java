/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Frame;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class GOpenSolutionAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        if (!documentHandler.onCloseDocument()) {
            return false;
        }
        String filePath = documentHandler.getSolutionPath();
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] filters = {};
        GFileReader reader = documentHandler.getFileReader(ownerFrame, filePath, filters, true);
        GSolution document;
        try {
            reader.init();
            reader.selectFile(); 
            filePath = reader.getSelectedFilePath();
            if (filePath == null) {
                return false;
            }
            InputSource source = new InputSource(reader.getInputStream());
            document = GXmlUtils.readSolution(source);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            documentHandler.error(exception);
            return false;
        }
        if (execute(document)) {
            setFilePath(filePath);
            return true;
        }
        return false;
    }

    public boolean execute(GSolution document) {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        try {
            document.getNotepad().validate();
        }
        catch (Exception exception) {
            documentHandler.error(exception.getMessage());
            return false;
        }
        documentHandler.setActiveDocument(document);
        documentHandler.setMasterSolution(document);
        document.importFigures(document.getProblem());
        documentHandler.clearUndoableActions();
        GLog log = document.getLog();
        for (int i = 0; i < log.size(); i++) {
            GLoggable action = log.actionAt(i);
            if (!action.execute(true)) {
                logger.error("Bad log entry " + action);
                documentHandler.setActiveDocument(null);
                documentHandler.setMasterSolution(null);
                documentHandler.documentChanged();
                documentHandler.updateActionHandlerStates();
                documentHandler.error(GDictionary.get("FileCorruptedSeeLog"));
                return false;
            }
        }
        documentHandler.documentChanged();
        documentHandler.setDocumentModified(false);
        return true;
    }

    public void setFilePath(String filePath) {
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        documentHandler.setSolutionPath(filePath);
        String fileName = new File(filePath).getName();
        documentHandler.setTitle(fileName);
        logger.info(filePath);
    }

}
