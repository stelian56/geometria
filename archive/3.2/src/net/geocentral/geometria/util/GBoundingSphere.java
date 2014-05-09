/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.Collection;
import java.util.List;

import javax.vecmath.Point3d;

//Smallest bounding sphere for a given collection of points
public class GBoundingSphere implements Cloneable {

    private Point3d center;

    private double radius;

    public GBoundingSphere() {}

    // The smallest sphere that contains the given collection of points.
    // Center is approximate, away from theoretical by no more than epsilon.
    // http://www.inb.uni-luebeck.de/publications/pdfs/MaMa04a.pdf, equation
    // (1).
    public GBoundingSphere(List<Point3d> points, double epsilon) {
        int numIterations = (int)(1.0 / epsilon);
        Point3d c = new Point3d(0, 0, 0);
        Point3d x;
        int i;
        for (i = 0; i < numIterations; i++) {
            x = furthestPoint(points, c);
            x.sub(c);
            x.scale(1.0 / (1 + i));
            x.add(c);
            c = new Point3d(x);
        }
        center = c;
        radius = furthestPoint(points, c).distance(c);
    }

    public GBoundingSphere clone() {
        GBoundingSphere bs = new GBoundingSphere();
        bs.radius = radius;
        bs.center = new Point3d(center);
        return bs;
    }

    // Return p such that p in points and dist(p, c) -> max
    private Point3d furthestPoint(final Collection<Point3d> points,
            final Point3d c) {
        double maxDistance = 0;
        Point3d fp = null;
        for (Point3d p : points) {
            double distance = c.distance(p);
            if (distance > maxDistance) {
                maxDistance = distance;
                fp = p;
            }
        }
        return new Point3d(fp);
    }

    public Point3d getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
