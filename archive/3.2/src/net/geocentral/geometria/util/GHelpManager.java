/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.MainWindow;
import javax.help.Map;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.model.GOptions;

import org.apache.log4j.Logger;

public class GHelpManager {

    private static GHelpManager instance;

    private HelpSet hs;

    private MainWindow mainWindow;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GHelpManager() {}

    public static GHelpManager getInstance() {
        if (instance != null)
            return instance;
        instance = new GHelpManager();
        return instance;
    }

    public void init() throws Exception {
        GOptions options = GOptionsManager.getInstance().getOptions();
        String language =  options.getLanguage();
        logger.info(String.format("Creating helpset for %s", language));
        if (mainWindow != null) {
            mainWindow.setDisplayed(false);
        }
        String file = String.format("/javahelp/%s/jhelpset.hs",language);
        URL url = GHelpManager.class.getResource(file);
        hs = new HelpSet(null, url);
        hs.createHelpBroker().initPresentation();
        mainWindow = (MainWindow)MainWindow.getPresentation(hs, "mainWindow");
        Frame owner = GDocumentHandler.getInstance().getOwnerFrame();
        mainWindow.setActivationWindow(owner);
        Font font = options.getFont();
        mainWindow.setFont(font);
    }

    public void fontChanged() {
        GOptions options = GOptionsManager.getInstance().getOptions();
        Font font = options.getFont();
        mainWindow.setFont(font);
    }
    
    public void displayContents() {
        logger.info("");
        mainWindow.setCurrentView("TOC");
        String title =
            String.format("%s - %s",GDictionary.get("Help"), GVersionManager.getInstance().getApplicationName());
        mainWindow.setTitle(title);
        displayPage("GeometriaUsersGuide", null);
    }

    public void displaySearch() {
        logger.info("");
        mainWindow.setCurrentView("Search");
        String title =
            String.format("%s - %s", GDictionary.get("Help"), GVersionManager.getInstance().getApplicationName());
        mainWindow.setTitle(title);
        displayPage("GeometriaUsersGuide", null);
    }

    public void displayPage(String pageId, Window owner) {
        logger.info(pageId);
        try {
            mainWindow.setCurrentID(Map.ID.create(pageId, hs));
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return;
        }
        mainWindow.setActivationWindow(owner);
        mainWindow.setDisplayed(true);
    }
}
