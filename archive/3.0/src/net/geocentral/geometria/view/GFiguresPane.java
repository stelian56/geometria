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
import java.util.Iterator;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.event.GDocumentModifiedEvent;
import net.geocentral.geometria.event.GEventHandler;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;

import org.apache.log4j.Logger;

public class GFiguresPane extends JTabbedPane implements ChangeListener {

    private GEventHandler eventHandler;

    private GDocument document;

    public static final int SCROLL_INCREMENT = 15;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GFiguresPane() {
        eventHandler = new GEventHandler();
        eventHandler.addListener(GDocumentHandler.getInstance());
        addChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        int index = getSelectedIndex();
        if (index < 0)
            return;
        logger.info(index);
        String title = getTitleAt(index);
        document.setSelectedFigure(title);
        eventHandler.fireEvent(new GDocumentModifiedEvent(this));
    }

    public void documentChanged(GDocument document) {
        this.document = document;
        removeChangeListener(this);
        removeAll();
        int selectedIndex = -1;
        int i = 0;
        for (Iterator<GFigure> it = document.getFigureIterator(); it.hasNext(); i++) {
            GFigure figure = it.next();
            addFigure(figure);
            if (figure.isSelected()) {
                selectedIndex = i;
            }
        }
        if (selectedIndex >= 0) {
            // Although the following line is seemingly redundant, removing it results in stateChanged()
            // being called even after the change listener has been removed. No explanation found.
            removeChangeListener(this);
            setSelectedIndex(selectedIndex);
        }
        addChangeListener(this);
    }

    public void selectionChanged() {
        int index = 0;
        for (Iterator<GFigure> it = document.getFigureIterator(); it.hasNext(); index++) {
            GFigure figure = it.next();
            if (figure.isSelected()) {
                removeChangeListener(this);
                setSelectedIndex(index);
                addChangeListener(this);
                return;
            }
        }
    }

    public Component addFigure(GFigure figure, int index) {
        logger.info(figure.getName() + ", " + index);
        removeChangeListener(this);
        GFigurePane figurePane = new GFigurePane(figure);
        JScrollPane sp = new JScrollPane(figurePane);
        sp.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        sp.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        figurePane.setViewPort(sp.getViewport());
        Component tab = add(sp, index);
        setTitleAt(index, figure.getName());
        setSelectedComponent(tab);
        addChangeListener(this);
        return tab;
    }

    public Component addFigure(GFigure figure) {
        logger.info(figure.getName());
        return addFigure(figure, this.getTabCount());
    }

    public void removeFigure(String figureName) {
        logger.info(figureName);
        removeChangeListener(this);
        for (int i = 0; i < getTabCount(); i++) {
            String n = getTitleAt(i);
            if (n.equals(figureName)) {
                remove(i);
                break;
            }
        }
        addChangeListener(this);
    }

    public void removeAllFigures() {
        logger.info("");
        removeChangeListener(this);
        removeAll();
        addChangeListener(this);
    }
    
    public void renameFigure(String oldName, String newName) {
        logger.info(oldName + ", " + newName);
        removeChangeListener(this);
        for (int i = 0; i < getTabCount(); i++) {
            String name = getTitleAt(i);
            if (name.equals(oldName)) {
                setTitleAt(i, newName);
                break;
            }
        }
        addChangeListener(this);
    }

    private static final long serialVersionUID = 1L;
}
