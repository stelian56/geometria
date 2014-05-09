/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GPoint3d implements Cloneable, GSelectable {

    private String label;

    public Point3d coords;

    public Point2d projCoords;

    public Point scrCoords;

    private boolean vertex;

    private List<GLine> lines;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GPoint3d() {
        lines = new ArrayList<GLine>();
    }

    public GPoint3d(double x, double y, double z, String label) {
        this();
        this.coords = new Point3d(x, y, z);
        this.label = label;
    }

    public GPoint3d(Point3d coords, String label) {
        this();
        this.coords = new Point3d(coords);
        this.label = label;
    }

    public GPoint3d clone() {
        GPoint3d p = new GPoint3d();
        p.label = label;
        p.vertex = vertex;
        p.coords = new Point3d(coords);
        return p;
    }

    public void make(Element node) throws Exception {
        makeLabel(node);
        makeCoords(node);
    }

    private void makeLabel(Element node) throws Exception {
        label = node.getElementsByTagName("label").item(0).getTextContent();
        GLabelFactory.getInstance().validateLabel(label);
    }

    private void makeCoords(Element node) throws Exception {
        NodeList ns = node.getElementsByTagName("coords");
        String coordsString = ns.item(0).getTextContent();
        coords = GStringUtils.coordsFromString(coordsString);
        if (coords == null) {
            logger.error(coords);
            throw new Exception();
        }
    }

    public void project(GCamera camera, Point3d centerCoords) {
        Matrix3d attitude = camera.getAttitude();
        Point3d css = new Point3d(coords);
        css.sub(centerCoords);
        Point3d cs = new Point3d();
        attitude.transform(css, cs);
        projCoords = new Point2d(cs.x, cs.y);
    }

    public void serialize(StringBuffer buf) {
        buf.append("\n<point>");
        buf.append("\n<label>");
        buf.append(label);
        buf.append("</label>");
        buf.append("\n<coords>");
        buf.append(String.valueOf(coords.x));
        buf.append(' ');
        buf.append(String.valueOf(coords.y));
        buf.append(' ');
        buf.append(String.valueOf(coords.z));
        buf.append("</coords>");
        buf.append("\n</point>");
    }

    public void trimCoords(double epsilon) {
        if (Math.abs(coords.x) < epsilon)
            coords.x = 0;
        if (Math.abs(coords.y) < epsilon)
            coords.y = 0;
        if (Math.abs(coords.z) < epsilon)
            coords.z = 0;
    }

    public String getLabel() {
        return label;
    }

    public void resetLines() {
        lines = new ArrayList<GLine>();
    }

    public void addLine(GLine line) {
        lines.add(line);
    }

    public int lineCount() {
        return lines.size();
    }

    public GLine lineAt(int i) {
        return lines.get(i);
    }

    public Collection<GFace> getFaces() {
        Collection<GFace> faces = new LinkedHashSet<GFace>();
        for (GLine line : lines) {
            GFace face = line.getFace();
            faces.add(face);
        }
        return faces;
    }

    public boolean isVertex() {
        return vertex;
    }

    public void setVertex(boolean vertex) {
        this.vertex = vertex;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String toString() {
        return label == null ? coords.toString() : label + coords;
    }
}
