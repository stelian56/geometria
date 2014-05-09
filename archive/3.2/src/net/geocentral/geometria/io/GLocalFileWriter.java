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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

public class GLocalFileWriter implements GFileWriter {

    private boolean approved;

    private String filePath;

    private JFileChooser fc;

    private Frame ownerFrame;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLocalFileWriter(Frame ownerFrame, String filePath,
            FileFilter[] filters, boolean acceptAllFileFilter) {
        logger.info(filePath);
        this.ownerFrame = ownerFrame;
        this.filePath = filePath;
        fc = new JFileChooser(filePath);
        for (FileFilter filter : filters)
            fc.addChoosableFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(acceptAllFileFilter);
    }

    public void selectFile() throws Exception {
        logger.info(filePath);
        int option = fc.showSaveDialog(ownerFrame);
        approved = option == JFileChooser.APPROVE_OPTION;
        if (!approved)
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

    public void write(String str) throws Exception {
        logger.info(str.length() + ", " + filePath);
        OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
        String encoding = writer.getEncoding();
        writer.close();
        logger.info("Encoding: " + encoding);
        str = new String(str.getBytes("UTF-8"), encoding);
        Writer out = new FileWriter(filePath);
        out.write(str);
        out.close();
    }

    public String getSelectedFilePath() {
        return filePath;
    }

    public boolean fileExists() {
        File file = new File(filePath);
        return file.exists();
    }

    public boolean approved() {
        return approved;
    }

    public FileFilter getFileFilter() {
        return fc.getFileFilter();
    }
}
