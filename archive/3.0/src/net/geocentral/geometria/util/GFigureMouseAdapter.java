/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Date;

import javax.vecmath.Vector3d;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.model.GCamera;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.view.GFigurePane;

public class GFigureMouseAdapter extends MouseAdapter implements
        MouseMotionListener {

    public static final int MAX_SPIN_START_DELAY = 250; // ms

    private GFigure figure;

    private GCamera camera;

    private int xDown;

    private int yDown;

    private int xUp;

    private int yUp;

    private Date lastDragged;

    private int lastStroke;

    private int lastStrokeTime;

    private boolean cameraSpinEligible;

    public GFigureMouseAdapter(GFigurePane figurePane, GFigure figure) {
        this.figure = figure;
        camera = figure.getCamera();
        figurePane.addMouseListener(this);
        figurePane.addMouseMotionListener(this);
    }

    public void mousePressed(MouseEvent e) {
        xDown = e.getX();
        yDown = e.getY();
        camera.seize();
        if (e.isPopupTrigger()) {
            figure.popupMenu(e.getX(), e.getY());
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON1
                && GDocumentHandler.getInstance().isSelectorOn()) {
            figure.select(e);
            return;
        }
    }

    public void mouseReleased(MouseEvent e) {
        xUp = e.getX();
        yUp = e.getY();
        if (e.isPopupTrigger()) {
            figure.popupMenu(e.getX(), e.getY());
            return;
        }
        if (GDocumentHandler.getInstance().isSelectorOn())
            return;
    }

    public void mouseDragged(MouseEvent e) {
        if (GDocumentHandler.getInstance().isSelectorOn())
            return;
        int nextXDown = e.getX();
        int nextYDown = e.getY();
        int dx = nextXDown - xDown;
        int dy = nextYDown - yDown;
        lastDragged = new Date();
        Vector3d axis = new Vector3d(dy, dx, 0);
        lastStroke = (int)Math.sqrt(dx * dx + dy * dy);
        lastStrokeTime = (int)(new Date().getTime() - lastDragged.getTime());
        camera.turn(axis, lastStroke, figure);
        cameraSpinEligible = true;
        xDown = nextXDown;
        yDown = nextYDown;
    }

    public void mouseMoved(MouseEvent e) {
        if (GDocumentHandler.getInstance().isSelectorOn())
            return;
        int nextXUp = e.getX();
        int nextYUp = e.getY();
        int dx = nextXUp - xUp;
        int dy = nextYUp - yUp;
        if (lastDragged != null && new Date().getTime() - lastDragged.getTime()
                <= MAX_SPIN_START_DELAY && cameraSpinEligible) {
            cameraSpinEligible = false;
            Vector3d axis = new Vector3d(dy, dx, 0);
            camera.spin(axis, Math.max(lastStroke, 1),
                    Math.max(lastStrokeTime, 10), figure);
        }
        xUp = nextXUp;
        yUp = nextYUp;
    }
}
