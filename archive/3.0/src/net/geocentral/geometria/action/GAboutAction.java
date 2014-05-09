/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.geocentral.geometria.util.GApplicationManager;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GAboutAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info("");
        ImageIcon icon = new ImageIcon(
                GAboutAction.class.getResource("/images/Logo.png"));
        String appName = GApplicationManager.getInstance().getApplicationName();
        String version = GApplicationManager.getInstance().getVersion();
        String url = GApplicationManager.getInstance().getHomeUrl();
        String message = GDictionary.get(appName) + "\n"
            + GDictionary.get("Version", version) + "\n" + url;
        JOptionPane.showMessageDialog(documentHandler.getOwnerFrame(),
                message, GDictionary.get("AboutGeometria"),
                JOptionPane.OK_CANCEL_OPTION, icon);
        return true;
    }
}
