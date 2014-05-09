/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.util.GNotepadMouseAdapter;

import org.apache.log4j.Logger;

public class GNotepadPane extends JPanel {

    private JList recordList;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GNotepadPane() {
        logger.info("");
        layoutComponents();
    }

    private void layoutComponents() {
        logger.info("");
        setLayout(new BorderLayout());
        recordList = new JList();
        new GNotepadMouseAdapter(this, recordList);
        recordList.setCellRenderer(new NotepadRecordRenderer());
        JScrollPane sp = new JScrollPane(recordList);
        add(sp);
    }

    public void documentChanged(GDocument document) {
        logger.info("");
        if (document != null) {
            GNotepad notepad = document.getNotepad();
            recordList.setModel(notepad.getModel());
            ListSelectionModel selectionModel = recordList.getSelectionModel();
            selectionModel.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION);
            notepad.setSelectionModel(selectionModel);
        }
        else
            recordList.setModel(new DefaultListModel());
    }

    public void repaintRecords() {
        recordList.repaint();
    }

    public void popupMenu(int x, int y) {
        GNotepadPopupMenu popup = new GNotepadPopupMenu(this);
        popup.show(this, x, y);
    }

    public void clearSelection() {
        recordList.clearSelection();
    }
    
    private static final long serialVersionUID = 1L;

    public boolean isSelectionEmpty() {
        return recordList.isSelectionEmpty();
    }
}

class NotepadRecordRenderer extends JLabel implements ListCellRenderer {

    public NotepadRecordRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        GNotepadRecord record = (GNotepadRecord)value;
        setText(record.toString());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }

    private static final long serialVersionUID = 1L;
}
