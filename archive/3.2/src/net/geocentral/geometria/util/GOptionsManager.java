/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.model.GOptions;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class GOptionsManager {

    private static final String FILENAME = "options.xml";

    private GOptions options;
    
    private static GOptionsManager instance;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GOptionsManager() {
        loadOptions();
    }

    public static GOptionsManager getInstance() {
        if (instance == null) {
            instance = new GOptionsManager();
        }
        return instance;
    }
    
    private void loadOptions() {
        logger.info("");
        String optionsFilePath;
        try {
            optionsFilePath = getOptionsFilePath();
        }
        catch (Exception exception) {
            logger.warn(String.format("Cannot find options path. Using defaults"));
            options = new GOptions();
            options.setDefaults();
            return;
        }
        File optionsFile = new File(optionsFilePath);
        if (!optionsFile.exists()) {
            logger.warn(String.format("Cannot find options path. Using defaults"));
            options = new GOptions();
            options.setDefaults();
            return;
        }
        InputSource in;
        try {
            in = new InputSource(new FileInputStream(optionsFile));
            options = GXmlUtils.readOptions(in);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            options = new GOptions();
            options.setDefaults();
            return;
        }
        logger.info(String.format("Options loaded from %s", optionsFilePath));
    }

    private String getOptionsFilePath() throws Exception {
        logger.info("");
        String userHome = System.getProperty("user.home");
        if(userHome == null) {
            logger.error("No user.home property");
            throw new Exception();
        }
        File homeDir = new File(userHome);
        File optionsDir = new File(homeDir, ".geometria");
        if(!optionsDir.exists()) {
            if(!optionsDir.mkdir()) {
                logger.error("Cannot create the .geometria directory");
                throw new Exception();
            }
        }
        String optionsFilePath = new File(optionsDir, FILENAME).getPath();
        return optionsFilePath;
    }
    
    public boolean saveOptions() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        String optionsFilePath;
        try {
            optionsFilePath = getOptionsFilePath();
        }
        catch (Exception exception) {
            documentHandler.error(GDictionary.get("CannotSaveOptions"));
            return false;
        }
        try {
            Writer writer = new FileWriter(optionsFilePath);
            StringBuffer buf = new StringBuffer();
            options.serialize(buf, true);
            writer.write(String.valueOf(buf));
            writer.close();
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            documentHandler.error(GDictionary.get("CannotSaveOptions"));
            return false;
        }
        logger.info(String.format("Options saved to %s", optionsFilePath));
        return true;
    }
    
    public GOptions getOptions() {
        return options;
    }
}
