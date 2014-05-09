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
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GOpenProblemAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        if (!documentHandler.onCloseDocument())
            return false;
        GProblem document;
        String filePath = documentHandler.getProblemPath();
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
            if (docElement.getNodeName().equals("solution"))
                throw new Exception(
                        GDictionary.get("FileContainsSolutionProblem"));
            if (docElement.getNodeName().equals("solid"))
                throw new Exception(
                        GDictionary.get("FileContainsFigureProblem"));
            document = new GProblem(docElement);
        }
        catch (Exception exception) {
            documentHandler.error(exception);
            return false;
        }
        try {
            document.validateWithSchema();
            document.validateVersion();
            document.make();
            document.getNotepad().validate();
        }
        catch (Exception exception) {
            documentHandler.error(GDictionary.get("FileCorruptedSeeLog"));
            return false;
        }
        documentHandler.clearUndoableActions();
        documentHandler.setActiveDocument(document);
        documentHandler.setMasterSolution(null);
        documentHandler.setProblemPath(filePath);
        String fileName = new File(filePath).getName();
        documentHandler.setTitle(fileName);
        documentHandler.documentChanged();
        documentHandler.setDocumentModified(false);
        logger.info(filePath);
        return true;
    }
}
