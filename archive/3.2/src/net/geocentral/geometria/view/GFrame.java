/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;


import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.action.GAction;
import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.action.GExitAction;
import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.io.GFileWriter;
import net.geocentral.geometria.io.GLocalFileReader;
import net.geocentral.geometria.io.GLocalFileWriter;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GDragAndDropHandler;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GHelpManager;
import net.geocentral.geometria.util.GOptionsManager;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GVersionManager;

import org.apache.log4j.Logger;

public class GFrame extends JFrame implements GContainer {

    private GMainPanel mainPanel;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
    public GFrame() {
        GVersionManager versionManager = GVersionManager.getInstance();
        logger.info(String.format("Starting %s %s:%s", versionManager.getApplicationName(),
                versionManager.getApplicationVersion(), versionManager.getSvnRevision()));
        mainPanel = new GMainPanel();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void load() throws Exception {
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
        new GDragAndDropHandler().init((JComponent)getContentPane());
        documentHandler.documentChanged();
        repaint();
        validate();
        pack();
    }

    private void layoutComponents() throws Exception {
        logger.info("");
        getContentPane().removeAll();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        GGraphicsFactory.getInstance().layoutToolBars(getContentPane());
        getContentPane().add(mainPanel);
    }

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        return new GLocalFileReader(ownerFrame, filePath, fileFilters, acceptAllFilesFilter);
    }

    public GFileWriter getFileWriter(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        return new GLocalFileWriter(ownerFrame, filePath, fileFilters, acceptAllFilesFilter);
    }

    public Frame getOwnerFrame() {
        return this;
    }

    public GMainPanel getMainPanel() {
        return mainPanel;
    }

    public void exit() {
        logger.info(String.format("Exiting %s", GVersionManager.getInstance().getApplicationName()));
        GOptionsManager.getInstance().saveOptions();
        System.exit(0);
    }

    public static void main(String[] args) {
        logger.info("");
        final GFrame frame = new GFrame();
        try {
            frame.load();
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            System.exit(0);
            return;
        }
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                GAction action = new GExitAction();
                try {
                    action.execute();
                }
                catch (Exception exception) {
                    logger.error(GStringUtils.stackTraceToString(exception));
                    System.exit(1);
                    return;
                }
            }
        });
        if (args.length > 0) {
            String documentLocation = args[0]; 
            try {
                String filePath = null;
                try {
                    filePath = new File(new URI(documentLocation)).getPath();
                }
                catch (Exception exception) {}
                GDocumentHandler.getInstance().setDocument(documentLocation, filePath);
            }
            catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        }
        frame.setVisible(true);
    }

    private static final long serialVersionUID = 1L;
}
