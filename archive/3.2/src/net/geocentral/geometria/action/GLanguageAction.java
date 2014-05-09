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

public class GLanguageAction implements GAction {

    private String language;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLanguageAction(String parameter) {
        language = parameter;
    }
    
    public boolean execute() {
        logger.info(String.format("Setting language to %s", language));
        GOptions options = GOptionsManager.getInstance().getOptions();
        String currentLanguage = options.getLanguage();
        if (currentLanguage.equals(language)) {
            return true;
        }
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        try {
            GAction action = new GCloseDocumentAction();
            if (!action.execute()) {
                return false;
            }
            options.setLanguage(language);
            documentHandler.languageChanged();
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return false;
        }
        return true;
    }
}
