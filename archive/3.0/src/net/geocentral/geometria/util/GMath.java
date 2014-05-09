/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.evaluator.GEvaluator;
import net.geocentral.geometria.model.GNotepadVariable;

public class GMath {

    public static final int NEGATIVE = 0;

    public static final int POSITIVE = 1;

    public static final int UNDEFINED = 2;

    public static final double EPSILON = 1e-7;

    public static Double evaluate(String expression) {
        return evaluate(expression, null);
    }

    public static Double evaluate(String expression,
            List<GNotepadVariable> variables) {
        GEvaluator evaluator = new GEvaluator(variables);
        double value;
        try {
            value = evaluator.evaluate(expression);
        }
        catch (Exception exception) {
            return null;
        }
        return value;
    }

    // Return true iff v1, v2 are cooriented
    public static boolean areCooriented(Vector3d v1, Vector3d v2,
            double epsilon) {
        double v1Length = v1.length();
        if (v1Length < epsilon)
            return true;
        Vector3d v = new Vector3d(v1);
        v.scale(v2.length() / v1Length);
        return v.epsilonEquals(v2, epsilon);
    }

    // Return true iff v1, v2 are collinear
    public static boolean areCollinear(Vector3d v1, Vector3d v2,
            double epsilon) {
        Vector3d v = new Vector3d();
        v.cross(v1, v2);
        return v.length() < epsilon;
    }

    // Return true iff ps are collinear
    public static boolean areCollinear(Point3d ps[], double epsilon) {
        for (int i = 0; i < ps.length - 2; i++) {
            if (!ps[i].epsilonEquals(ps[i + 1], epsilon)) {
                Vector3d v = new Vector3d(ps[i + 1]);
                v.sub(ps[i]);
                return areCollinear(ps, i + 1, v, epsilon);
            }
        }
        return true;
    }

    // Assume v has positive length. Return true iff ps starting at index,
    // v are collinear.
    private static boolean areCollinear(Point3d[] ps, int index, Vector3d v,
            double epsilon) {
        for (int i = index; i < ps.length - 1; i++) {
            Vector3d vi = new Vector3d(ps[i + 1]);
            vi.sub(ps[i]);
            if (!areCollinear(vi, v, epsilon))
                return false;
        }
        return true;
    }

    // Assume p11 != p22, p21 != p22, infinite lines p11-p12, p21-p22 are
    // coplanar.
    // Return the point of intersection, null if lines are collinear.
    public static Point3d intersect(Point3d p11, Point3d p12, Point3d p21,
            Point3d p22, double epsilon) {
        Vector3d v1 = new Vector3d();
        v1.sub(p12, p11);
        Vector3d v2 = new Vector3d();
        v2.sub(p22, p21);
        if (areCollinear(v1, v2, epsilon))
            return null;
        double t;
        double k = v1.x * v2.y - v1.y * v2.x;
        if (Math.abs(k) / v1.length() * v2.length() > EPSILON)
            t = (v2.y * (p21.x - p11.x) - v2.x * (p21.y - p11.y)) / k;
        else {
            k = v1.y * v2.z - v1.z * v2.y;
            if (Math.abs(k) / v1.length() * v2.length() > EPSILON)
                t = (v2.z * (p21.y - p11.y) - v2.y * (p21.z - p11.z)) / k;
            else
                t = (v2.x * (p21.z - p11.z) - v2.z * (p21.x - p11.x))
                        / (v1.z * v2.x - v1.x * v2.z);
        }
        return
        new Point3d(p11.x + v1.x * t, p11.y + v1.y * t, p11.z + v1.z * t);
    }

    // Cross product of vectors p1p2, p1p3
    public static Vector3d cross(Point3d p1, Point3d p2, Point3d p3) {
        Vector3d v1 = new Vector3d();
        v1.sub(p2, p1);
        Vector3d v2 = new Vector3d();
        v2.sub(p3, p1);
        Vector3d v = new Vector3d();
        v.cross(v1, v2);
        return v;
    }

    // Assume p1, p2 are distinct points.
    // Return projection of p onto infinite line p1p2.
    public static Point3d project(Point3d p, Point3d p1, Point3d p2) {
        Vector3d p1p = new Vector3d();
        p1p.sub(p, p1);
        Vector3d p1p2 = new Vector3d();
        p1p2.sub(p2, p1);
        double k = p1p.dot(p1p2) / p1p2.lengthSquared();
        Point3d projection = new Point3d();
        projection.add(p2);
        projection.sub(p1);
        projection.scale(k);
        projection.add(p1);
        return projection;
    }

    // Assume p1, p2, p are distinct collinear points.
    // Return simple ratio p1p / pp2
    public static double simpleRatio(Point3d p1, Point3d p2, Point3d p) {
        Vector3d v1 = new Vector3d();
        v1.sub(p, p1);
        Vector3d v2 = new Vector3d();
        v2.sub(p2, p);
        double k = v1.length() / v2.length();
        return v1.dot(v2) > 0 ? k : -k;
    }

    // Assume p1, p2 are distinct points.
    // Return true iff p is between p1 and p2
    public static boolean isBetween(Point3d p, Point3d p1, Point3d p2,
            double epsilon) {
        return p.epsilonEquals(p1, epsilon)
        || p.epsilonEquals(p2, epsilon) || areCollinear(new Point3d[] {
                p, p1, p2 }, epsilon) && simpleRatio(p1, p2, p) > 0;
    }

    // Assume p1, p2 are distinct points.
    // Return true iff p is between p1 and p2, but not equal to any of them
    public static boolean isStrictlyBetween(Point3d p, Point3d p1, Point3d p2,
            double epsilon) {
        return areCollinear(new Point3d[] { p, p1, p2 }, epsilon)
                && simpleRatio(p1, p2, p) > 0;
    }

    // Assume p0, p1, p2 are non-collinear, k > 0.
    // Return vector that divides angle p1p0p2 in ratio k.
    public static Vector3d divideAngle(Point3d p0, Point3d p1, Point3d p2,
            double k) {
        Vector3d v1 = new Vector3d(p1);
        v1.sub(p0);
        v1.normalize();
        Vector3d v2 = new Vector3d(p2);
        v2.sub(p0);
        v2.normalize();
        double phi = Math.acos(v1.dot(v2));
        Vector3d v = new Vector3d(v2);
        v.sub(v1);
        v.scale(0.5 * (1 - Math.tan(0.5 * phi * (1 - k) / (1 + k))
                / Math.tan(phi / 2)));
        v.add(v1);
        return v;
    }

    // Assume p1, p2 are distinct points. Intersect line p1p2 with sphere (c, r)
    public static List<Point3d> intersectSphere(Point3d p1, Point3d p2,
            Point3d c, double r) {
        List<Point3d> ps = new ArrayList<Point3d>();
        List<Double> ts = new ArrayList<Double>();
        Vector3d v12 = new Vector3d(p2);
        v12.sub(p1);
        Vector3d v1c = new Vector3d(p1);
        v1c.sub(c);
        double k = v12.lengthSquared();
        double m = v12.dot(v1c);
        double n = v1c.lengthSquared() - r * r;
        double d = m * m - k * n;
        if (d / (m * m) < -EPSILON)
            return ps;
        else if (d / (m * m) < EPSILON) ts.add(-m / k);
        else {
            double srd = Math.sqrt(d);
            ts.add((-m - srd) / k);
            ts.add((-m + srd) / k);
        }
        for (double t : ts) {
            if (t < 1 + EPSILON && t > -EPSILON) {
                Point3d p = new Point3d(p1);
                Vector3d sv12 = new Vector3d(v12);
                sv12.scale(t);
                p.add(sv12);
                ps.add(p);
            }
        }
        return ps;
    }

    // Assume p1, p2, p3 are non-collinear. Return distance from p0 to plane
    // p1p2p3.
    public static double distanceToPlane(Point3d p0, Point3d p1, Point3d p2,
            Point3d p3) {
        Vector3d n = cross(p1, p2, p3);
        Vector3d v = new Vector3d(p0);
        v.sub(p1);
        double distance = Math.abs(v.dot(n)) / n.length();
        return distance;
    }

    // Assume p1, p2, p3 are non-collinear. Return area of triangle p1p2p3.
    public static double area(Point3d p1, Point3d p2, Point3d p3) {
        return 0.5 * cross(p1, p2, p3).length();
    }

    // Assume n has positive length, p1 and p2 are distinct points.
    // Return the intersection point of line p1p2 with the plane (p0, n).
    // Return null if p1p2 is collinear with the plane or p1p2
    // does not intersect the plane.
    public static Point3d intersectPlane(Point3d p1, Point3d p2, Point3d p0,
            Vector3d n) {
        Vector3d p1p2 = new Vector3d(p2);
        p1p2.sub(p1);
        double p1p2n = p1p2.dot(n);
        if (Math.abs(p1p2n / (p1p2.length() * n.length())) < EPSILON)
            return null;
        double t = (n.x * (p0.x - p1.x) + n.y * (p0.y - p1.y) + n.z
                * (p0.z - p1.z)) / p1p2n;
        if (t < 1 + EPSILON && t > -EPSILON) {
            p1p2.scale(t);
            Point3d p = new Point3d(p1);
            p.add(p1p2);
            return p;
        }
        return null;
    }

    // Assume n has positive length. Return true iff p is in plane (p0, n).
    public static boolean isInPlane(Point3d p, Point3d p0, Vector3d n) {
        Vector3d p0p = new Vector3d(p);
        p0p.sub(p0);
        if (p0p.length() / n.length() < EPSILON)
            return true;
        return Math.abs(p0p.dot(n) / (p0p.length() * n.length())) < EPSILON;
    }

    // Assume v, n are orthogonal and have positive lengths, 0 < angle < pi.
    // Return the two unit vectors that are orthogonal to n and make angle
    // 'angle'
    // with v.
    public static Vector3d[] layAngle(Vector3d v, Vector3d n, double angle) {
        double lv = v.length();
        double k = lv * Math.cos(angle);
        Vector3d[] u = new Vector3d[2];
        double d = v.x * n.y - v.y * n.x;
        if (Math.abs(d / (lv * n.length())) > EPSILON)
            u = solve(v, n, k);
        else {
            d = v.y * n.z - v.z * n.y;
            if (Math.abs(d / (lv * n.length())) > EPSILON) {
                Vector3d vyzx = new Vector3d(v.y, v.z, v.x);
                Vector3d nyzx = new Vector3d(n.y, n.z, n.x);
                u = solve(vyzx, nyzx, k);
                u[0] = new Vector3d(u[0].z, u[0].x, u[0].y);
                u[1] = new Vector3d(u[1].z, u[1].x, u[1].y);
            }
            else {
                d = v.x * n.z - v.z * n.x;
                Vector3d vxzy = new Vector3d(v.x, v.z, v.y);
                Vector3d nxzy = new Vector3d(n.x, n.z, n.y);
                u = solve(vxzy, nxzy, k);
                u[0] = new Vector3d(u[0].x, u[0].z, u[0].y);
                u[1] = new Vector3d(u[1].x, u[1].z, u[1].y);
            }
        }
        return u;
    }

    // The two solutions (presumingly real and different) of system
    // u.dot(n) = 0
    // u.dot(v) = k
    // length(u) = 1
    // where v.x * n.y - v.y * n.x != 0
    private static Vector3d[] solve(Vector3d v, Vector3d n, double k) {
        double d = v.x * n.y - v.y * n.x;
        double r1 = v.z * n.y - v.y * n.z;
        double r2 = v.z * n.x - v.x * n.z;
        double a = r1 * r1 + r2 * r2 + d * d;
        double b = r1 * k * n.y + r2 * k * n.x;
        double c = k * k * (n.x * n.x + n.y * n.y) - d * d;
        double m = Math.sqrt(b * b - a * c);
        double[] z = { (b + m) / a, (b - m) / a };
        Vector3d[] u = new Vector3d[2];
        for (int i = 0; i < 2; i++) {
            double x = (k * n.y - z[i] * r1) / d;
            double y = (-k * n.x + z[i] * r2) / d;
            u[i] = new Vector3d(x, y, z[i]);
        }
        return u;
    }

    // Assume n has a positive length.
    // Scale p about plane (p0, n) with factor 'factor'.
    public static Point3d scale(Point3d p, Point3d p0, Vector3d n,
            double factor) {
        double pn = p.x * n.x + p.y * n.y + p.z * n.z;
        double n2 = n.lengthSquared();
        Vector3d sn = new Vector3d(n);
        sn.scale(pn / n2 * (factor - 1));
        Point3d sp = new Point3d(p);
        sp.add(sn);
        return sp;
    }

    // Assume v1, v2 are non-collinear. Shear p->s along (p, v1)
    public static Point3d shear(Point3d p, Point3d p0, Vector3d v1,
            Vector3d v2) {
        Vector3d v3 = new Vector3d(v1);
        v3.scale(-v1.dot(v2) / v1.lengthSquared());
        v3.add(v2);
        Vector3d r = new Vector3d(p);
        r.sub(p0);
        double m = r.dot(v3) / v3.lengthSquared();
        Vector3d w = new Vector3d(v2);
        w.sub(v3);
        w.scale(m);
        Point3d s = new Point3d(p);
        s.add(w);
        return s;
    }

    // Assume v1, v2 are non-collinear. Undo shear p->s along (p, v1)
    public static Point3d undoShear(Point3d p, Point3d p0, Vector3d v1,
            Vector3d v2) {
        Vector3d v3 = new Vector3d(v1);
        v3.scale(-v1.dot(v2) / v1.lengthSquared());
        v3.add(v2);
        Vector3d v4 = new Vector3d(v3);
        v4.scale(2);
        v4.sub(v2);
        Vector3d v5 = new Vector3d(v1);
        v5.scale(-1);
        return shear(p, p0, v5, v4);
    }

    public static List<Integer> matchCircular(List<Double> list1,
            List<Double> list2, boolean flipList2, double epsilon) {
        List<Integer> matchIndexes = new ArrayList<Integer>();
        int size = list1.size();
        if (list2.size() != size)
            return matchIndexes;
        for (int i1 = 0; i1 < size; i1++) {
            boolean goodI1 = true;
            for (int i2 = 0; i2 < size; i2++) {
                boolean match;
                if (!flipList2)
                    match = Math.abs(list2.get(i2) - list1.get((i1 + i2)
                                    % size)) < epsilon;
                else
                    match = Math.abs(list2.get((size - i2 - 1) % size)
                            - list1.get((i1 + i2) % size)) < epsilon;
                if (!match) {
                    goodI1 = false;
                    break;
                }
            }
            if (goodI1)
                matchIndexes.add(i1);
        }
        return matchIndexes;
    }

    public static void orthize(Vector3d v1, Vector3d v2) {
        v1.normalize();
        Vector3d v = new Vector3d(v1);
        v.scale(v1.dot(v2));
        v.sub(v2);
        v.scale(-1);
        v2.set(v);
        v2.normalize();
    }

    public static int getOrientation(Point3d p1, Point3d p2, Point3d p3,
            Point3d p4) {
        Vector3d v3 = new Vector3d(p4);
        v3.sub(p1);
        return getOrientation(p1, p2, p3, v3);
    }

    public static int getOrientation(Point3d p1, Point3d p2, Point3d p3, Vector3d v) {
        Vector3d v1 = new Vector3d(p2);
        v1.sub(p1);
        Vector3d v2 = new Vector3d(p3);
        v2.sub(p1);
        Vector3d v12 = new Vector3d();
        v12.cross(v1, v2);
        v12.normalize();
        v.normalize();
        double signedVolume = v12.dot(v);
        if (signedVolume > EPSILON) {
            return POSITIVE;
        }
        if (signedVolume < -EPSILON) {
            return NEGATIVE;
        }
        return UNDEFINED;
    }
}
