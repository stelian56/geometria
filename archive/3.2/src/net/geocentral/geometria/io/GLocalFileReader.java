/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GLocalFileReader implements GFileReader {

    protected String filePath;

    private JFileChooser fc;

    private Frame ownerFrame;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLocalFileReader(Frame ownerFrame, String filePath,
            FileFilter[] filters, boolean acceptAllFileFilter) {
        logger.info(filePath);
        this.ownerFrame = ownerFrame;
        this.filePath = filePath;
        fc = new JFileChooser(filePath);
        for (FileFilter filter : filters)
            fc.addChoosableFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(acceptAllFileFilter);
    }
    
    public void init() throws Exception {
        logger.info("");
    }
    
    public void selectFile() throws Exception {
        logger.info("");
        int option = fc.showOpenDialog(ownerFrame);
        if (option != JFileChooser.APPROVE_OPTION)
            filePath = null;
        else {
            File file = fc.getSelectedFile();
            if (file != null) {
                filePath = file.getPath();
                FileFilter filter = fc.getFileFilter();
                if (filter instanceof GExtensionFileFilter) {
                    String extension =
                        ((GExtensionFileFilter)filter).getExtension(); 
                    if (!filePath.endsWith("." + extension))
                        filePath = filePath + "." + extension;
                }
            }
            else
                filePath = null;
        }
        logger.info(filePath);
    }

    public InputStream getInputStream() throws Exception {
        logger.info(filePath);
        try {
            return new FileInputStream(filePath);
        }
        catch (Exception exception) {
            logger.info("Cannot read file:" + filePath);
            throw new Exception(GDictionary.get("CannotReadFile", filePath));
        }
    }
    
    public String getSelectedFilePath() {
        return filePath;
    }

    public String getSelectedFileName() {
        File file = new File(filePath);
        return file.getName();
    }

    public FileFilter getFileFilter() {
        return fc.getFileFilter();
    }
}
