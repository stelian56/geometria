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

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileWriter;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GSaveFigureAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        String filePath = documentHandler.getFigurePath();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        GSolid solid = figure.getSolid();
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] fileFilters = {};
        GFileWriter writer = documentHandler.getFileWriter(ownerFrame, filePath, fileFilters, true);
        try {
            writer.selectFile();
            if (!writer.approved()) {
                return false;
            }
            if (writer.fileExists()) {
                int option = GGraphicsFactory.getInstance().showYesNoDialog(GDictionary.get("FileExistsOverwrite"));
                if (option != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            StringBuffer buf = new StringBuffer();
            solid.serialize(buf, true);
            writer.write(String.valueOf(buf));
        }
        catch (Exception exception) {
            documentHandler.error(exception);
            return false;
        }
        filePath = writer.getSelectedFilePath();
        logger.info(filePath + ", " + figure.getName());
        return true;
    }
}
