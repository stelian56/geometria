/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.Frame;
import java.awt.Window;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.MainWindow;
import javax.help.Map;

import net.geocentral.geometria.action.GDocumentHandler;

import org.apache.log4j.Logger;

public class GHelpManager {

    public static final String HELPSET_FILE = "/javahelp/jhelpset.hs";

    private static GHelpManager instance;

    private HelpSet hs;

    private MainWindow mainWindow;

    private Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GHelpManager() {}

    public static GHelpManager getInstance() {
        if (instance != null)
            return instance;
        instance = new GHelpManager();
        try {
            URL url = GHelpManager.class.getResource(HELPSET_FILE);
            instance.hs = new HelpSet(null, url);
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            return null;
        }
        return instance;
    }

    public void init() {
        logger.info("");
        hs.createHelpBroker().initPresentation();
        mainWindow = (MainWindow)MainWindow.getPresentation(hs, "mainWindow");
        Frame owner = GDocumentHandler.getInstance().getOwnerFrame();
        mainWindow.setActivationWindow(owner);
//        mainWindow.setFont(GGraphicsFactory.FONT);
    }

    public void displayContents() {
        logger.info("");
        mainWindow.setCurrentView("TOC");
        String title = GDictionary.get("Help") + " - "
            + GApplicationManager.getInstance().getApplicationName();
        mainWindow.setTitle(title);
        displayPage("top", null);
    }

    public void displaySearch() {
        logger.info("");
        mainWindow.setCurrentView("Search");
        String title = GDictionary.get("Help") + " - "
            + GApplicationManager.getInstance().getApplicationName();
        mainWindow.setTitle(title);
        displayPage("top", null);
    }

    public void displayPage(String pageId, Window owner) {
        logger.info(pageId);
        try {
            mainWindow.setCurrentID(Map.ID.create(pageId, hs));
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            return;
        }
        if (owner != null)
            mainWindow.setActivationWindow(owner);
        mainWindow.setDisplayed(true);
    }
}
