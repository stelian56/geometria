/**
 * Copyright 2000-2010 Geometria Contributors
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
import net.geocentral.geometria.util.GApplicationManager;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GApplet extends JApplet implements GContainer {

    private GMainPanel mainPanel;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void init() {
        logger.info("");
        try {
            GApplicationManager.getInstance().init();
            GDocumentHandler documentHandler = GDocumentHandler.getInstance();
            if (documentHandler == null)
                throw new Exception();
            documentHandler.setContainer(this);
            GMenuBar menuBar = new GMenuBar();
            menuBar.loadMenus();
            setJMenuBar(menuBar);
            mainPanel = new GMainPanel();
            documentHandler.setMainPanel(mainPanel);
            layoutComponents();
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

    private void layoutComponents() {
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        GGraphicsFactory.getInstance().layoutToolBars(getContentPane());
        getContentPane().add(mainPanel);
    }

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        logger.info(filePath);
        URL baseUrl = getCodeBase();
        return new GRemoteFileReader(ownerFrame, baseUrl, filePath);
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
            if (c instanceof Frame)
                return (Frame)c;
            c = c.getParent();
        }
    }

    public GMainPanel getMainPanel() {
        return mainPanel;
    }

    public void exit() {
        logger.info("");
        try {
            String path = GApplicationManager.getInstance().getHomeUrl();
            URL url = new URL(path);
            getAppletContext().showDocument(url);
        }
        catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
        stop();
    }

    private static final long serialVersionUID = 1L;
}
