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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class GDocument extends GXmlEntity {

    protected LinkedHashMap<String, GFigure> figures;

    protected GNotepad notepad;

    private boolean prime;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GDocument() {
        figures = new LinkedHashMap<String, GFigure>();
        notepad = new GNotepad(this);
    }

    public void removeAllFigures() {
        logger.info(figures.keySet());
        figures = new LinkedHashMap<String, GFigure>();
    }

    public GFigure getSelectedFigure() {
        for (GFigure figure : figures.values()) {
            if (figure.isSelected())
                return figure;
        }
        return null;
    }

    public void setSelectedFigure(String figureName) {
        logger.info(figureName);
        for (GFigure figure : figures.values()) {
            if (figure.isSelected()) {
                figure.setSelected(false);
                break;
            }
        }
        GFigure figure = figures.get(figureName);
        figure.setSelected(true);
    }

    public void setSelectedFigure(int index) {
        logger.info(index);
        if (figures.isEmpty())
            return;
        Iterator<GFigure> it = figures.values().iterator();
        GFigure figure = null;
        for (int i = 0; i <= index; i++) {
            if (!it.hasNext())
                return;
            figure = it.next();
        }
        figure.setSelected(true);
    }

    public void addFigure(GFigure figure, int index) {
        logger.info(figure + ", " + index);
        LinkedHashMap<String, GFigure> fs = new LinkedHashMap<String, GFigure>(); 
        int i = 0;
        for (Iterator<GFigure> it = figures.values().iterator(); ; ++i) {
            if (i == index) {
                fs.put(figure.getName(), figure);
            }
            if (!it.hasNext()) {
                break;
            }
            GFigure f = it.next();
            fs.put(f.getName(), f);
        }
        figures = fs;
    }
    
    public void addFigure(GFigure figure) {
        logger.info(figure);
        figures.put(figure.getName(), figure);
    }

    public int removeFigure(String figureName) {
        logger.info(figureName);
        int index = 0;
        for (Iterator<GFigure> it = figures.values().iterator(); it.hasNext(); ++index) {
            if (it.next().getName().equals(figureName)) {
                break;
            }
        }
        GFigure figure = figures.remove(figureName);
        if (figure.isSelected() && !figures.isEmpty()) {
            Iterator<GFigure> it = figures.values().iterator();
            GFigure f = null;
            while (it.hasNext())
                f = it.next();
            if (f != null)
                f.setSelected(true);
        }
        return index;
    }

    public void renameFigure(GFigure figure, String newName) {
        logger.info(figure + ", " + newName);
        LinkedHashMap<String, GFigure> fs = new LinkedHashMap<String, GFigure>();
        for (Iterator<GFigure> it =
                figures.values().iterator(); it.hasNext();) {
            GFigure f = it.next();
            if (f == figure)
                figure.setName(newName);
            fs.put(f.getName(), f);
        }
        figures = fs;
    }

    public GNotepadVariable getVariable(String variableName) {
        GNotepadRecord record = notepad.getRecord(variableName);
        return record == null ? null : record.getVariable();
    }

    public boolean hasFigures() {
        return !figures.isEmpty();
    }

    public GFigure getFigure(String figureName) {
        return figures.get(figureName);
    }

    public List<String> getFigureNames() {
        List<String> figureNames = new ArrayList<String>();
        for (String figureName : figures.keySet()) {
            figureNames.add(figureName);
        }
        return figureNames;
    }

    public Iterator<GFigure> getFigureIterator() {
        return figures.values().iterator();
    }

    public int getFigureCount() {
        return figures.size();
    }

    public GNotepad getNotepad() {
        return notepad;
    }

    public boolean isPrime() {
        return prime;
    }

    public void setPrime(boolean prime) {
        this.prime = prime;
    }

    abstract public GDocumentEnvelope getEnvelope();

    abstract public void setEnvelope(GDocumentEnvelope envelope);
}
