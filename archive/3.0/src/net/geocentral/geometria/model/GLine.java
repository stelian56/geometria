/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.util.GMath;

public class GLine {

    private List<String> labels;

    private GFace face;

    private GLine twin;

    public GLine() {
        labels = new ArrayList<String>();
    }

    public GLine(String label1, String label2) {
        this();
        labels.add(label1);
        labels.add(label2);
    }

    public GLine(GStick stick) {
        this(stick.label1, stick.label2);
    }
    
    public GLine(List<String> labels) {
        this.labels = labels;
    }

    public GLine(GLine line, int fromIndex, int toIndex) {
        this();
        for (int i = fromIndex; i <= toIndex; i++)
            labels.add(line.labelAt(i));
    }

    public GLine(GLine line) {
        this(line, 0, line.labelCount() - 1);
    }

    public GLine clone() {
        GLine line = new GLine(this);
        return line;
    }

    public void serialize(StringBuffer buf) {
        buf.append("\n<line>");
        buf.append("\n<labels>");
        buf.append("\n<label>");
        buf.append(firstLabel());
        buf.append("</label>");
        buf.append("\n<label>");
        buf.append(lastLabel());
        buf.append("</label>");
        buf.append("\n</labels>");
        buf.append("\n</line>");
    }

    public int labelCount() {
        return labels.size();
    }

    public String labelAt(int i) {
        return labels.get(i);
    }

    public String firstLabel() {
        return labels.get(0);
    }

    public String lastLabel() {
        return labels.get(labels.size() - 1);
    }

    public boolean contains(String label) {
        return labels.contains(label);
    }

    public int indexOf(String label) {
        return labels.indexOf(label);
    }

    public void remove(String label) {
        labels.remove(label);
    }

    public double length(GSolid solid) {
        return solid.getPoint(firstLabel()).coords.distance(solid
                .getPoint(lastLabel()).coords);
    }

    public void reverse() {
        Collections.reverse(labels);
    }

    public GFace getFace() {
        return face;
    }

    public void setFace(GFace face) {
        this.face = face;
    }

    public void setTwin(GLine twin) {
        this.twin = twin;
    }

    public GLine getTwin() {
        return twin;
    }

    public boolean acquire(GPoint3d p, GSolid solid) {
        Point3d coords1 = solid.getPoint(firstLabel()).coords;
        Point3d coords2 = solid.getPoint(lastLabel()).coords;
        Point3d projection = GMath.project(p.coords, coords1, coords2);
        if (projection.distance(p.coords) > solid.getEpsilon())
            return false;
        return insert(p, solid);
    }

    // Add point p that is known to lie on this line, unless p already belongs
    // to
    // 'points'. Return true iff p was actually added.
    public boolean insert(GPoint3d p, GSolid solid) {
        double epsilon = solid.getEpsilon();
        if (solid.getPoint(firstLabel()).coords.distance(p.coords) < epsilon)
            return false;
        for (int i = 0; i < labels.size() - 1; i++) {
            Point3d pPrev = solid.getPoint(labels.get(i)).coords;
            Point3d pNext = solid.getPoint(labels.get(i + 1)).coords;
            if (pNext.distance(p.coords) < epsilon)
                return false;
            Vector3d ppPrev = new Vector3d();
            ppPrev.sub(pPrev, p.coords);
            Vector3d ppNext = new Vector3d();
            ppNext.sub(pNext, p.coords);
            if (ppNext.dot(ppPrev) < 0) {
                labels.add(i + 1, p.getLabel());
                return true;
            }
        }
        return false;
    }

    // Assume this line does not contain p, which lies on the same infinite line
    // as this line. Add point p to this line
    public void addPoint(GPoint3d p, GSolid solid) {
        double k = GMath.simpleRatio(solid.getPoint(firstLabel()).coords, solid
                .getPoint(lastLabel()).coords, p.coords);
        if (k > 0)
            insert(p, solid);
        else if (k > -1)
            labels.add(0, p.getLabel());
        else
            labels.add(p.getLabel());
    }

    public boolean acquire(GLine line, GSolid solid) {
        double epsilon = solid.getEpsilon();
        Point3d p1 = solid.getPoint(firstLabel()).coords;
        Point3d p2 = solid.getPoint(lastLabel()).coords;
        Point3d p3 = solid.getPoint(line.firstLabel()).coords;
        Point3d p4 = solid.getPoint(line.lastLabel()).coords;
        Point3d pr3 = GMath.project(p3, p1, p2);
        if (pr3.distance(p3) > epsilon)
            return false;
        Point3d pr4 = GMath.project(p4, p1, p2);
        if (pr4.distance(p4) > epsilon)
            return false;
        // Now we know that 'line' lies on the same infinite line as this
        double k3 = -GMath.simpleRatio(p3, p2, p1);
        double k4 = -GMath.simpleRatio(p4, p2, p1);
        if (k3 < -GMath.EPSILON && k4 < -GMath.EPSILON)
            // p3, p4 < p1 < p2
            return false;
        if (k3 > 1 + GMath.EPSILON && k4 > 1 + GMath.EPSILON)
            // p1 < p2 < p3, p4
            return false;
        if (k3 < -GMath.EPSILON)
            // p3 < p1
            labels.add(0, line.firstLabel());
        else if (k3 > 1 + GMath.EPSILON)
            // p3 > p2
            labels.add(line.firstLabel());
        else
            // p1 <= p3 <= p2
            insert(solid.getPoint(line.firstLabel()), solid);
        if (k4 < -GMath.EPSILON)
            // p4 < p1
            labels.add(0, line.lastLabel());
        else if (k4 > 1 + GMath.EPSILON)
            // p4 > p2
            labels.add(line.lastLabel());
        else
            // p1 <= p4 <= p2
            insert(solid.getPoint(line.lastLabel()), solid);
        for (int i = 1; i < line.labelCount() - 1; i++) {
            GPoint3d p = solid.getPoint(line.labelAt(i));
            insert(p, solid);
        }
        return true;
    }

    public Vector3d toVector(GSolid solid) {
        Vector3d v = new Vector3d(solid.getPoint(lastLabel()).coords);
        v.sub(solid.getPoint(firstLabel()).coords);
        return v;
    }

    public GPoint3d getPoint(Point3d coords, GSolid solid) {
        double epsilon = solid.getEpsilon();
        for (String label : labels) {
            GPoint3d p = solid.getPoint(label);
            if (p.coords.epsilonEquals(coords, epsilon))
                return p;
        }
        return null;
    }


    public void pointRenamed(String oldLabel, String newLabel) {
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label.equals(oldLabel)) {
                labels.remove(i);
                labels.add(i, newLabel);
                return;
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        for (String label : labels)
            buf.append(label);
        buf.append("]");
        return String.valueOf(buf);
    }
}
