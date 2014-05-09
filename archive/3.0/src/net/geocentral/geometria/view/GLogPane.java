/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GLoggable;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GLogPane extends JPanel {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    private GSolution document;
    
    public GLogPane() {
    }
    
    public void documentChanged(GSolution document) {
        if (document != this.document) {
            this.document = document;
            layoutComponents(document.getLog());
        }
    }

    private void layoutComponents(GLog log) {
        logger.info("");
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel upperPane = new JPanel();
        upperPane.setLayout(new BoxLayout(upperPane, BoxLayout.X_AXIS));
        upperPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        upperPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        upperPane.add(new JLabel(GDictionary.get("SolutionLog")));
        upperPane.add(Box.createHorizontalGlue());
        JToolBar toolBar = GToolBarHandler.getInstance().getToolBar("Log");
        upperPane.add(toolBar);
        add(upperPane);
        JList list = new JList();
        list.setModel(log.getModel());
        log.setSelectionModel(list.getSelectionModel());
        list.setCellRenderer(new LogRecordRenderer(log));
        JScrollPane sp = new JScrollPane(list);
        add(sp);
    }

    private static final long serialVersionUID = 1L;
}

class LogRecordRenderer extends JLabel implements ListCellRenderer {

    private GLog log;

    public LogRecordRenderer(GLog log) {
        this.log = log;
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        GLoggable record = (GLoggable) value;
        setText(record.toLogString());
        if (log.isPlaying()
                && index == log.getCurrentPos()) {
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
