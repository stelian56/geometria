/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPainter;

public class GFace implements Cloneable, GSelectable {

    private int sideCount;

    private List<GLine> lines;

    public GFace() {
        lines = new ArrayList<GLine>();
    }

    public GFace(int sideCount, List<GLine> lines) {
        this.sideCount = sideCount;
        this.lines = lines;
    }

    public GFace clone() {
        GFace face = new GFace();
        face.sideCount = sideCount;
        for (GLine line : lines) {
            GLine l = line.clone();
            face.lines.add(l);
        }
        return face;
    }

    public boolean contains(String label) {
        for (GLine line : lines) {
            if (line.contains(label))
                return true;
        }
        return false;
    }

    public String labelAt(int index) {
        return lines.get(index).firstLabel();
    }

    public boolean containsLine(GLine line) {
        return lines.contains(line);
    }

    public boolean containsSide(GLine line) {
        int index = lines.indexOf(line);
        return index < sideCount;
    }

    public int sideCount() {
        return sideCount;
    }

    public int lineCount() {
        return lines.size();
    }

    public GLine lineAt(int index) {
        return lines.get(index);
    }

    public void paintOpaque(Graphics2D g2d, Color baseColor, GCamera camera,
            Set<GSelectable> selection, GSolid solid, Point3d refPoint) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < sideCount; i++) {
            Point p = solid.getPoint(labelAt(i)).scrCoords;
            polygon.addPoint(p.x, p.y);
        }
        if (selection.contains(this)) {
            g2d.setColor(GSolid.SELECTION_COLOR);
            g2d.fill(polygon);
        }
        else {
            Vector3d on = getNormal(solid, refPoint);
            camera.getAttitude().transform(on);
            Color color = GPainter.getInstance().getHue(baseColor, on.z);
            g2d.setColor(color);
            g2d.fill(polygon);
        }
        for (int i = 0; i < lines.size(); i++) {
            GLine line = lines.get(i);
            Point p1 = solid.getPoint(line.firstLabel()).scrCoords;
            Point p2 = solid.getPoint(line.lastLabel()).scrCoords;
            GStick stick = new GStick(line);
            if (selection.contains(stick)) {
                g2d.setColor(GSolid.SELECTION_COLOR);
                g2d.setStroke(GSolid.SELECTION_STROKE);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            else if (i >= sideCount) {
                g2d.setColor(GSolid.OPAQUE_LINE_COLOR);
                g2d.setStroke(GSolid.SOLID_STROKE);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            for (int j = 0; j < line.labelCount(); j++) {
                GPoint3d p = solid.getPoint(line.labelAt(j));
                if (selection.contains(p)) {
                    g2d.setColor(GSolid.SELECTION_COLOR);
                    g2d.fillOval(p.scrCoords.x
                            - GSolid.SELECTED_POINT_DIAMETER / 2, p.scrCoords.y
                            - GSolid.SELECTED_POINT_DIAMETER / 2,
                            GSolid.SELECTED_POINT_DIAMETER,
                            GSolid.SELECTED_POINT_DIAMETER);
                }
            }
        }
    }

    public Vector3d getNormal(GSolid solid) {
        Vector3d normal = new Vector3d();
        Point3d p1 = solid.getPoint(labelAt(0)).coords;
        Point3d p2 = solid.getPoint(labelAt(1)).coords;
        Point3d p3 = solid.getPoint(labelAt(2)).coords;
        Vector3d edge1 = new Vector3d();
        edge1.sub(p2, p1);
        Vector3d edge2 = new Vector3d();
        edge2.sub(p3, p2);
        normal.cross(edge1, edge2);
        normal.normalize();
        return normal;
    }

    public int getOrientation(GSolid solid, Point3d refPoint) {
        Point3d p0 = solid.getPoint(labelAt(0)).coords;
        Point3d p1 = solid.getPoint(labelAt(1)).coords;
        Point3d p2 = solid.getPoint(labelAt(2)).coords;
        return GMath.getOrientation(p0, p1, p2, refPoint);
    }

    public Vector3d getNormal(GSolid solid, Point3d refPoint) {
        Vector3d normal = getNormal(solid);
        Point3d p1 = solid.getPoint(labelAt(0)).coords;
        Vector3d toOutside = new Vector3d();
        toOutside.sub(p1, refPoint);
        if (normal.dot(toOutside) < 0)
            normal.scale(-1);
        return normal;
    }

    public boolean covers(Point3d p, GSolid solid) {
        double epsilon = solid.getEpsilon();
        // Check if p belongs to the perimeter
        for (int i = 0; i < sideCount; i++) {
            GLine line = lines.get(i);
            if (GMath.isBetween(p, solid.getPoint(line.firstLabel()).coords,
                    solid.getPoint(line.lastLabel()).coords, epsilon))
                return true;
        }
        // Check if p belongs to the interior of this face
        Vector3d v1 = GMath.cross(p, solid.getPoint(labelAt(0)).coords, solid
                .getPoint(labelAt(1)).coords);
        v1.normalize();
        for (int i = 1; i < sideCount; i++) {
            GLine line = lines.get(i);
            Vector3d vi = GMath.cross(p, solid.getPoint(line.firstLabel())
                    .coords, solid.getPoint(line.lastLabel()).coords);
            vi.normalize();
            if (vi.dot(v1) < -GMath.EPSILON)
                return false;
        }
        return true;
    }

    public GLine lineThroughPoints(String label1, String label2) {
        for (GLine line : lines)
            if (line.contains(label1) && line.contains(label2))
                return line;
        return null;
    }

    public List<GLine> linesThroughPoint(String label) {
        List<GLine> ls = new ArrayList<GLine>();
        for (GLine line : lines)
            if (line.contains(label))
                ls.add(line);
        return ls;
    }

    public GLine getLineAt(double xp, double yp, double selectTolerance,
            Map<String, GPoint3d> pointMap) {
        for (GLine line : lines) {
            GPoint3d p1 = pointMap.get(line.firstLabel());
            GPoint3d p2 = pointMap.get(line.lastLabel());
            double dist = Line2D.ptSegDist(p1.projCoords.x, p1.projCoords.y,
                    p2.projCoords.x, p2.projCoords.y, xp, yp);
            if (dist < selectTolerance)
                return line;
        }
        return null;
    }

    public Object getElementAt(double xp, double yp, double tolerance,
            Map<String, GPoint3d> pointMap) {
        GLine line = getLineAt(xp, yp, tolerance, pointMap);
        if (line != null) {
            GPoint3d p1 = pointMap.get(line.firstLabel());
            GPoint3d p2 = pointMap.get(line.lastLabel());
            if (Point2D.distance(p1.projCoords.x, p1.projCoords.y, xp, yp) <
                    tolerance)
                return p1;
            if (Point2D.distance(p2.projCoords.x, p2.projCoords.y, xp, yp) <
                    tolerance)
                return p2;
            return line;
        }
        Iterator<GLine> it = lines.iterator();
        Double firstDet = null;
        for (int i = 0; i < sideCount; i++) {
            line = it.next();
            Point2d p1 = pointMap.get(line.firstLabel()).projCoords;
            Point2d p2 = pointMap.get(line.lastLabel()).projCoords;
            double det = (xp - p1.x) * (yp - p2.y) - (xp - p2.x) * (yp - p1.y);
            if (firstDet == null)
                firstDet = det;
            else if (firstDet * det <= 0)
                return null;
        }
        return this;
    }

    public boolean addPoint(GPoint3d p, GSolid solid) {
        boolean added = false;
        for (GLine line : lines)
            added = added || line.acquire(p, solid);
        return added;
    }

    public void removePoint(GPoint3d p) {
        for (GLine line : lines)
            line.remove(p.getLabel());
    }

    public GLine addLine(GPoint3d p1, GPoint3d p2, List<GLine> removedLines,
            GSolid solid) {
        for (int i = 0; i < sideCount; i++) {
            GLine l = lines.get(i);
            if (l.contains(p1.getLabel()) && l.contains(p2.getLabel()))
                return null;
        }
        GLine line = new GLine(p1.getLabel(), p2.getLabel());
        for (int i = sideCount; i < lines.size(); i++) {
            GLine l = lines.get(i);
            if (line.acquire(l, solid))
                removedLines.add(l);
        }
        for (GLine l : removedLines)
            lines.remove(l);
        for (int i = sideCount; i < lines.size(); i++) {
            GLine l = lines.get(i);
            for (int j = 0; j < l.labelCount(); j++) {
                GPoint3d p = solid.getPoint(l.labelAt(j));
                line.acquire(p, solid);
            }
        }
        lines.add(line);
        return line;
    }

    public void undoAddLine(GLine addedLine, List<GLine> removedLines) {
        if (addedLine != null)
            lines.remove(addedLine);
        for (GLine line : removedLines)
            lines.add(line);
    }

    public void undoAddLines(List<GLine> addedLines, List<GLine> removedLines) {
        for (GLine line : addedLines)
            lines.remove(line);
        for (GLine line : removedLines)
            lines.add(line);
    }

    public void addLine(GLine line) {
        lines.add(line);
    }

    public GLine removeLine(GPoint3d p1, GPoint3d p2, List<GLine> addedLines,
            List<String> removedLabels) {
        String label1 = p1.getLabel();
        String label2 = p2.getLabel();
        GLine line = lineThroughPoints(label1, label2);
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < line.labelCount(); i++) {
            String label = line.labelAt(i);
            if (label.equals(label1) || label.equals(label2))
                indices.add(i);
            if (indices.size() == 2)
                break;
        }
        if (indices.get(0) > 0) {
            GLine l = new GLine(line, 0, indices.get(0));
            addedLines.add(l);
            lines.add(l);
        }
        if (indices.get(1) < line.labelCount() - 1) {
            GLine l = new GLine(line, indices.get(1), line.labelCount() - 1);
            addedLines.add(l);
            lines.add(l);
        }
        for (int i = indices.get(0); i <= indices.get(1); i++) {
            String label = line.labelAt(i);
            if (linesThroughPoint(label).size() < 2)
                removedLabels.add(label);
        }
        lines.remove(line);
        return line;
    }

    public void undoRemoveLine(GLine removedLine, List<GLine> addedLines) {
        lines.add(removedLine);
        for (GLine line : addedLines)
            lines.remove(line);
    }

    public void removeLine(GLine line, List<String> danglingLabels) {
        for (int i = 0; i < line.labelCount(); i++) {
            String label = line.labelAt(i);
            if (linesThroughPoint(label).size() < 2)
                danglingLabels.add(label);
        }
        lines.remove(line);
    }

    // Assume v has positive length, p is covered by this face and (p, v) is
    // looking
    // to the interior of this face. Return:
    // 1. The line intersected by ray (p, v), any of them if there are two.
    // 2. The intersection point.
    public Object[] intersectRay(Point3d p, Vector3d v, GSolid solid) {
        double epsilon = solid.getEpsilon();
        Point3d pv = new Point3d(p);
        pv.add(v);
        for (int i = 0; i < sideCount; i++) {
            GLine line = lineAt(i);
            Point3d p1 = solid.getPoint(line.firstLabel()).coords;
            Point3d p2 = solid.getPoint(line.lastLabel()).coords;
            Point3d p3 = GMath.intersect(p, pv, p1, p2, epsilon);
            if (p3 != null && GMath.isBetween(p3, p1, p2, epsilon)) {
                Vector3d v3 = new Vector3d(p3);
                v3.sub(p);
                if (v3.length() > epsilon
                        && GMath.areCooriented(v3, v, epsilon))
                    return new Object[] {
                        line, p3 };
            }
        }
        return null;
    }

    // Assume v has positive length. Return the
    // points of intersection of this face with plane (p, v). If an edge lies
    // in the plane, return its ends.
    public List<Point3d> intersectPlane(Point3d p, Vector3d n, GSolid solid) {
        List<Point3d> ps = new ArrayList<Point3d>();
        for (int i = 0; i < sideCount; i++) {
            GLine line = lineAt(i);
            Point3d p1 = solid.getPoint(line.firstLabel()).coords;
            Point3d p2 = solid.getPoint(line.lastLabel()).coords;
            boolean p1InPlane = GMath.isInPlane(p1, p, n);
            boolean p2InPlane = GMath.isInPlane(p2, p, n);
            if (!p1InPlane && !p2InPlane) {
                Point3d p3 = GMath.intersectPlane(p1, p2, p, n);
                if (p3 != null)
                    ps.add(p3);
            }
            else if (p1InPlane && !ps.contains(p1))
                ps.add(p1);
            else if (p2InPlane && !ps.contains(p2))
                ps.add(p2);
            if (ps.size() == 2)
                return ps;
        }
        return ps;
    }

    public boolean liesInPlane(Point3d p0, Vector3d n, GSolid solid) {
        Point3d p1 = solid.getPoint(lineAt(0).firstLabel()).coords;
        Point3d p2 = solid.getPoint(lineAt(0).lastLabel()).coords;
        Point3d p3 = solid.getPoint(lineAt(1).lastLabel()).coords;
        return GMath.isInPlane(p1, p0, n)
        && GMath.isInPlane(p2, p0, n) && GMath.isInPlane(p3, p0, n);
    }

    public Point3d computeGCenter(GSolid solid) {
        Point3d gCenter = new Point3d();
        for (int i = 0; i < sideCount; i++) {
            GLine line = lines.get(i);
            gCenter.add(solid.getPoint(line.firstLabel()).coords);
        }
        gCenter.scale(1.0 / sideCount);
        return gCenter;
    }

    public double computeArea(GSolid solid) {
        double area = 0;
        Point3d gCenter = computeGCenter(solid);
        for (int i = 0; i < sideCount; i++) {
            GLine line = lines.get(i);
            Point3d p1 = solid.getPoint(line.firstLabel()).coords;
            Point3d p2 = solid.getPoint(line.lastLabel()).coords;
            area += GMath.area(p1, p2, gCenter);
        }
        return area;
    }

    private List<Double> getSideLengths(GSolid solid) {
        List<Double> sideLengths = new ArrayList<Double>();
        for (int i = 0; i < sideCount; i++) {
            GLine line = lineAt(i);
            sideLengths.add(line.length(solid));
        }
        return sideLengths;
    }

    private List<Double> getCosines(GSolid solid) {
        List<Double> cosines = new ArrayList<Double>();
        for (int i = 0; i < sideCount; i++) {
            GLine line1 = lineAt(i);
            GLine line2 = lineAt((sideCount + i - 1) % sideCount);
            Vector3d v1 =
                new Vector3d(solid.getPoint(line1.firstLabel()).coords);
            v1.sub(solid.getPoint(line1.lastLabel()).coords);
            Vector3d v2 =
                new Vector3d(solid.getPoint(line2.lastLabel()).coords);
            v2.sub(solid.getPoint(line2.firstLabel()).coords);
            cosines.add(v1.dot(v2) / (v1.length() * v2.length()));
        }
        return cosines;
    }

    // Return an empty list if face1 is not similar to this face.
    // Otherwise, return vertex indexes in this face starting at which
    // this face can be superposed, after a suitable scaling, on face1.
    public List<Integer> match(GFace face1, boolean flipFace1, GSolid solid,
            GSolid solid1) {
        List<Double> sideLengths = getSideLengths(solid);
        List<Double> sideLengths1 = face1.getSideLengths(solid1);

        double min = Collections.min(sideLengths);
        double min1 = Collections.min(sideLengths1);
        double scaleFactor = min / min1;
        for (int i = 0; i < sideLengths1.size(); i++)
            sideLengths1.set(i, sideLengths1.get(i) * scaleFactor);
        double epsilon = min * GMath.EPSILON;

        double max = Collections.max(sideLengths);
        double max1 = Collections.max(sideLengths1);
        if (Math.abs(max1 - max) > epsilon)
            return new ArrayList<Integer>();

        List<Integer> matchIndexes = GMath.matchCircular(sideLengths,
                sideLengths1, flipFace1, epsilon);
        if (matchIndexes.isEmpty())
            return matchIndexes;

        List<Double> cosines = getCosines(solid);
        List<Double> cosines1 = face1.getCosines(solid1);
        List<Integer> badIndexes = new ArrayList<Integer>();
        for (int index : matchIndexes) {
            boolean indexIsBad = false;
            for (int i = 0; i < sideCount; i++) {
                boolean match;
                if (!flipFace1)
                    match = Math.abs(cosines1.get(i) - cosines.get((index + i)
                                    % sideCount)) < GMath.EPSILON;
                else
                    match = Math.abs(cosines1.get((sideCount - i) % sideCount)
                        - cosines.get((index + i) % sideCount)) < GMath.EPSILON;
                if (!match) {
                    indexIsBad = true;
                    break;
                }
            }
            if (indexIsBad)
                badIndexes.add(index);
        }
        matchIndexes.removeAll(badIndexes);
        return matchIndexes;
    }

    public int indexOf(GLine line) {
        return lines.indexOf(line);
    }

    // Assume 'line' lies inside this face.
    // Cut off this face the part boundered by 'line' and pointed to by 'n'
    public void cutOff(GLine line, Vector3d n, Set<GPoint3d> toBeRemovedPoints,
            GSolid solid) {
        double epsilon = solid.getEpsilon();
        Point3d coordsLine1 = solid.getPoint(line.firstLabel()).coords;
        Point3d coordsLine2 = solid.getPoint(line.lastLabel()).coords;
        List<GLine> ls = new ArrayList<GLine>();
        for (int i = 0; i < sideCount; i++) {
            GLine l = lineAt(i);
            Point3d coordsL1 = solid.getPoint(l.firstLabel()).coords;
            Point3d coordsL2 = solid.getPoint(l.lastLabel()).coords;
            Point3d coords = GMath.intersect(coordsL1, coordsL2, coordsLine1,
                    coordsLine2, epsilon);
            int pIndex = -1;
            if (coords != null) {
                if (l.firstLabel().equals(line.firstLabel())
                        || l.firstLabel().equals(line.lastLabel()))
                    pIndex = 0;
                else if (l.lastLabel().equals(line.firstLabel())
                        || l.lastLabel() == line.lastLabel())
                    pIndex = l.labelCount() - 1;
                else {
                    if (GMath.isBetween(coords, coordsL1, coordsL2, epsilon)) {
                        for (int j = 0; j < l.labelCount(); j++) {
                            if (solid.getPoint(l.labelAt(j)).coords
                                    .epsilonEquals(coords, epsilon)) {
                                pIndex = j;
                                break;
                            }
                        }
                    }
                }
            }
            if (pIndex < 0) {
                Vector3d v = new Vector3d(coordsL1);
                v.sub(coordsLine1);
                if (v.dot(n) > 0) {
                    for (int j = 0; j < l.labelCount() - 1; j++)
                        toBeRemovedPoints.add(solid.getPoint(l.labelAt(j)));
                }
                else {
                    GLine ll = l.clone();
                    ls.add(ll);
                }
            }
            else {
                Vector3d v = new Vector3d(coordsL2);
                v.sub(coordsL1);
                if (v.dot(n) > 0) {
                    for (int j = pIndex + 1; j < l.labelCount(); j++)
                        toBeRemovedPoints.add(solid.getPoint(l.labelAt(j)));
                    if (pIndex > 0) {
                        GLine ll = new GLine(l, 0, pIndex);
                        ls.add(ll);
                    }
                    if (pIndex > 0)
                        ls.add(line);
                }
                else {
                    for (int j = 0; j < pIndex; j++)
                        toBeRemovedPoints.add(solid.getPoint(l.labelAt(j)));
                    if (pIndex < l.labelCount() - 1) {
                        GLine ll = new GLine(l, pIndex, l.labelCount() - 1);
                        ls.add(ll);
                    }
                }
            }
        }
        int sc = ls.size();

        // Intersect 'line' with interior lines
        for (int i = sideCount; i < lineCount(); i++) {
            GLine l = lineAt(i);
            if (l == line)
                continue;
            Point3d coordsL1 = solid.getPoint(l.firstLabel()).coords;
            Point3d coordsL2 = solid.getPoint(l.lastLabel()).coords;
            Point3d coords = GMath.intersect(coordsLine1, coordsLine2,
                    coordsL1, coordsL2, epsilon);
            if (coords == null
                    || !GMath.isStrictlyBetween(coords, coordsLine1,
                            coordsLine2, epsilon))
                continue;
            if (!GMath.isBetween(coords, coordsL1, coordsL2, epsilon))
                continue;
            GPoint3d p = solid.getPoint(coords);
            if (p == null)
                p = solid.addPoint(coords);
            if (!line.contains(p.getLabel()))
                line.insert(p, solid);
            if (!l.contains(p.getLabel()))
                l.insert(p, solid);
        }

        // Clip interior lines
        for (int i = sideCount; i < lineCount(); i++) {
            GLine l = lineAt(i);
            if (l == line)
                continue;
            Point3d coordsL1 = solid.getPoint(l.firstLabel()).coords;
            Point3d coordsL2 = solid.getPoint(l.lastLabel()).coords;
            int indexLine = -1;
            int indexL = -1;
            for (int j = 0; j < l.labelCount(); j++) {
                indexLine = line.indexOf(l.labelAt(j));
                if (indexLine >= 0) {
                    indexL = j;
                    break;
                }
            }
            if (indexL >= 0) {
                Vector3d v = new Vector3d(coordsL2);
                v.sub(coordsL1);
                if (v.dot(n) > 0) {
                    for (int j = indexL + 1; j < l.labelCount(); j++) {
                        GPoint3d p = solid.getPoint(l.labelAt(j));
                        toBeRemovedPoints.add(p);
                    }
                    if (indexL > 0) {
                        GLine ll = new GLine(l, 0, indexL);
                        ls.add(ll);
                    }
                }
                else {
                    for (int j = 0; j < indexL; j++) {
                        GPoint3d p = solid.getPoint(l.labelAt(j));
                        toBeRemovedPoints.add(p);
                    }
                    if (indexL < l.labelCount() - 1) {
                        GLine ll = new GLine(l, indexL, l.labelCount() - 1);
                        ls.add(ll);
                    }
                }
            }
            else {
                Vector3d v = new Vector3d(coordsL1);
                v.sub(coordsLine1);
                if (v.dot(n) > 0) {
                    for (int j = 0; j < l.labelCount(); j++) {
                        GPoint3d p = solid.getPoint(l.labelAt(j));
                        toBeRemovedPoints.add(p);
                    }
                }
                else {
                    GLine ll = l.clone();
                    ls.add(ll);
                }
            }
        }

        lines = ls;
        sideCount = sc;
        chainSides();
    }

    public void chainSides() {
        List<GLine> ls = new ArrayList<GLine>();
        GLine line = lineAt(0);
        GLine nextLine = lineAt(1);
        if (!line.lastLabel().equals(nextLine.firstLabel())
                && !line.lastLabel().equals(nextLine.lastLabel()))
            line.reverse();
        ls.add(line);
        for (int i = 1; i < sideCount; i++) {
            nextLine = lineAt(i);
            if (!nextLine.firstLabel().equals(line.lastLabel()))
                nextLine.reverse();
            ls.add(nextLine);
            line = nextLine;
        }
        for (int i = sideCount; i < lineCount(); i++) {
            line = lineAt(i);
            ls.add(line);
        }
        lines = ls;
    }

    public void reverse() {
        List<GLine> ls = new ArrayList<GLine>();
        for (int i = sideCount - 1; i >= 0; i--) {
            GLine line = lineAt(i);
            line.reverse();
            ls.add(line);
        }
        for (int i = sideCount; i < lineCount(); i++) {
            GLine line = lineAt(i);
            ls.add(line);
        }
        lines = ls;
    }

    public void anchorAt(int index) {
        List<GLine> ls = new ArrayList<GLine>();
        for (int i = 0; i < sideCount; i++) {
            GLine line = lineAt((i + index) % sideCount);
            ls.add(line);
        }
        for (int i = sideCount; i < lineCount(); i++) {
            GLine line = lineAt(i);
            ls.add(line);
        }
        lines = ls;
    }

    public void addExternalPoint(GPoint3d p, GSolid solid) {
        Vector3d n = getNormal(solid);
        List<GLine> front = new ArrayList<GLine>();
        List<GLine> rear = new ArrayList<GLine>();
        List<GLine> rim = new ArrayList<GLine>();
        double epsilon = solid.getEpsilon();
        for (GLine line : lines) {
            GPoint3d p1 = solid.getPoint(line.firstLabel());
            GPoint3d p2 = solid.getPoint(line.lastLabel());
            if (GMath.areCollinear(new Point3d[] {p.coords, p1.coords, p2.coords}, epsilon)) {
                rim.add(line);
            }
            else {
                int orientation = GMath.getOrientation(p.coords, p1.coords, p2.coords, n);
                if (orientation == GMath.NEGATIVE)
                    front.add(line);
                else if (orientation == GMath.POSITIVE)
                    rear.add(line);
            }
        }
        int index = 0;
        for (int i = 1; i < rear.size(); i++) {
            GLine line = rear.get(i - 1);
            GLine l = rear.get(i);
            if (!line.lastLabel().equals(l.firstLabel())) {
                index = i;
                break;
            }
        }
        List<GLine> ls = new ArrayList<GLine>();
        for (int i = 0; i < rear.size(); i++) {
            GLine line = rear.get((index + i) % rear.size());
            ls.add(line);
        }
        GLine line1 = new GLine(p.getLabel(), ls.get(0).firstLabel());
        GLine line2 =
            new GLine(ls.get(ls.size() - 1).lastLabel(), p.getLabel());
        ls.add(0, line1);
        ls.add(line2);
        lines = ls;
        sideCount = ls.size();
    }


    public void pointRenamed(String oldLabel, String newLabel) {
        for (GLine line : lines) {
            line.pointRenamed(oldLabel, newLabel);
        }
    }

    public boolean isSquare(GSolid solid) {
        return isRectangle(solid) && isRhombus(solid);
    }

    public boolean isRhombus(GSolid solid) {
        if (!isParallelogram(solid))
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        Vector3d v2 = lineAt(1).toVector(solid);
        return Math.abs(v1.length() - v2.length()) < solid.getEpsilon();
    }

    public boolean isRectangle(GSolid solid) {
        if (!isParallelogram(solid))
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        v1.normalize();
        Vector3d v2 = lineAt(1).toVector(solid);
        v2.normalize();
        return Math.abs(v1.dot(v2)) < GMath.EPSILON;
    }

    public boolean isParallelogram(GSolid solid) {
        if (lineCount() != 4)
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        Vector3d v2 = lineAt(2).toVector(solid);
        v2.add(v1);
        return v2.length() < solid.getEpsilon();
    }

    public boolean isEquilateralTriangle(GSolid solid) {
        if (lineCount() != 3)
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        Vector3d v2 = lineAt(1).toVector(solid);
        Vector3d v3 = lineAt(2).toVector(solid);
        double epsilon = solid.getEpsilon();
        return Math.abs(v1.length() - v2.length()) < epsilon
                && Math.abs(v1.length() - v3.length()) < epsilon;
    }

    public boolean isIsoscellesTriangle(GSolid solid) {
        if (lineCount() != 3)
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        Vector3d v2 = lineAt(1).toVector(solid);
        Vector3d v3 = lineAt(2).toVector(solid);
        double epsilon = solid.getEpsilon();
        return Math.abs(v1.length() - v2.length()) < epsilon
            || Math.abs(v2.length() - v3.length()) < epsilon
                || Math.abs(v1.length() - v3.length()) < epsilon;
    }

    public boolean isRectangularTriangle(GSolid solid) {
        if (lineCount() != 3)
            return false;
        Vector3d v1 = lineAt(0).toVector(solid);
        v1.normalize();
        Vector3d v2 = lineAt(1).toVector(solid);
        v2.normalize();
        Vector3d v3 = lineAt(2).toVector(solid);
        v3.normalize();
        return Math.abs(v1.dot(v2)) < GMath.EPSILON
            || Math.abs(v2.dot(v3)) < GMath.EPSILON
            || Math.abs(v1.dot(v3)) < GMath.EPSILON;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        for (int i = 0; i < 3; i++)
            buf.append(labelAt(i));
        buf.append("]");
        return String.valueOf(buf);
    }
}
