/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.action.GFigureAction;
import net.geocentral.geometria.action.GLoggable;
import net.geocentral.geometria.action.GSolutionAnswerAction;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GLog implements ListSelectionListener {

    private DefaultListModel model;

    private ListSelectionModel selectionModel;

    private List<GLoggable> clonedActions;

    private boolean playing = false;

    private int currentPos = -1;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLog() {
        model = new DefaultListModel();
    }

    public void setSelectionModel(ListSelectionModel sm) {
        selectionModel = sm;
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this);
    }

    public void add(GLoggable action) {
        model.addElement(action);
    }

    public void removeLast() {
        int index = model.size() - 1;
        logger.info(index);
        model.remove(index);
    }

    public void startPlaying() {
        logger.info("");
        cloneActions();
        selectionModel.removeSelectionInterval(0, size() - 1);
        currentPos = -1;
        playing = true;
    }

    private void cloneActions() {
        clonedActions = new ArrayList<GLoggable>();
        for (int i = 0; i < model.getSize(); i++) {
            GLoggable action = actionAt(i).clone();
            clonedActions.add(action);
        }
    }

    public void stopPlaying() {
        logger.info("");
        selectionModel.removeSelectionInterval(0, size() - 1);
        playing = false;
    }

    public void setCurrentPos(int index) {
        logger.info(index);
        selectionModel.removeSelectionInterval(0, size() - 1);
        selectionModel.setSelectionInterval(index, index);
    }

    public void advanceCurrentPos() {
        logger.info(currentPos);
        selectionModel.removeSelectionInterval(currentPos, currentPos);
        selectionModel.setSelectionInterval(currentPos + 1, currentPos + 1);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<log>");
        for (int i = 0; i < model.size(); i++) {
            GLoggable action = (GLoggable)model.get(i);
            action.serialize(buf);
        }
        buf.append("\n</log>");
    }

    public void make(Element node) throws Exception {
        logger.info("");
        ListDataListener[] listeners = (ListDataListener[])model.getListeners(ListDataListener.class);
        for (ListDataListener listener : listeners) {
            model.removeListDataListener(listener);
        }
        NodeList ns = node.getElementsByTagName("action");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            String packageName = GLoggable.class.getPackage().getName();
            String cName = n.getElementsByTagName("className").item(0).getTextContent();
            final String className = packageName + "." + cName;
            GLoggable action;
            try {
                action = (GLoggable)Class.forName(className).newInstance();
            }
            catch (Exception exception) {
                logger.error(GStringUtils.stackTraceToString(exception));
                throw exception;
            }
            action.make(n);
            add(action);
            if (action instanceof GSolutionAnswerAction) {
                break;
            }
        }
        for (ListDataListener listener : listeners) {
            model.addListDataListener(listener);
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting())
            return;
        if (selectionModel.isSelectionEmpty())
            return;
        logger.info("currentPos: " + currentPos);
        int newPos = selectionModel.getMinSelectionIndex();
        if (newPos == currentPos)
            return;
        if (!playing) {
            currentPos = newPos;
            return;
        }
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GSolution document = (GSolution)documentHandler.getActiveDocument();
        if (newPos > currentPos) {
            for (int i = currentPos + 1; i <= newPos; i++) {
                GLoggable action = clonedActions.get(i);
                try {
                    action.execute(true);
                }
                catch (Exception exception) {
                    logger.error(GStringUtils.stackTraceToString(exception));
                    return;
                }
            }
        }
        else {
            for (int i = currentPos; i > newPos; i--) {
                GLoggable action = clonedActions.get(i);
                action.undo(documentHandler);
            }
        }
        currentPos = newPos;
        GLoggable action = clonedActions.get(currentPos);
        if (action instanceof GFigureAction) {
            String figureName = ((GFigureAction)action).getFigureName();
            if (figureName != null) {
                document.setSelectedFigure(figureName);
                documentHandler.figureSelectionChanged();
            }
        }
        documentHandler.updateActionHandlerStates();
        logger.info("newPos: " + newPos);
    }

    public ListModel getModel() {
        return model;
    }

    public boolean isEmpty() {
        return model.isEmpty();
    }

    public GLoggable actionAt(int index) {
        return (GLoggable) model.get(index);
    }

    public int indexOf(GLoggable action) {
        return model.indexOf(action);
    }

    public GLoggable actionAtCurrentPos() {
        if (currentPos < 0) {
            return null;
        }
        return actionAt(currentPos);
    }

    public int size() {
        return model.size();
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getCurrentPos() {
        return currentPos;
    }
}
