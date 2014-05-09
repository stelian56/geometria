/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Frame;
import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GOpenSolutionAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        if (!documentHandler.onCloseDocument())
            return false;
        GSolution document;
        String filePath = documentHandler.getSolutionPath();
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] filters = {};
        GFileReader reader = documentHandler.getFileReader(ownerFrame, filePath,
                filters, true);
        try {
            reader.init();
            reader.selectFile();
            filePath = reader.getSelectedFilePath();
            if (filePath == null)
                return false;
            InputStream in = reader.getInputStream();
            Element docElement = GXmlUtils.read(in);
            in.close();
            if (docElement.getNodeName().equals("problem"))
                throw new Exception(
                        GDictionary.get("FileContainsProblemSolution"));
            if (docElement.getNodeName().equals("solid"))
                throw new Exception(
                        GDictionary.get("FileContainsFigureSolution"));
            document = new GSolution(docElement);
        }
        catch (Exception exception) {
            documentHandler.error(exception);
            return false;
        }
        try {
            document.validateWithSchema();
            document.validateVersion();
            document.make();
        }
        catch (Exception exception) {
            documentHandler.error(GDictionary.get("FileCorruptedSeeLog"));
            return false;
        }
        documentHandler.setActiveDocument(document);
        documentHandler.setMasterSolution(document);
        document.importFigures(document.getProblem());
        documentHandler.clearUndoableActions();
        GLog log = document.getLog();
        for (int i = 0; i < log.size(); i++) {
            GLoggable action = log.actionAt(i);
            if (!action.execute(documentHandler, true)) {
                logger.error("Bad log entry " + action);
                documentHandler.setActiveDocument(null);
                documentHandler.setMasterSolution(null);
                documentHandler.documentChanged();
                documentHandler.updateActionHandlerStates();
                documentHandler.error(GDictionary.get("FileCorruptedSeeLog"));
                return false;
            }
        }
        documentHandler.setSolutionPath(filePath);
        String fileName = new File(filePath).getName();
        documentHandler.setTitle(fileName);
        documentHandler.setDocumentModified(false);
        documentHandler.documentChanged();
        logger.info(filePath);
        return true;
    }
}
