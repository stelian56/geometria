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
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GAnswerAction implements GLoggable, GActionWithHelp {

    private GAction action;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler, boolean quietMode) {
        logger.info(quietMode);
        GDocument document = documentHandler.getActiveDocument();
        if (!quietMode) {
            if (document instanceof GProblem) {
                action = new GProblemAnswerAction();
                ((GProblemAnswerAction)action).setHelpId(helpId);
            }
            else {
                action = new GSolutionAnswerAction();
                ((GSolutionAnswerAction)action).setHelpId(helpId);
            }
        }
        return action.execute(documentHandler, quietMode);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        ((GUndoable)action).undo(documentHandler);
    }

    public GLoggable clone() {
        GAnswerAction a = new GAnswerAction();
        if (action instanceof GSolutionAnswerAction)
            a.action = ((GSolutionAnswerAction)action).clone();
        return a;
    }

    public String toLogString() {
        return ((GLoggable)action).toLogString();
    }

    public void make(Element node) throws Exception {
        logger.info("");
        ((GLoggable)action).make(node);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        ((GLoggable)action).serialize(buf);
    }

    public String getShortDescription() {
        return GDictionary.get("answer");
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
