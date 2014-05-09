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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;

import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.util.GFigureMouseAdapter;

public class GFigurePane extends JPanel {

    private GFigure figure;

    private JViewport viewport;

    public GFigurePane(GFigure figure) {
        this.figure = figure;
        setBackground(Color.WHITE);
        figure.setPane(this);
        new GFigureMouseAdapter(this, figure);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        figure.paint(g2d);
    }

    public void setViewPort(JViewport viewport) {
        this.viewport = viewport;
    }

    public ChangeListener[] removeChangeListeners() {
        ChangeListener[] listeners = viewport.getChangeListeners();
        for (ChangeListener listener : listeners) {
            viewport.removeChangeListener(listener);
        }
        return listeners;
    }

    public void addChangeListeners(ChangeListener[] listeners) {
        for (ChangeListener listener : listeners)
            viewport.addChangeListener(listener);
    }

    public void repositionViewport(double factor) {
        Rectangle vpRect = viewport.getViewRect();
        Point vpCenter = new Point(vpRect.x + vpRect.width / 2, vpRect.y + vpRect.height / 2);
        Point newVpCenter = new Point((int)(vpCenter.x * factor), (int)(vpCenter.y * factor));
        Point newVpPosition = new Point(newVpCenter.x - vpRect.width / 2, newVpCenter.y - vpRect.height / 2);
        viewport.setViewPosition(newVpPosition);
    }

    public Dimension getViewSize() {
        return getParent().getSize();
    }

    private static final long serialVersionUID = 1L;
}
