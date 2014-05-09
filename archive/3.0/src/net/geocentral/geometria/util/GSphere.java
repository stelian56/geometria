/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

//http://www.flipcode.com/cgi-bin/fcarticles.cgi?show=64043
public class GSphere {

    private Point3d center;

    private double radius;

    public GSphere() {
        center = new Point3d();
    }

    public GSphere(Point3d p1) {
        center = new Point3d();
        center.set(p1);
        radius = 0;
    }

    public GSphere(Point3d p1, Point3d p2) {
        center = new Point3d();
        center.add(p1, p2);
        center.scale(0.5);
        radius = center.distance(p1);
    }

    public GSphere(Point3d p1, Point3d p2, Point3d p3) {
        Vector3d a = new Vector3d();
        a.sub(p2, p1);
        Vector3d b = new Vector3d();
        b.sub(p3, p1);
        Vector3d ab = new Vector3d();
        ab.cross(a, b);
        double denominator = 2 * ab.dot(ab);
        Vector3d aba = new Vector3d();
        aba.cross(ab, a);
        Vector3d bab = new Vector3d();
        bab.cross(b, ab);
        aba.scale(b.dot(b));
        bab.scale(a.dot(a));
        Vector3d o = new Vector3d();
        o.add(aba, bab);
        o.scale(1 / denominator);
        center = new Point3d();
        center.add(p1, o);
        radius = center.distance(p1);
    }

    public GSphere(Point3d p1, Point3d p2, Point3d p3, Point3d p4) {
        center = new Point3d();
        radius = 1000;
    }

    public GSphere(final List<Point3d> points) {
        center = new Point3d();
        GSphere sphere = seb(points, 0, points.size(), 0);
        center.set(sphere.center);
        radius = sphere.radius;
    }

    private GSphere seb(List<Point3d> points, int inStartIndex,
            int inPointCount, int onPointCount) {
        GSphere sphere = new GSphere();
        switch (onPointCount) {
        case 1:
            sphere = new GSphere(points.get(inStartIndex - 1));
            break;
        case 2:
            sphere = new GSphere(points.get(inStartIndex - 1),
                    points.get(inStartIndex - 2));
            break;
        case 3:
            sphere = new GSphere(points.get(inStartIndex - 1),
                    points.get(inStartIndex - 2), points.get(inStartIndex - 3));
            break;
        case 4:
            sphere = new GSphere(points.get(inStartIndex - 1),
                    points.get(inStartIndex - 2), points.get(inStartIndex - 3),
                    points.get(inStartIndex - 4));
            return sphere;
        }
        for (int i = 0; i < inPointCount; i++) {
            if (sphere.distance(points.get(inStartIndex + i)) > 0) {
                Point3d p = points.remove(inStartIndex + i);
                points.add(inStartIndex, p);
                sphere = seb(points, inStartIndex + 1, i, onPointCount + 1);
            }
        }
        return sphere;
    }

    public double distance(Point3d p) {
        double d = center.distance(p);
        return d > radius ? d - radius : 0;
    }

    public Point3d getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
