/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Component;
import java.awt.Frame;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.io.GFileWriter;
import net.geocentral.geometria.io.GRemoteFileReader;
import net.geocentral.geometria.io.GRemoteFileWriter;
import net.geocentral.geometria.model.GOptions;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GHelpManager;
import net.geocentral.geometria.util.GOptionsManager;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GVersionManager;

import org.apache.log4j.Logger;

public class GApplet extends JApplet implements GContainer {

    private GMainPanel mainPanel;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void init() {
        logger.info("");
        try {
            String language = this.getParameter("language");
            GOptions options = GOptionsManager.getInstance().getOptions();
            options.setLanguage(language);
            load();
            String documentLocation = this.getParameter("document");
            if (documentLocation != null) {
                GDocumentHandler.getInstance().setDocument(documentLocation, null);
            }
       }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
        }
    }

    public void load() throws Exception {
        GVersionManager versionManager = GVersionManager.getInstance();
        logger.info(String.format("Starting %s %s:%s", versionManager.getApplicationName(),
                versionManager.getApplicationVersion(), versionManager.getSvnRevision()));
        mainPanel = new GMainPanel();
        GDictionary.init();
        GGraphicsFactory.getInstance().setFont();
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        documentHandler.setContainer(this);
        documentHandler.setMainPanel(mainPanel);
        documentHandler.init();
        GHelpManager.getInstance().init();
        GMenuBar menuBar = new GMenuBar();
        menuBar.loadMenus();
        setJMenuBar(menuBar);
        mainPanel.layoutComponents();
        layoutComponents();
        documentHandler.documentChanged();
        repaint();
        validate();
    }
    
    private void layoutComponents() throws Exception {
        getContentPane().removeAll();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        GGraphicsFactory.getInstance().layoutToolBars(getContentPane());
        getContentPane().add(mainPanel);
    }

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        logger.info(filePath);
        URL baseUrl;
        try {
            baseUrl = new URL(String.format("%s%s/", getCodeBase(), GDocumentHandler.SAMPLES));
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return new GRemoteFileReader(this.getOwnerFrame(), baseUrl);
    }

    public GFileWriter getFileWriter(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        logger.info(filePath);
        URL baseUrl = getCodeBase();
        return new GRemoteFileWriter(ownerFrame, baseUrl, filePath);
    }

    public Frame getOwnerFrame() {
        Component c = getParent();
        while (true) {
            if (c instanceof Frame) {
                return (Frame)c;
            }
            c = c.getParent();
        }
    }

    public GMainPanel getMainPanel() {
        return mainPanel;
    }

    public void exit() {
        logger.info("");
        try {
            String path = GVersionManager.getInstance().getVendor();
            URL url = new URL(path);
            getAppletContext().showDocument(url);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
        }
        stop();
    }

    private static final long serialVersionUID = 1L;
}
