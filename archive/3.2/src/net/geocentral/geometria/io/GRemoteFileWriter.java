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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFileChooser;

import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GRemoteFileWriter extends GRemoteFileHandler
    implements GFileWriter {

    private static final String uploaddocumentHandlerPath = "uploadDoc.php";

    private String filePath;

    private Frame ownerFrame;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRemoteFileWriter(Frame ownerFrame, URL baseUrl, String filePath) {
        super(baseUrl);
        logger.info(baseUrl + ", " + filePath);
        this.ownerFrame = ownerFrame;
    }

    public void selectFile() {
        logger.info(filePath);
        GFileChooser treeNodeChooser =
            new GFileChooser(docTreeElement, ownerFrame, filePath);
        int option = treeNodeChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION)
            filePath = treeNodeChooser.getSelectionPath();
    }

    public void write(String str) throws Exception {
        logger.info(str.length());
        URL url = new URL(baseUrl.getProtocol(), baseUrl.getHost(),
                baseUrl.getPort(), baseUrl.getPath()
                + uploaddocumentHandlerPath + "?filePath=" + filePath);
        logger.info(url);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoOutput(true);
        Writer out = new PrintWriter(connection.getOutputStream());
        out.write(str);
        out.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String response = in.readLine();
        logger.info(response);
        if (response.indexOf("OK") < 0) {
            logger.error(response);
            throw new Exception(GDictionary.get("CannotSave", response));
        }
        connection.disconnect();
    }

    public String getSelectedFilePath() {
        return filePath;
    }

    public boolean fileExists() {
        return false;
    }

    public boolean approved() {
        return true;
    }

    public GExtensionFileFilter getFileFilter() {
        logger.error("");
        throw new RuntimeException();
    }
}
