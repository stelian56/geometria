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
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GOpenFigureAction implements GUndoable {

    private GDocument document;

    private String figureName;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        GSolid solid = new GSolid();
        String filePath = documentHandler.getFigurePath();
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] filters = {};
        GFileReader reader = documentHandler.getFileReader(ownerFrame,
                filePath, filters, true);
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
                        GDictionary.get("FileContainsProblemFigure"));
            if (docElement.getNodeName().equals("solution"))
                throw new Exception(
                        GDictionary.get("FileContainsSolutionFigure"));
            solid = new GSolid(docElement);
        }
        catch (Exception exception) {
            documentHandler.error(exception);
            return false;
        }
        try {
            solid.validateWithSchema();
            solid.validateVersion();
            solid.make();
        }
        catch (Exception exception) {
            documentHandler.error(GDictionary.get("FileCorruptedSeeLog"));
            return false;
        }
        documentHandler.setFigurePath(filePath);
        figureName = document.newFigureName();
        GFigure figure = new GFigure(solid);
        figure.setName(figureName);
        document.addFigure(figure);
        document.setSelectedFigure(figureName);
        if (!quietMode)
        	documentHandler.setDocumentModified(true);
        documentHandler.addFigure(figure);
        documentHandler.notepadChanged();
        logger.info(filePath + ", " + figureName);
        return true;
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
