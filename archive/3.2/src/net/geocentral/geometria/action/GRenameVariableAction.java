/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.List;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GRenameVariableDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GRenameVariableAction implements GLoggable, GActionWithHelp {

    private String oldName;

    private String newName;

    private GDocument document;
    
    private GNotepad notepad;

    private String helpId;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        document = documentHandler.getActiveDocument();
        notepad = document.getNotepad();
        if (silent) {
            try {
                validateApply();
            }
            catch (Exception exception) {
                return false;
            }
        }
        else {
            prefillSelection();
            List<GNotepadVariable> variables = notepad.getVariables();
            String[] variableNames = new String[variables.size()];
            for (int i = 0; i < variables.size(); i++) {
                variableNames[i] = variables.get(i).getName();
            }
            GRenameVariableDialog dialog =
                new GRenameVariableDialog(documentHandler.getOwnerFrame(), this, variableNames);
            dialog.prefill(oldName);
            dialog.setVisible(true);
            if (!dialog.getResult()) {
                return false;
            }
            notepad.clearSelection();
        }
        documentHandler.notepadChanged();
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        logger.info(String.format("Variable %s renamed to %s", oldName, newName));
        return true;
    }

    private void prefillSelection() {
        GNotepadRecord notepadRecord = document.getNotepad().getSelectedRecord();
        if (notepadRecord != null) {
            oldName = notepadRecord.getVariable().getName();
        }
    }
    
    public void setInput(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public void validateApply() throws Exception {
        if (newName.trim().length() == 0) {
            logger.error("No name");
            throw new Exception(GDictionary.get("EnterNewVariableName"));
        }
        if (oldName.equals(newName))
        {
            logger.error(String.format("New name same as old, %s", oldName));
            throw new Exception(GDictionary.get("NamesAreSame"));
        }
        if (!newName.matches(GLabelFactory.VARIABLE_NAME_PATTERN)) {
            logger.info(String.format("Bad variable name: %s", newName));
            throw new Exception(GDictionary.get("InvalidVariable", newName));
        }
        if (document.getVariable(newName) != null) {
            logger.error(String.format("Duplicate variable %", newName));
            throw new Exception(GDictionary.get("DuplicateVariable", newName));
        }
        document.getNotepad().renameVariable(this);
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        document.getNotepad().undoRenameVariable(this);
        documentHandler.notepadChanged();
        logger.info(String.format("Rename variable %s to %s undone", oldName, newName));
    }
    
    public GLoggable clone() {
        GRenameVariableAction action = new GRenameVariableAction();
        action.oldName = oldName;
        action.newName = newName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("RenameVariableTo", oldName, newName);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("oldName");
        if (ns.getLength() == 0) {
            logger.error("No old name");
            throw new Exception();
        }
        oldName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("newName");
        if (ns.getLength() == 0) {
            logger.error("No new name");
            throw new Exception();
        }
        newName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("comments");
        if (ns.getLength() > 0) {
            String s = ns.item(0).getTextContent();
            comments = GStringUtils.fromXml(s);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>")
            .append("\n<className>")
            .append(this.getClass().getSimpleName())
            .append("</className>")
            .append("\n<oldName>")
            .append(oldName)
            .append("</oldName>")
            .append("\n<newName>")
            .append(newName)
            .append("</newName>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("renameVariable", oldName, newName);
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public List<GNotepadVariable> getVariables() {
        return notepad.getVariables();
    }
    
    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
