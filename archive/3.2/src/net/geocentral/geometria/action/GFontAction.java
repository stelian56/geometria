/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GOptions;
import net.geocentral.geometria.util.GOptionsManager;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;

public class GFontAction implements GAction {

    private int fontSize;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GFontAction(String parameter) {
        fontSize = Integer.valueOf(parameter);
    }
    
    public boolean execute() {
        logger.info(String.format("Setting font size to %d", fontSize));
        GOptions options = GOptionsManager.getInstance().getOptions();
        int currentFontSize = options.getFont().getSize();
        if (currentFontSize == fontSize) {
            return true;
        }
        options.setFontSize(fontSize);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        try {
            documentHandler.fontChanged();
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return false;
        }
        return true;
    }
}
