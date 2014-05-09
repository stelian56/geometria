/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GVersionManager;

import org.apache.log4j.Logger;

public class GAboutAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GVersionManager versionManager = GVersionManager.getInstance();
        ImageIcon icon = new ImageIcon(GAboutAction.class.getResource("/images/Logo.png"));
        String appName = versionManager.getApplicationName();
        String version = versionManager.getApplicationVersion();
        String url = versionManager.getVendor();
        JLabel link = GGraphicsFactory.createLinkedLabel(url, url);
        Object[] message = { appName, GDictionary.get("Version", version), link };
        JOptionPane.showMessageDialog(documentHandler.getOwnerFrame(),
            message, GDictionary.get("AboutGeometria"), JOptionPane.OK_CANCEL_OPTION, icon);
        return true;
    }
}
