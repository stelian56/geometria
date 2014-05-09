/**
 * Copyright 2000-2010 Geometria Contributors
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

import javax.swing.BoxLayout;
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
import net.geocentral.geometria.util.GApplicationManager;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GFrame extends JFrame implements GContainer {

    private GMainPanel mainPanel;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
    public GFrame() throws Exception {
        logger.info("");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        if (documentHandler == null)
            throw new Exception();
        documentHandler.setContainer(this);
        GMenuBar menuBar = new GMenuBar();
        menuBar.loadMenus();
        setJMenuBar(menuBar);
        mainPanel = new GMainPanel();
        documentHandler.setMainPanel(mainPanel);
        documentHandler.documentChanged();
        layoutComponents();
        pack();
    }

    private void layoutComponents() throws Exception {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        GGraphicsFactory.getInstance().layoutToolBars(getContentPane());
        getContentPane().add(mainPanel);
    }

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        return new GLocalFileReader(ownerFrame, filePath, fileFilters,
                acceptAllFilesFilter);
    }

    public GFileWriter getFileWriter(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter) {
        return new GLocalFileWriter(ownerFrame, filePath, fileFilters,
                acceptAllFilesFilter);
    }

    public Frame getOwnerFrame() {
        return this;
    }

    public GMainPanel getMainPanel() {
        return mainPanel;
    }

    public void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        logger.info("");
        final GFrame frame;
        try {
            GApplicationManager.getInstance().init();
            frame = new GFrame();
        }
        catch (Exception exception) {
            System.exit(0);
            return;
        }
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                GAction action = new GExitAction();
                try {
                    action.execute(GDocumentHandler.getInstance(), false);
                }
                catch (Exception exception) {
                    exception.printStackTrace(System.err);
                    System.exit(0);
                    return;
                }
            }
        });
        frame.setVisible(true);
    }

    private static final long serialVersionUID = 1L;
}
