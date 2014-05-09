/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector2d;

public class GStar {

    public static final int LABEL_CLEARANCE = 14;

    public GPoint3d owner;

    private List<GPoint3d> neighbors;

    public GStar(GPoint3d owner) {
        this.owner = owner;
        neighbors = new ArrayList<GPoint3d>();
    }

    public void addNeighbor(GPoint3d p) {
        if (!neighbors.contains(p))
            neighbors.add(p);
    }

    public Point fitLabel(int labelWidth, int labelAscent) {
        // Angles off X axis
        List<Double> angles = new ArrayList<Double>();
        for (GPoint3d p : neighbors) {
            Vector2d v = new Vector2d(p.scrCoords.x - owner.scrCoords.x,
                    p.scrCoords.y - owner.scrCoords.y);
            v.normalize();
            double angle = Math.acos(v.x);
            if (v.y < 0)
                angle = 2 * Math.PI - angle;
            angles.add(angle);
        }
        Collections.sort(angles);
        angles.add(angles.get(0) + 2 * Math.PI);
        double angle1 = 0;
        double angle2 = 2 * Math.PI;
        double gap = 0;
        for (int i = 0; i < angles.size() - 1; i++) {
            if (angles.get(i + 1) - angles.get(i) > gap) {
                angle1 = angles.get(i);
                angle2 = angles.get(i + 1);
                gap = angle2 - angle1;
                // A pi/2 gap is sufficient to fit a label
                if (gap > Math.PI / 2)
                    break;
            }
        }
        double angle = (angle1 + angle2) / 2;
        Point labelPos = new Point(owner.scrCoords.x
                + (int)(LABEL_CLEARANCE * Math.cos(angle)), owner.scrCoords.y
                + (int)(LABEL_CLEARANCE * Math.sin(angle)));
        labelPos.translate(-labelWidth / 2, labelAscent / 2);
        return labelPos;
    }
}
