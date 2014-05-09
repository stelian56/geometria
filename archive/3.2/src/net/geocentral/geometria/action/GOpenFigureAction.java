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
import java.util.List;

import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class GOpenFigureAction implements GUndoable {

    private GDocument document;

    private String figureName;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        String filePath = documentHandler.getFigurePath();
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] filters = {};
        GFileReader reader = documentHandler.getFileReader(ownerFrame, filePath, filters, true);
        GSolid solid;
        try {
            reader.init();
            reader.selectFile(); 
            filePath = reader.getSelectedFilePath();
            if (filePath == null) {
                return false;
            }
            InputSource source = new InputSource(reader.getInputStream());
            solid = GXmlUtils.readSolid(source);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            documentHandler.error(exception);
            return false;
        }
        if (execute(solid)) {
            setFilePath(filePath);
            if (!silent) {
                documentHandler.setDocumentModified(true);
            }
            return true;
        }
        return false;
    }

    public boolean execute(GSolid solid) {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        document = documentHandler.getActiveDocument();
        List<String> figureNames = document.getFigureNames();
        figureName = GLabelFactory.getInstance().newFigureName(figureNames);
        GFigure figure = new GFigure(solid);
        figure.setName(figureName);
        document.addFigure(figure);
        document.setSelectedFigure(figureName);
        documentHandler.addFigure(figure);
        documentHandler.notepadChanged();
        documentHandler.clearUndoableActions();
        documentHandler.setActiveDocument(document);
        documentHandler.setMasterSolution(null);
        documentHandler.documentChanged();
        documentHandler.setDocumentModified(false);
        return true;
    }

    public void setFilePath(String filePath) {
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        documentHandler.setFigurePath(filePath);
        logger.info(filePath);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        document.removeFigure(figureName);
        documentHandler.removeFigure(figureName);
        documentHandler.notepadChanged();
        logger.info(figureName);
    }

    public String getShortDescription() {
        return GDictionary.get("openFigure", figureName);
    }
}
