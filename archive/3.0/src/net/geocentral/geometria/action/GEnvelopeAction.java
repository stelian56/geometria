/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GDocumentEnvelope;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.view.GEnvelopeDialog;
import net.geocentral.geometria.view.GHelpOkCancelDialog;

import org.apache.log4j.Logger;

public class GEnvelopeAction implements GUndoable, GActionWithHelp {

    private GDocumentEnvelope oldEnvelope;
    private GDocumentEnvelope newEnvelope;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler, boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        if (quietMode) {
            document.setEnvelope(newEnvelope);
        }
        else {
            oldEnvelope = document.getEnvelope();
            GEnvelopeDialog dialog = new GEnvelopeDialog(documentHandler.getOwnerFrame(), document, this);
            dialog.setVisible(true);
            if (dialog.getOption() != GHelpOkCancelDialog.OK_OPTION) {
                return false;
            }
            newEnvelope = document.getEnvelope();
        }
        documentHandler.setDocumentModified(true);
        logger.info(oldEnvelope + ", " + newEnvelope);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        newEnvelope = document.getEnvelope().clone();
        document.setEnvelope(oldEnvelope);
        logger.info(oldEnvelope + ", " + newEnvelope);
    }

    public String getShortDescription() {
        return GDictionary.get("editEnvelope");
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
