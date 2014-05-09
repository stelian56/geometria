/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import javax.vecmath.Vector3d;

import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;

public class GCameraSpinner implements Runnable {

    public static final double SPIN_ANGLE = 7e-3;

    private GCamera camera;

    private boolean seized;

    private Vector3d axis;

    private int stroke;

    private int strokeTime;

    private GFigure figure;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GCameraSpinner(GCamera camera, Vector3d axis, int stroke, int delay,
            GFigure figure) {
        this.camera = camera;
        this.axis = axis;
        this.stroke = stroke;
        this.strokeTime = delay;
        this.figure = figure;
    }

    public void seize() {
        seized = true;
    }

    public void run() {
        try {
            while (!seized) {
                camera.turn(axis, stroke, figure);
                Thread.sleep(strokeTime);
            }
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
        }
        figure.repaint();
    }
}
