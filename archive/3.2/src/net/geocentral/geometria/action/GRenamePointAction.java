/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Set;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.view.GRenamePointDialog;

import org.apache.log4j.Logger;

public class GRenamePointAction implements GUndoable, GFigureAction, GActionWithHelp {

    private String oldLabel;

    private String newLabel;

    private String figureName;

    private GDocument document;

    private GFigure figure;
    
    private GSolid solid;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        document = documentHandler.getActiveDocument();
        if (silent) {
            try {
                validateApply();
            }
            catch (Exception exception) {
                return false;
            }
        }
        else {
            figure = document.getSelectedFigure();
            figureName = figure.getName();
            solid = figure.getSolid();
            prefillSelection();
            GRenamePointDialog dialog = new GRenamePointDialog(documentHandler.getOwnerFrame(), this);
            dialog.prefill(oldLabel);
            dialog.setVisible(true);
            if (!dialog.getResult()) {
                return false;
            }
            solid.clearSelection();
        }
        document.getNotepad().pointRenamed(this);
        documentHandler.notepadChanged();
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        logger.info(figureName + ", " + oldLabel + ", " + newLabel);
        return true;
    }

    private void prefillSelection() {
        Set<GSelectable> selection = solid.getSelection();
        for (GSelectable element : selection) {
            if (element instanceof GPoint3d) {
                oldLabel = ((GPoint3d)element).getLabel();
                break;
            }
        }
    }
    
    public void setInput(String oldLabel, String newLabel) {
        this.oldLabel = oldLabel;
        this.newLabel = newLabel;
    }
    
    public void validateApply() throws Exception {
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        if (oldLabel.trim().length() == 0 || newLabel.trim().length() == 0) {
            logger.error("No label");
            throw new Exception(GDictionary.get("EnterLabels"));
        }
        if (solid.getPoint(oldLabel) == null) {
            logger.error("No such point " + oldLabel);
            throw new Exception(GDictionary.get("NoSuchPoint", oldLabel));
        }
        if (oldLabel.equals(newLabel))
        {
            logger.error(oldLabel + ", " + newLabel);
            throw new Exception(GDictionary.get("LabelsAreSame"));
        }
        if (!newLabel.matches(GLabelFactory.LABEL_PATTERN)) {
            logger.error("Bad label " + newLabel);
            throw new Exception(GDictionary.get("BadLabel", newLabel));
        }
        if (solid.getPoint(newLabel) != null) {
            logger.error("Duplicate label " + newLabel);
            throw new Exception(GDictionary.get("DuplicateLabel", newLabel));
        }
        solid.RenamePoint(oldLabel, newLabel);
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.RenamePoint(newLabel, oldLabel);
        solid.makeConfig();
        document.getNotepad().renamePointUndone(this);
        documentHandler.notepadChanged();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + newLabel + ", " + oldLabel);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<oldLabelName>");
        buf.append(oldLabel);
        buf.append("</oldLabel>");
        buf.append("\n<newLabel>");
        buf.append(newLabel);
        buf.append("</newLabel>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("renamePoint", oldLabel, newLabel, figureName);
    }

    public String getFigureName() {
        return figureName;
    }

    public String getOldLabel() {
        return oldLabel;
    }

    public String getNewLabel() {
        return newLabel;
    }
    
    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
