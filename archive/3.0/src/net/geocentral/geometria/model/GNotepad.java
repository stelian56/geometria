/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;

import net.geocentral.geometria.action.GEraseLineAction;
import net.geocentral.geometria.action.GEraseSelectionAction;
import net.geocentral.geometria.action.GRemoveFigureAction;
import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.action.GRenamePointAction;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GNotepad {

    private GDocument document;

    private DefaultListModel model;

    private ListSelectionModel selectionModel;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GNotepad(GDocument document) {
        this.document = document;
        model = new DefaultListModel();
    }

    public void setSelectionModel(ListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    public void add(GNotepadRecord record) {
        logger.info(record);
        model.addElement(record);
    }

    public void removeLastRecord() {
        GNotepadRecord record =
            (GNotepadRecord)model.remove(model.getSize() - 1);
        logger.info(record);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<notepad>");
        for (int i = 0; i < model.size(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.serialize(buf);
        }
        buf.append("\n</notepad>");
    }

    public void make(Element node) throws Exception {
        logger.info("");
        ListDataListener[] listeners =
            model.getListeners(ListDataListener.class);
        for (ListDataListener listener : listeners)
            model.removeListDataListener(listener);
        NodeList ns = node.getElementsByTagName("record");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            GNotepadRecord record = new GNotepadRecord();
            record.make(n);
            add(record);
        }
        for (ListDataListener listener : listeners)
            model.addListDataListener(listener);
    }

    public void validate() throws Exception {
        logger.info("");
        Map<String, GNotepadVariable> variables =
            new LinkedHashMap<String, GNotepadVariable>();
        for (int i = 0; i < model.size(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.validate(document, variables);
            GNotepadVariable variable = record.getVariable();
            variables.put(variable.getName(), variable);
        }
    }

    public void update() {
        logger.info("");
        try {
            validate();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ListModel getModel() {
        return model;
    }

    public boolean isEmpty() {
        return model.isEmpty();
    }

    public int size() {
        return model.size();
    }

    public GNotepadRecord getRecord(String variableName) {
        for (int i = 0; i < model.size(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            GNotepadVariable variable = record.getVariable();
            if (variable.getName().equals(variableName))
                return record;
        }
        return null;
    }

    public List<GNotepadRecord> getRecords() {
        List<GNotepadRecord> records = new ArrayList<GNotepadRecord>();
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            records.add(record);
        }
        return records;
    }

    public void figureRenamed(GRenameFigureAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.figureRenamed(action);
        }
    }

    public void renameFigureUndone(GRenameFigureAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.renameFigureUndone(action);
        }
    }

    public void pointRenamed(GRenamePointAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.pointRenamed(action);
        }
    }

    public void renamePointUndone(GRenamePointAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.renamePointUndone(action);
        }
    }

    public void clear() {
        logger.info("");
        model.clear();
    }

    public List<GNotepadVariable> getVariables() {
        List<GNotepadVariable> variables = new ArrayList<GNotepadVariable>();
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            GNotepadVariable variable = record.getVariable();
            variables.add(variable);
        }
        return variables;
    }

    public void figureRemoved(GRemoveFigureAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.figureRemoved(action);
        }
    }

    public void removeFigureUndone(GRemoveFigureAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.removeFigureUndone(action);
        }
    }

    public void lineErased(GEraseLineAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.lineErased(action);
        }
    }

    public void eraseLineUndone(GEraseLineAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.eraseLineUndone(action);
        }
    }

    public void selectionErased(GEraseSelectionAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.selectionErased(action);
        }
    }

    public void eraseSelectionUndone(GEraseSelectionAction action) {
        logger.info("");
        for (int i = 0; i < model.getSize(); i++) {
            GNotepadRecord record = (GNotepadRecord)model.get(i);
            record.eraseSelectionUndone(action);
        }
    }

    public GNotepadRecord getSelectedRecord() {
        int index = selectionModel.getMaxSelectionIndex();
        if (index < 0)
            return null;
        return (GNotepadRecord)
        model.getElementAt(index);
    }
}
