/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GLoggable;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GLogMouseAdapter;

import org.apache.log4j.Logger;

public class GLogPane extends JPanel {

    private GSolution document;

    private JList actionList;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
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
        createActionList();
        actionList.setModel(log.getModel());
        log.setSelectionModel(actionList.getSelectionModel());
        actionList.setCellRenderer(new LogRecordRenderer(log));
        JScrollPane sp = new JScrollPane(actionList);
        add(sp);
    }
    
    private void createActionList() {
        actionList = new JList() {
            public String getToolTipText(MouseEvent event) {
                ListModel model = getModel();
                if (model.getSize() < 1) {
                    return null;
                }
                int index = locationToIndex(event.getPoint());
                GLoggable action = (GLoggable)model.getElementAt(index);
                return action.getComments();
            }
            private static final long serialVersionUID = 1L;
        };
        new GLogMouseAdapter(this, actionList);
    }

    public void popupMenu(MouseEvent event) {
        Point point = event.getPoint();
        int index = actionList.locationToIndex(point);
        GLoggable action = (GLoggable)actionList.getModel().getElementAt(index);
        GLogPopupMenu popup = new GLogPopupMenu(action);
        popup.show(this, point.x, point.y);
    }

    private static final long serialVersionUID = 1L;
}

class LogRecordRenderer extends JLabel implements ListCellRenderer {

    private static final Color COMMENTED_COLOR = new Color(0xff, 0xff, 0x80);

    private GLog log;

    public LogRecordRenderer(GLog log) {
        this.log = log;
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        GLoggable record = (GLoggable) value;
        String comments = record.getComments();
        setText(record.toLogString());
        if (index == log.getCurrentPos()) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else if (comments != null){
            setBackground(COMMENTED_COLOR);
            setForeground(list.getForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }

    private static final long serialVersionUID = 1L;
}
