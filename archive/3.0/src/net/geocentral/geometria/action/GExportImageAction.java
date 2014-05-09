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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GExtensionFileFilter;
import net.geocentral.geometria.io.GFileWriter;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GExportImageAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
    		boolean quietMode) {
        logger.info("");
        String filePath = documentHandler.getFigurePath();
        GDocument document = documentHandler.getActiveDocument();
        GFigure figure = document.getSelectedFigure();
        String figureName = figure.getName();
        BufferedImage image = figure.exportImage();
        Set<String> formats = new TreeSet<String>();
        for (String format : ImageIO.getWriterFormatNames())
            formats.add(format.toLowerCase());
        if (formats.contains("jpeg") && formats.contains("jpg"))
            formats.remove("jpeg");
        Frame ownerFrame = documentHandler.getOwnerFrame();
        FileFilter[] filters = new FileFilter[formats.size()];
        int index = 0;
        for (String format : formats)
            filters[index++] = new GExtensionFileFilter(format);
        GFileWriter writer =
            documentHandler.getFileWriter(ownerFrame, filePath, filters, false);
        try {
            writer.selectFile();
            if (!writer.approved())
                return false;
            if (writer.fileExists()) {
                int option = GGraphicsFactory.getInstance().showYesNoDialog(
                        GDictionary.get("FileExistsOverwrite"));
                if (option != JOptionPane.YES_OPTION)
                    return false;
            }
            String fp = writer.getSelectedFilePath();
            String extension =
                ((GExtensionFileFilter)writer.getFileFilter()).getExtension();
            ImageIO.write(image, extension, new File(fp));
            filePath = fp;
        }
        catch (Exception exception) {
            documentHandler.error(
                    GDictionary.get("CannotExport", figureName));
        }
        logger.info(figureName + ": " + filePath);
        return true;
    }
}
