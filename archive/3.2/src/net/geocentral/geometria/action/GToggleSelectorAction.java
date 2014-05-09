/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.geocentral.geometria.util.GIconManager;

import org.apache.log4j.Logger;

public class GToggleSelectorAction implements GAction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        documentHandler.setSelectorOn(!documentHandler.isSelectorOn());
        AbstractAction actionHandler = documentHandler.getActionHandler("view.toggleSelector");
        ImageIcon icon = GIconManager.getInstance().get24x24Icon(
            documentHandler.isSelectorOn() ? "SelectorOn.png" : "SelectorOff.png");
        actionHandler.putValue(AbstractAction.SMALL_ICON, icon);
        logger.info(documentHandler.isSelectorOn());
        return true;
    }
}
