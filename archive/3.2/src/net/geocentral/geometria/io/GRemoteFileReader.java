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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFileChooser;

import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GRemoteFileReader extends GRemoteFileHandler implements GFileReader {

    private String filePath;
    
    private Frame ownerFrame;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRemoteFileReader(Frame ownerFrame, URL baseUrl) {
        super(baseUrl);
        logger.info(baseUrl);
        this.ownerFrame = ownerFrame;
    }

    public void selectFile() throws Exception {
        logger.info("");
        GFileChooser treeNodeChooser = new GFileChooser(docTreeElement, ownerFrame, filePath);
        int option = treeNodeChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            filePath = treeNodeChooser.getSelectionPath();
        }
        else {
            filePath = null;
        }
        logger.info(filePath);
    }

    public void selectFile(Frame ownerFrame) {
    }

    public InputStream getInputStream() throws Exception {
        logger.info(filePath);
        try {
            URL url = new URL(baseUrl.getProtocol(), baseUrl.getHost(), 
                    baseUrl.getPort(), baseUrl.getPath() + filePath);
            logger.info(url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        }
        catch (Exception exception) {
            logger.error(filePath);
            throw new Exception(GDictionary.get("CannotReadRemoteFile", baseUrl.getPath() + filePath));
        }
    }

    public String getSelectedFilePath() {
        return filePath;
    }

    public String getSelectedFileName() {
        String[] tokens = filePath.split("/");
        return tokens[tokens.length - 1];
    }
}
