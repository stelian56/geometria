/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GCamera implements Cloneable {

    private Matrix3d attitude;

    private Matrix3d initialAttitude;

    private GCameraSpinner spinner;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GCamera() {
        initialAttitude = new Matrix3d();
        initialAttitude.set(defaultAttitude());
        attitude = new Matrix3d();
        attitude.set(initialAttitude);
    }

    public GCamera clone() {
        GCamera camera = new GCamera();
        camera.initialAttitude = new Matrix3d(initialAttitude);
        camera.attitude = new Matrix3d(attitude);
        return camera;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        makeAttitude(node);
    }

    private void makeAttitude(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("attitude");
        if (ns.getLength() == 0)
            // Camera has default attitude
            return;
        attitude = new Matrix3d();
        String[] tokens = ns.item(0).getTextContent().split(" ");
        int index = 0;
        Quat4d quat;
        quat = new Quat4d(Double.valueOf(tokens[index++]),
            Double.valueOf(tokens[index++]), Double.valueOf(tokens[index++]),
            Double.valueOf(tokens[index]));
        initialAttitude = new Matrix3d();
        initialAttitude.set(quat);
        attitude = new Matrix3d();
        attitude.set(initialAttitude);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<camera>");
        Quat4d quat = new Quat4d();
        quat.set(attitude);
        buf.append("\n<attitude>");
        buf.append(String.valueOf(quat.x));
        buf.append(' ');
        buf.append(String.valueOf(quat.y));
        buf.append(' ');
        buf.append(String.valueOf(quat.z));
        buf.append(' ');
        buf.append(String.valueOf(quat.w));
        buf.append("</attitude>");
        buf.append("\n</camera>");
    }

    public void turn(Vector3d axis, int stroke, GFigure figure) {
        double angle = stroke * GCameraSpinner.SPIN_ANGLE;
        AxisAngle4d axisAngle = new AxisAngle4d(axis, angle);
        Matrix3d rotMatrix = new Matrix3d();
        rotMatrix.set(axisAngle);
        rotMatrix.mulNormalize(attitude);
        attitude.set(rotMatrix);
        figure.cameraTurned();
    }

    public void spin(Vector3d axis, int stroke, int delay, GFigure fig) {
        spinner = new GCameraSpinner(this, axis, stroke, delay, fig);
        new Thread(spinner).start();
    }

    public void seize() {
        if (spinner != null)
            spinner.seize();
    }

    public boolean visible(GFace face, GSolid solid, Point3d refPoint) {
        Vector3d on = new Vector3d(face.getNormal(solid, refPoint));
        attitude.transform(on);
        return on.z > 0;
    }

    public static Matrix3d defaultAttitude() {
        AxisAngle4d axisAngle1 = new AxisAngle4d(1, 1, 1, -Math.PI * 2 / 3);
        Matrix3d m1 = new Matrix3d();
        m1.set(axisAngle1);
        AxisAngle4d axisAngle2 = new AxisAngle4d(0, 1, 0, Math.PI / 7);
        Matrix3d m2 = new Matrix3d();
        m2.set(axisAngle2);
        AxisAngle4d axisAngle3 = new AxisAngle4d(1, 0, 0, Math.PI / 15);
        Matrix3d m3 = new Matrix3d();
        m3.set(axisAngle3);
        m2.mul(m1);
        m3.mul(m2);
        return m3;
    }

    public Matrix3d getAttitude() {
        return attitude;
    }

    public Matrix3d getInitialAttitude() {
        return initialAttitude;
    }
    
    public void setInitialAttitude(Matrix3d initialAttitude) {
        this.initialAttitude = initialAttitude;
    }
    
    public void setAttitude(Matrix3d attitude) {
        this.attitude = attitude;
    }

    public void toInitialAttitude() {
        attitude.set(initialAttitude);
    }

    public void toDefaultAttitude() {
        attitude.set(defaultAttitude());
    }

    public String toString() {
        return String.valueOf(attitude);
    }
}
