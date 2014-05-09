/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.view.GProblemAnswerDialog;

import org.apache.log4j.Logger;

public class GProblemAnswerAction implements GUndoable, GActionWithHelp {

    private GAnswer oldAnswer;
    
    private GAnswer newAnswer;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler, boolean quietMode) {
        logger.info("");
        GProblem document = (GProblem)documentHandler.getActiveDocument();
        if (quietMode) {
            document.setAnswer(newAnswer);
        }
        else {
            oldAnswer = document.getAnswer();
            GProblemAnswerDialog dialog = new GProblemAnswerDialog( documentHandler.getOwnerFrame(), this, document);
            dialog.setVisible(true);
            GAnswer answer = dialog.getAnswer();
            if (answer == null) {
                return false;
            }
            document.setAnswer(answer);
            newAnswer = answer;
        }
        documentHandler.setDocumentModified(true);
        logger.info(oldAnswer + ", " + newAnswer);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GProblem document = (GProblem)documentHandler.getActiveDocument();
        document.setAnswer(oldAnswer);
        logger.info(oldAnswer + ", " + newAnswer);
    }

    public String getShortDescription() {
        return "answer";
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
