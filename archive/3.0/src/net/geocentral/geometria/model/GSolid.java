/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.geocentral.geometria.util.GApplicationManager;
import net.geocentral.geometria.util.GBoundingSphere;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GSolid extends GXmlEntity implements Cloneable {

    public static final String SCHEMA = "/conf/solid.xsd";

    public static final double DEFAULT_SELECT_TOLERANCE = 5;

    public static final Color POINT_COLOR = Color.RED;

    public static final Color TRANSPARENT_LINE_COLOR = Color.BLACK;

    public static final Color OPAQUE_LINE_COLOR = Color.WHITE;

    public static final Color SELECTION_COLOR = Color.MAGENTA.darker();

    public static final int POINT_DIAMETER = 6;

    public static final int SELECTED_POINT_DIAMETER = 8;

    public static final Stroke SOLID_STROKE = new BasicStroke(1);

    public static final float[] DASH = { 12, 8 };

    public static final Stroke DASH_STROKE = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, DASH, 0);

    public static final Stroke SELECTION_STROKE = new BasicStroke(4);

    private Map<String, GPoint3d> points;

    private List<GFace> faces;

    private Map<GPoint3d, GStar> stars;

    private Point3d gCenter;

    private GBoundingSphere boundingSphere;

    private Set<GSelectable> selection;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSolid() {
        points = new LinkedHashMap<String, GPoint3d>();
        selection = new LinkedHashSet<GSelectable>();
    }

    public GSolid(Element node) {
        this();
        docElement = node;
    }

    public GSolid(List<GPoint3d> pointList, List<GFace> faces) {
        this();
        logger.info(pointList + ", " + faces);
        for (GPoint3d p : pointList)
            points.put(p.getLabel(), p);
        this.faces = faces;
        makeConfig();
        computeGCenter();
        computeBoundingSphere();
    }

    public GSolid clone() {
        logger.info("");
        GSolid solid = new GSolid();
        // Clone points
        solid.points = new LinkedHashMap<String, GPoint3d>();
        for (String label : points.keySet()) {
            GPoint3d p = points.get(label);
            GPoint3d clonedP = p.clone();
            solid.points.put(label, clonedP);
        }
        // Clone faces
        solid.faces = new ArrayList<GFace>();
        for (GFace face : faces) {
            GFace f = face.clone();
            solid.faces.add(f);
        }
        solid.gCenter = new Point3d(gCenter);
        solid.selection = new LinkedHashSet<GSelectable>();
        solid.boundingSphere = boundingSphere.clone();
        solid.makeConfig();
        return solid;
    }

    public void computeGCenter() {
        logger.info("");
        gCenter = new Point3d();
        int vertexCount = 0;
        for (GPoint3d p : points.values()) {
            if (p.isVertex()) {
                vertexCount++;
                gCenter.add(p.coords);
            }
        }
        gCenter.scale(1.0 / vertexCount);
    }

    public void computeBoundingSphere() {
        logger.info("");
        double epsilon = 1e-2;
        List<Point3d> ps = new ArrayList<Point3d>();
        for (GPoint3d p : points.values()) {
            if (p.isVertex())
                ps.add(p.coords);
        }
        boundingSphere = new GBoundingSphere(ps, epsilon);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        Map<String, GPoint3d> pointMap = new LinkedHashMap<String, GPoint3d>();
        makePoints(node, pointMap);
        List<GLine> lineList = new ArrayList<GLine>();
        makeLines(node, lineList);
        buildFaces(pointMap, lineList);
        computeGCenter();
        computeBoundingSphere();
    }

    private void makePoints(Element node, Map<String, GPoint3d> pointMap)
            throws Exception {
        logger.info("");
        Map<String, GPoint3d> pMap = new TreeMap<String, GPoint3d>();
        NodeList ns = ((Element)node.getElementsByTagName("points")
                .item(0)).getElementsByTagName("point");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            GPoint3d p = new GPoint3d();
            p.make(n);
            pMap.put(p.getLabel(), p);
        }
        for (String label : pMap.keySet()) {
            GPoint3d p = pMap.get(label);
            pointMap.put(label, p);
        }
    }

    private void makeLines(Element node, List<GLine> lineList)
            throws Exception {
        logger.info("");
        Set<GStick> sticks = new TreeSet<GStick>();
        NodeList ns = ((Element)node.getElementsByTagName("lines").item(0))
            .getElementsByTagName("line");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            GStick stick = new GStick();
            stick.make(n);
            sticks.add(stick);
        }
        for (GStick stick : sticks) {
            GLine line = new GLine(stick);
            lineList.add(line);
        }
    }

    public void makeConfig() {
        logger.info("");
        // Make stars
        stars = new LinkedHashMap<GPoint3d, GStar>();
        for (GFace face : faces) {
            for (int i = 0; i < face.lineCount(); i++) {
                GLine line = face.lineAt(i);
                for (int j = 0; j < line.labelCount() - 1; j++) {
                    GPoint3d p1 = points.get(line.labelAt(j));
                    GPoint3d p2 = points.get(line.labelAt(j + 1));
                    GStar star = stars.get(p1);
                    if (star == null) {
                        star = new GStar(p1);
                        stars.put(p1, star);
                    }
                    star.addNeighbor(p2);
                }
                for (int j = line.labelCount() - 1; j > 0; j--) {
                    GPoint3d p1 = points.get(line.labelAt(j));
                    GPoint3d p2 = points.get(line.labelAt(j - 1));
                    GStar star = stars.get(p1);
                    if (star == null) {
                        star = new GStar(p1);
                        stars.put(p1, star);
                    }
                    star.addNeighbor(p2);
                }
            }
        }
        for (GPoint3d p : points.values())
            p.resetLines();
        Map<GStick, GLine> edges = new LinkedHashMap<GStick, GLine>();
        Map<GPoint3d, Set<GFace>> facesThroughPoints =
            new LinkedHashMap<GPoint3d, Set<GFace>>();
        for (GFace face : faces) {
            for (int i = 0; i < face.lineCount(); i++) {
                GLine line = face.lineAt(i);
                line.setFace(face);
                for (int j = 0; j < line.labelCount(); j++) {
                    GPoint3d p = points.get(line.labelAt(j));
                    p.addLine(line);
                    Set<GFace> fs = facesThroughPoints.get(p);
                    if (fs == null) {
                        fs = new LinkedHashSet<GFace>();
                        facesThroughPoints.put(p, fs);
                    }
                    fs.add(face);
                }
            }
            for (int i = 0; i < face.sideCount(); i++) {
                GLine line = face.lineAt(i);
                GStick stick = new GStick(line);
                GLine twin = edges.get(stick);
                if (twin == null)
                    edges.put(stick, line);
                else {
                    line.setTwin(twin);
                    twin.setTwin(line);
                    edges.remove(stick);
                }
            }
        }
        for (GPoint3d p : facesThroughPoints.keySet()) {
            Set<GFace> fs = facesThroughPoints.get(p);
            p.setVertex(fs.size() > 2);
        }
    }

    public void project(GCamera camera) {
        Point3d bsCenter = boundingSphere.getCenter();
        for (GPoint3d p : points.values())
            p.project(camera, bsCenter);
    }

    public void toScreen(double scalingFactor, Dimension figSize) {
        for (GPoint3d p : points.values()) {
            int scrX =
                (int)(0.5 * figSize.width + scalingFactor * p.projCoords.x);
            int scrY =
                (int)(0.5 * figSize.height - scalingFactor * p.projCoords.y);
            p.scrCoords = new Point(scrX, scrY);
        }
    }

    public void paintTransparent(Graphics2D g2d, GCamera camera,
            double scalingFactor, Dimension figSize, boolean labelled) {
        project(camera);
        toScreen(scalingFactor, figSize);
        // Draw faces
        Map<GStick, Boolean> sticks = new LinkedHashMap<GStick, Boolean>();
        for (GFace face : faces) {
            boolean faceVisible = camera.visible(face, this, gCenter);
            for (int i = 0; i < face.lineCount(); i++) {
                GLine line = face.lineAt(i);
                GStick stick = new GStick(line);
                boolean visible = Boolean.TRUE.equals(sticks.get(stick));
                sticks.put(stick, visible || faceVisible);
            }
        }
        g2d.setColor(TRANSPARENT_LINE_COLOR);
        for (GStick stick : sticks.keySet()) {
            Point p1 = points.get(stick.label1).scrCoords;
            Point p2 = points.get(stick.label2).scrCoords;
            boolean visible = sticks.get(stick);
            g2d.setStroke(visible ? SOLID_STROKE : DASH_STROKE);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        // Draw points
        for (GPoint3d p : points.values()) {
            g2d.setColor(selection.contains(p) ? SELECTION_COLOR : POINT_COLOR);
            g2d.fillOval(p.scrCoords.x - POINT_DIAMETER / 2, p.scrCoords.y
                    - POINT_DIAMETER / 2, POINT_DIAMETER, POINT_DIAMETER);
            g2d.setColor(Color.black);
            if (labelled) {
                GStar star = stars.get(p);
                String label = p.getLabel();
                int labelAscent = g2d.getFontMetrics().getAscent();
                int labelWidth = g2d.getFontMetrics().stringWidth(label);
                Point labelPos = star.fitLabel(labelWidth, labelAscent);
                g2d.drawString(label, labelPos.x, labelPos.y);
            }
        }
        // Draw selection
        g2d.setColor(SELECTION_COLOR);
        for (GSelectable element : selection) {
            if (element instanceof GPoint3d) {
                Point p = ((GPoint3d)element).scrCoords;
                g2d.fillOval(p.x - SELECTED_POINT_DIAMETER / 2, p.y
                        - SELECTED_POINT_DIAMETER / 2, SELECTED_POINT_DIAMETER,
                        SELECTED_POINT_DIAMETER);
            }
            else if (element instanceof GStick) {
                Point p1 = points.get(((GStick)element).label1).scrCoords;
                Point p2 = points.get(((GStick)element).label2).scrCoords;
                g2d.setStroke(SELECTION_STROKE);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public void paintOpaque(Graphics2D g2d, GCamera camera,
            double scalingFactor, Dimension figSize, Color baseColor,
            boolean labelled) {
        project(camera);
        toScreen(scalingFactor, figSize);
        // Draw faces
        for (GFace face : faces) {
            if (camera.visible(face, this, gCenter))
                face.paintOpaque(g2d, baseColor, camera, selection, this,
                        gCenter);
        }
    }

    public GBoundingSphere getBoundingSphere() {
        return boundingSphere;
    }

    public String getSchemaFile() {
        return SCHEMA;
    }

    public GPoint3d getPoint(String label) {
        return points.get(label);
    }

    public int faceCount() {
        return faces.size();
    }

    public GFace faceAt(int index) {
        return faces.get(index);
    }

    public int pointCount() {
        return points.size();
    }

    public Iterator<GPoint3d> pointIterator() {
        return points.values().iterator();
    }

    public Collection<GFace> facesThroughPoints(String[] labels) {
        if (labels.length == 0)
            return new LinkedHashSet<GFace>();
        Collection<GFace> fs = facesThroughPoint(labels[0]);
        for (int i = 1; i < labels.length; i++) {
            Collection<GFace> fss = facesThroughPoint(labels[i]);
            fs.retainAll(fss);
            if (fs.isEmpty())
                return fs;
        }
        return fs;
    }

    public Collection<GFace> facesThroughPoint(String label) {
        GPoint3d p = getPoint(label);
        return p.getFaces();
    }

    public Collection<GLine> linesThroughPoints(String p1Label, String p2Label) {
        GPoint3d p1 = getPoint(p1Label);
        GPoint3d p2 = getPoint(p2Label);
        Set<GLine> lines1 = new LinkedHashSet<GLine>();
        for (int i = 0; i < p1.lineCount(); i++) {
            GLine line = p1.lineAt(i);
            lines1.add(line);
        }
        Set<GLine> lines2 = new LinkedHashSet<GLine>();
        for (int i = 0; i < p2.lineCount(); i++) {
            GLine line = p2.lineAt(i);
            lines2.add(line);
        }
        lines1.retainAll(lines2);
        return lines1;
    }

    public GPoint3d getPoint(Point3d coords) {
        double epsilon = getEpsilon();
        for (GPoint3d p : points.values()) {
            if (p.coords.epsilonEquals(coords, epsilon))
                return p;
        }
        return null;
    }

    public GPoint3d addPoint(Point3d coords) {
        logger.info(coords);
        String label = GLabelFactory.getInstance().createLabel(points.keySet());
        GPoint3d p = new GPoint3d(coords, label);
        points.put(label, p);
        return p;
    }

    public void removePoint(String label) {
        logger.info(label);
        points.remove(label);
    }

    public void undoRemovePoint(GPoint3d p) {
        points.put(p.getLabel(), p);
    }

    private GSelectable getSelectableAt(double xp, double yp,
            double scalingFactor, boolean transparent, GCamera camera) {
        double selectTolerance =
            DEFAULT_SELECT_TOLERANCE * Math.min(1.0, 1.0 / scalingFactor);
        if (transparent) {
            for (GPoint3d p : points.values())
                if (Point2D.distance(p.projCoords.x, p.projCoords.y, xp, yp) < selectTolerance)
                    return p;
            for (GFace face : faces) {
                GLine line = face.getLineAt(xp, yp, selectTolerance, points);
                if (line != null)
                    return new GStick(line);
            }
        }
        else {
            for (GFace face : faces) {
                if (camera.visible(face, this, gCenter)) {
                    Object element =
                        face.getElementAt(xp, yp, selectTolerance, points);
                    if (element instanceof GLine)
                        return new GStick((GLine)element);
                    if (element instanceof GPoint3d)
                        return (GPoint3d)element;
                    if (element instanceof GFace)
                        return (GFace)element;
                }
            }
        }
        return null;
    }

    public void select(double xp, double yp, double scalingFactor,
            boolean controlDown, boolean transparent, GCamera camera) {
        GSelectable element =
            getSelectableAt(xp, yp, scalingFactor, transparent, camera);
        if (element == null) {
            if (!controlDown)
                clearSelection();
        }
        else if (controlDown) {
            if (selection.contains(element))
                selection.remove(element);
            else
                selection.add(element);
        }
        else {
            if (selection.contains(element))
                clearSelection();
            else {
                clearSelection();
                selection.add(element);
            }
        }
    }

    public void clearSelection() {
        selection.clear();
    }

    public Set<GSelectable> getSelection() {
        return selection;
    }

    public void selectAll() {
        logger.info("");
        clearSelection();
        for (GPoint3d p : points.values()) {
            if (!p.isVertex())
                selection.add(p);
        }
        for (GFace face : faces) {
            for (int i = face.sideCount(); i < face.lineCount(); i++) {
                GLine line = face.lineAt(i);
                selection.add(new GStick(line));
            }
        }
    }

    public double computeVolume() {
       logger.info("");
       double volume = 0;
        for (GFace face : faces) {
            Point3d p1 = points.get(face.labelAt(0)).coords;
            Point3d p2 = points.get(face.labelAt(1)).coords;
            Point3d p3 = points.get(face.labelAt(2)).coords;
            volume += face.computeArea(this)
                * GMath.distanceToPlane(gCenter, p1, p2, p3);
        }
        return volume / 3;
    }

    public void scale(Point3d coords, Vector3d v, double factor) {
        logger.info(coords + ", " + v + ", " + factor);
        for (GPoint3d p : points.values()) {
            Point3d pCoords =
                GMath.scale(p.coords, new Point3d(0, 0, 0), v, factor);
            p.coords.set(pCoords);
        }
        makeConfig();
        computeGCenter();
        computeBoundingSphere();
    }

    public void shear(Point3d p0, Vector3d v1, Vector3d v2) {
        logger.info(p0 + ", " + v1 + ", " + v2);
        for (GPoint3d p : points.values()) {
            Point3d pCoords = GMath.shear(p.coords, p0, v1, v2);
            p.coords.set(pCoords);
        }
        makeConfig();
        computeGCenter();
        computeBoundingSphere();
    }

    public void undoShear(Point3d p0, Vector3d v1, Vector3d v2) {
        logger.info(p0 + ", " + v1 + ", " + v2);
        for (GPoint3d p : points.values()) {
            Point3d pCoords = GMath.undoShear(p.coords, p0, v1, v2);
            p.coords.set(pCoords);
        }
        makeConfig();
        computeGCenter();
        computeBoundingSphere();
    }

    public GFace getFace(String[] labels) {
        for (GFace face : faces) {
            boolean match = true;
            for (int i = 0; i < 3; i++) {
                if (!face.contains(labels[i])) {
                    match = false;
                    break;
                }
            }
            if (match)
                return face;
        }
        return null;
    }

    public double getEpsilon() {
        Iterator<GPoint3d> it = points.values().iterator();
        Point3d p1 = it.next().coords;
        Point3d p2 = it.next().coords;
        return GMath.EPSILON * p1.distance(p2);
    }

    public Point3d getGCenter() {
        return gCenter;
    }

    // Assume n has a positive length. Cut off this solid the part bounded by
    // plane (p0, n) and pointed to by n. Throw exception if plane has
    // an empty intersection with the interior of the solid.
    public void cutOff(Point3d p0Coords, Vector3d n) throws Exception {
        logger.info(p0Coords + ", " + n);
        double epsilon = getEpsilon();
        Point3d cs = null;
        for (GFace face : faces) {
            if (face.liesInPlane(p0Coords, n, this)) {
                logger.info("Plane does not intersect figure: " + p0Coords
                        + ", " + n);
                throw new Exception(
                        GDictionary.get("PlaneDoesNotIntersectFigure"));
            }
        }
        GFace face = null;
        for (int i = 0; i < faces.size(); i++) {
            face = faces.get(i);
            List<Point3d> css = face.intersectPlane(p0Coords, n, this);
            if (css.size() > 1) {
                cs = css.get(0);
                break;
            }
        }
        if (cs == null) {
            logger.info("Plane does not intersect figure");
            throw new Exception(
                    GDictionary.get("PlaneDoesNotIntersectFigure"));
        }
        GPoint3d pInit = getPoint(cs);
        if (pInit == null) {
            pInit = addPoint(cs);
            for (int i = 0; i < face.sideCount(); i++) {
                GLine line = face.lineAt(i);
                Point3d p1 = points.get(line.firstLabel()).coords;
                Point3d p2 = points.get(line.lastLabel()).coords;
                if (GMath.isBetween(pInit.coords, p1, p2, epsilon)) {
                    face.addPoint(pInit, this);
                    pInit.addLine(line);
                    GLine l = line.getTwin();
                    GFace f = l.getFace();
                    f.addPoint(pInit, this);
                    pInit.addLine(l);
                    break;
                }
            }
        }

        // Run along the section, map sectioned faces to section lines
        GPoint3d pPrev = null;
        GPoint3d pCurr = pInit;
        GPoint3d pNext = null;
        GFace fCurr = null;
        Map<GLine, GFace> sectionMap = new LinkedHashMap<GLine, GFace>();
        while (true) {
            Collection<GFace> fs = facesThroughPoint(pCurr.getLabel());
            Point3d coords = null;
            for (GFace f : fs) {
                List<Point3d> ps = f.intersectPlane(p0Coords, n, this);
                if (ps.size() < 2)
                    continue;
                GPoint3d p1 = getPoint(ps.get(0));
                GPoint3d p2 = getPoint(ps.get(1));
                if (p1 == pCurr) {
                    pNext = p2;
                    coords = ps.get(1);
                }
                else {
                    pNext = p1;
                    coords = ps.get(0);
                }
                if (pPrev != null
                        && pNext == pPrev)
                    continue;
                fCurr = f;
                if (pNext == null) {
                    pNext = addPoint(coords);
                    for (int i = 0; i < fCurr.sideCount(); i++) {
                        GLine line = fCurr.lineAt(i);
                        GPoint3d pp1 = points.get(line.firstLabel());
                        GPoint3d pp2 = points.get(line.lastLabel());
                        if (GMath.isStrictlyBetween(pNext.coords, pp1.coords,
                                pp2.coords, epsilon)) {
                            fCurr.addPoint(pNext, this);
                            GLine l = line.getTwin();
                            GFace ff = l.getFace();
                            ff.addPoint(pNext, this);
                            pNext.addLine(line);
                            pNext.addLine(l);
                            break;
                        }
                    }
                }
                break;
            }
            GLine line =
                fCurr.lineThroughPoints(pCurr.getLabel(), pNext.getLabel());
            if (line == null) {
                List<GLine> removedLines = new ArrayList<GLine>();
                line = fCurr.addLine(pCurr, pNext, removedLines, this);
                for (GLine l : removedLines)
                    fCurr.removeLine(l, new ArrayList<String>());
            }
            if (fCurr.containsSide(line))
                sectionMap.put(line, null);
            else
                sectionMap.put(line, fCurr);
            if (pNext == pInit)
                break;
            pPrev = pCurr;
            pCurr = pNext;
        }

        // Collect faces located entirely on the wrong side of the section,
        // together with their vertices
        List<GFace> toBeRemovedFaces = new ArrayList<GFace>();
        Set<GPoint3d> toBeRemovedPoints = new LinkedHashSet<GPoint3d>();
        for (GFace f : faces) {
            if (sectionMap.containsValue(f))
                continue;
            Point3d fCenter = f.computeGCenter(this);
            Vector3d v = new Vector3d(fCenter);
            v.sub(p0Coords);
            if (v.dot(n) < 0)
                continue;
            toBeRemovedFaces.add(f);
            for (int i = 0; i < f.lineCount(); i++) {
                GLine line = f.lineAt(i);
                for (int j = 0; j < line.labelCount(); j++) {
                    GPoint3d p = points.get(line.labelAt(j));
                    if (!GMath.isInPlane(p.coords, p0Coords, n))
                        toBeRemovedPoints.add(p);
                }
            }
        }

        // Clip sectioned faces. Add the new face to the end of the list
        String prevEndLabel = null;
        List<GLine> ls = new ArrayList<GLine>();
        for (GLine line : sectionMap.keySet()) {
            GFace ff = sectionMap.get(line);
            if (ff != null) {
                Set<GPoint3d> tbrPs = new LinkedHashSet<GPoint3d>();
                ff.cutOff(line, n, tbrPs, this);
                toBeRemovedPoints.addAll(tbrPs);
            }
            GLine l = line.clone();
            if (prevEndLabel != null
                    && !line.firstLabel().equals(prevEndLabel))
                l.reverse();
            prevEndLabel = l.lastLabel();
            if (ff != null)
                l.setTwin(line);
            else {
                GFace f = line.getFace();
                if (!toBeRemovedFaces.contains(f))
                    l.setTwin(line);
                else
                    l.setTwin(line.getTwin());
            }
            ls.add(l);
        }
        GFace section = new GFace(ls.size(), ls);
        section.chainSides();
        for (GPoint3d p : toBeRemovedPoints)
            points.remove(p.getLabel());
        faces.removeAll(toBeRemovedFaces);
        faces.add(section);
        makeConfig();
        computeGCenter();
        computeBoundingSphere();
    }

    public void cutOff(String p0Label, Vector3d n) throws Exception {
        logger.info(p0Label + ", " + n);
        Point3d p0Coords = getPoint(p0Label).coords;
        cutOff(p0Coords, n);
    }

    private SortedMap<Integer, Set<GFace>> groupFacesBySize() {
        logger.info("");
        SortedMap<Integer, Set<GFace>> facesBySize =
            new TreeMap<Integer, Set<GFace>>();
        for (GFace face : faces) {
            int sideCount = face.sideCount();
            if (!facesBySize.containsKey(sideCount))
                facesBySize.put(sideCount, new LinkedHashSet<GFace>());
            Set<GFace> faceGroup = facesBySize.get(sideCount);
            faceGroup.add(face);
        }
        return facesBySize;
    }

    public Map<GFace, Map<GFace, List<Integer>>> getJoinMatches(GSolid solid) {
        logger.info("");
        Map<GFace, Map<GFace, List<Integer>>> joinMatches =
            new LinkedHashMap<GFace, Map<GFace, List<Integer>>>();

        // Group faces by size
        SortedMap<Integer, Set<GFace>> facesBySize1 = groupFacesBySize();
        SortedMap<Integer, Set<GFace>> facesBySize2 = solid.groupFacesBySize();

        // Remove groups with unmatched sizes
        Set<Integer> unmatchedSizes = new LinkedHashSet<Integer>();
        for (int size : facesBySize1.keySet()) {
            if (!facesBySize2.containsKey(size))
                unmatchedSizes.add(size);
        }
        for (int size : facesBySize2.keySet()) {
            if (!facesBySize1.containsKey(size))
                unmatchedSizes.add(size);
        }
        for (int size : unmatchedSizes) {
            facesBySize1.remove(size);
            facesBySize2.remove(size);
        }

        // Match similar faces
        Iterator<Set<GFace>> it1 = facesBySize1.values().iterator();
        Iterator<Set<GFace>> it2 = facesBySize2.values().iterator();
        while (it1.hasNext()) {
            Set<GFace> fs1 = it1.next();
            Set<GFace> fs2 = it2.next();
            for (GFace face1 : fs1) {
                for (GFace face2 : fs2) {
                    boolean flipFace2 = face1.getOrientation(this, gCenter) ==
                        face2.getOrientation(solid, solid.gCenter);
                    List<Integer> indexes =
                        face1.match(face2, flipFace2, this,solid);
                    if (!indexes.isEmpty()) {
                        Map<GFace, List<Integer>> face1Matches =
                            joinMatches.get(face1);
                        if (face1Matches == null) {
                            face1Matches =
                                new LinkedHashMap<GFace, List<Integer>>();
                            joinMatches.put(face1, face1Matches);
                        }
                        face1Matches.put(face2, indexes);
                    }
                }
            }
        }
        return joinMatches;
    }

    public GSolid join(GSolid solid1, GFace face, GFace face1, int matchIndex)
            throws Exception {
        logger.info(this + ", " + solid1 + ", " + face + ", " + face1 + ", "
                + matchIndex);
        double epsilon = getEpsilon();
        try {
            boolean flipFace1 = face.getOrientation(this, gCenter) ==
                face1.getOrientation(solid1, solid1.gCenter);
            GSolid solid1Clone = (GSolid) solid1.clone();
            String[] labels1 = {
                    face1.labelAt(0), face1.labelAt(1), face1.labelAt(2) };
            GFace face1Clone =
                solid1Clone.facesThroughPoints(labels1).iterator().next();
            if (flipFace1)
                face1Clone.reverse();
            face1Clone.anchorAt(face1Clone.sideCount() - matchIndex);

            // Scale solid1
            double factor = face.lineAt(0).length(this)
            / face1Clone.lineAt(0).length(solid1Clone);
            for (GPoint3d p : solid1Clone.points.values())
                p.coords.scale(factor);

            // Bring face1Clone onto 'face'
            // Compute rotation matrix for solid1Clone
            Vector3d v1 = face.lineAt(0).toVector(this);
            Vector3d v2 = face.lineAt(face.sideCount() - 1).toVector(this);
            v2.scale(-1);
            GMath.orthize(v1, v2);
            Matrix3d matrix = new Matrix3d();
            matrix.setColumn(0, v1);
            matrix.setColumn(1, v2);
            matrix.setColumn(2, face.getNormal(this));
            Vector3d v11 = face1Clone.lineAt(0).toVector(solid1Clone);
            Vector3d v12 = face1Clone.lineAt(face1Clone.sideCount() - 1)
                .toVector(solid1Clone);
            v12.scale(-1);
            GMath.orthize(v11, v12);
            Matrix3d matrix1 = new Matrix3d();
            matrix1.setColumn(0, v11);
            matrix1.setColumn(1, v12);
            matrix1.setColumn(2, face1Clone.getNormal(solid1Clone));
            matrix1.invert();
            matrix.mul(matrix1);

            // Rotate solid1
            Point3d pivot = solid1Clone.getPoint(face1Clone.labelAt(0)).coords;
            for (GPoint3d p : solid1Clone.points.values()) {
                Vector3d v = new Vector3d(p.coords);
                v.sub(pivot);
                matrix.transform(v);
                p.coords.add(pivot, v);
            }

            // Translate solid1
            Vector3d v = new Vector3d(getPoint(face.labelAt(0)).coords);
            v.sub(solid1Clone.getPoint(face1Clone.labelAt(0)).coords);
            for (GPoint3d p1 : solid1Clone.points.values())
                p1.coords.add(v);
            solid1Clone.computeGCenter();

            // Check convexity of the joint solid
            for (GFace f : faces) {
                if (f == face)
                    continue;
                Vector3d outerNormal = f.getNormal(this, gCenter);
                Point3d p = getPoint(f.labelAt(0)).coords;
                for (GPoint3d p1 : solid1Clone.points.values()) {
                    v = new Vector3d(p1.coords);
                    v.sub(p);
                    if (v.dot(outerNormal) > epsilon)
                        return null;
                }
            }
            for (GFace f1 : solid1Clone.faces) {
                if (f1 == face1Clone)
                    continue;
                Vector3d outerNormal =
                    f1.getNormal(solid1Clone, solid1Clone.gCenter);
                Point3d p1 = solid1Clone.getPoint(f1.labelAt(0)).coords;
                for (GPoint3d p : points.values()) {
                    v = new Vector3d(p.coords);
                    v.sub(p1);
                    if (v.dot(outerNormal) > epsilon)
                        return null;
                }
            }

            GSolid thisClone = clone();
            String[] labels =
                { face.labelAt(0), face.labelAt(1), face.labelAt(2) };
            GFace faceClone = thisClone.facesThroughPoints(labels).iterator().next();

            // Join 'thisClone' and 'solid1Clone'
            Map<GPoint3d, GPoint3d> p1ToP =
                new LinkedHashMap<GPoint3d, GPoint3d>();
            Set<GFace> faces1 = new LinkedHashSet<GFace>();
            Set<GFace> newFaces = new LinkedHashSet<GFace>();
            for (int i = 0; i < faceClone.sideCount(); i++) {
                GLine line = faceClone.lineAt(i).getTwin();
                GLine line1 = face1Clone.lineAt(i).getTwin();
                for (int j = 0; j < line1.labelCount(); j++) {
                    GPoint3d p1 = solid1Clone.getPoint(line1.labelAt(j));
                    GPoint3d p = line.getPoint(p1.coords, thisClone);
                    if (p == null) {
                        p = thisClone.addPoint(p1.coords);
                        line.insert(p, thisClone);
                    }
                    p1ToP.put(p1, p);
                }

                GFace f = line.getFace();
                GFace f1 = line1.getFace();
                int index1 = f1.indexOf(line1);
                f1.anchorAt(index1);
                List<GLine> ls = new ArrayList<GLine>();
                GLine newL1 = new GLine(line);
                ls.add(newL1);
                for (int j = 1; j < f1.lineCount(); j++) {
                    GLine l1 = f1.lineAt(j);
                    List<String> ps = new ArrayList<String>();
                    for (int k = 0; k < l1.labelCount(); k++) {
                        GPoint3d p1 = solid1Clone.getPoint(l1.labelAt(k));
                        GPoint3d p = p1ToP.get(p1);
                        if (p == null) {
                            p = thisClone.addPoint(p1.coords);
                            p1ToP.put(p1, p);
                        }
                        ps.add(p.getLabel());
                    }
                    newL1 = new GLine(ps);
                    ls.add(newL1);
                }
                faces1.add(f1);
                GFace newFace = new GFace(f1.sideCount(), ls);
                newFace.chainSides();

                if (f1.getNormal(solid1Clone, solid1Clone.gCenter).dot(
                        f.getNormal(thisClone, thisClone.gCenter)) <
                        1 - GMath.EPSILON) {
                    newFaces.add(newFace);
                    continue;
                }

                // Merge f and newFace into nf
                int index = f.indexOf(line);
                f.anchorAt(index);
                if (f.labelAt(0).equals(newFace.labelAt(0))) {
                    newFace.reverse();
                    newFace.anchorAt(newFace.sideCount() - 1);
                }
                int sc = f.sideCount();
                int sc1 = newFace.sideCount();
                ls = new ArrayList<GLine>();
                for (int j = 1; j < sc; j++) {
                    GLine l = f.lineAt(j);
                    ls.add(l);
                }
                for (int j = 1; j < sc1; j++) {
                    GLine newL = newFace.lineAt(j);
                    ls.add(newL);
                }
                GLine lBegin = ls.get(0);
                GLine lEnd = ls.get(sc - 2);
                GLine newLBegin = ls.get(sc - 1);
                GLine newLEnd = ls.get(sc
                        + sc1 - 3);
                if (lBegin.acquire(newLEnd, thisClone))
                    ls.remove(newLEnd);
                if (lEnd.acquire(newLBegin, thisClone))
                    ls.remove(newLBegin);
                int newSideCount = ls.size();
                GFace nf = new GFace(newSideCount, ls);
                nf.chainSides();
                for (int j = sc; j < f.lineCount(); j++) {
                    GLine l = f.lineAt(j);
                    nf.addLine(l);
                }
                for (int j = sc1; j < newFace.lineCount(); j++) {
                    GLine l = newFace.lineAt(j);
                    for (int k = sc; k < f.lineCount(); k++) {
                        GLine ll = f.lineAt(k);
                        if (!ll.acquire(l, thisClone)) {
                        	nf.addLine(l);
                        }
                    }
                }
                nf.addLine(line);
                thisClone.faces.remove(f);
                newFaces.add(nf);
            }

            faces1.add(face1Clone);

            for (GFace f1 : solid1Clone.faces) {
                if (faces1.contains(f1))
                    continue;
                List<GLine> ls = new ArrayList<GLine>();
                for (int j = 0; j < f1.lineCount(); j++) {
                    GLine l1 = f1.lineAt(j);
                    List<String> ps = new ArrayList<String>();
                    for (int k = 0; k < l1.labelCount(); k++) {
                        GPoint3d p1 = solid1Clone.getPoint(l1.labelAt(k));
                        GPoint3d p = p1ToP.get(p1);
                        if (p == null) {
                            p = thisClone.addPoint(p1.coords);
                            p1ToP.put(p1, p);
                        }
                        ps.add(p.getLabel());
                    }
                    GLine newLine = new GLine(ps);
                    ls.add(newLine);
                }
                faces1.add(f1);
                GFace newFace = new GFace(f1.sideCount(), ls);
                newFace.chainSides();
                newFaces.add(newFace);
            }

            thisClone.faces.remove(faceClone);
            Set<String> ls = new LinkedHashSet<String>();
            for (int i = 0; i < faceClone.sideCount(); i++) {
                GLine line = faceClone.lineAt(i);
                for (int j = 0; j < line.labelCount(); j++)
                    ls.add(line.labelAt(j));
            }
            for (int i = faceClone.sideCount(); i < faceClone.lineCount(); i++) {
                GLine line = faceClone.lineAt(i);
                for (int j = 0; j < line.labelCount(); j++) {
                    String label = line.labelAt(j);
                    if (!ls.contains(label))
                        thisClone.removePoint(label);
                }
            }
            thisClone.faces.addAll(newFaces);
            thisClone.makeConfig();
            thisClone.computeGCenter();
            thisClone.makeConfig();
            thisClone.computeBoundingSphere();
            return thisClone;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void buildFaces(Map<String, GPoint3d> pointMap,
            List<GLine> lineList) throws Exception {
        logger.info("");
        // Calculate epsilon
        Point3d o = new Point3d(0, 0, 0);
        double maxDist = 0;
        for (GPoint3d p : pointMap.values()) {
            double dist = p.coords.distance(o);
            if (dist > maxDist)
                maxDist = dist;
        }
        double epsilon = maxDist * GMath.EPSILON;
        // Find vertices of a tetrahedron
        List<GPoint3d> toBeRemovedPoints = new ArrayList<GPoint3d>();
        for (GPoint3d p : pointMap.values()) {
            if (points.isEmpty()) {
                points.put(p.getLabel(), p);
                toBeRemovedPoints.add(p);
                continue;
            }
            if (points.size() == 1) {
                Iterator<GPoint3d> it = points.values().iterator();
                if (it.next().coords.epsilonEquals(p.coords, epsilon)) {
                    logger.error("Points " + p.getLabel() + ", "
                    + points.keySet().iterator().next() + " virtually equal");
                    throw new Exception();
                }
                points.put(p.getLabel(), p);
                toBeRemovedPoints.add(p);
                continue;
            }
            if (points.size() == 2) {
                Iterator<GPoint3d> it = points.values().iterator();
                Point3d[] ps =
                    { it.next().coords, it.next().coords, p.coords };
                if (GMath.areCollinear(ps, epsilon))
                    continue;
                points.put(p.getLabel(), p);
                toBeRemovedPoints.add(p);
                continue;
            }
            if (points.size() == 3) {
                Iterator<GPoint3d> it = points.values().iterator();
                Vector3d n = GMath.cross(it.next().coords, it.next().coords,
                        it.next().coords);
                n.normalize();
                Vector3d v = new Vector3d(p.coords);
                it = points.values().iterator();
                v.sub(it.next().coords);
                if (Math.abs(v.dot(n)) < epsilon)
                    continue;
                points.put(p.getLabel(), p);
                toBeRemovedPoints.add(p);
                break;
            }
        }
        Point3d refPoint = new Point3d();
        for (GPoint3d p : points.values())
            refPoint.add(p.coords);
        refPoint.scale(1.0 / points.size());
        // Make faces of tetrahedron
        faces = new ArrayList<GFace>();
        Iterator<GPoint3d> it = points.values().iterator();
        GPoint3d[] ps =
            { it.next(), it.next(), it.next(), it.next() };
        GPoint3d[] ps1 =
            { ps[0], ps[1], ps[2] };
        faces.add(makeTriangle(ps1, refPoint));
        GPoint3d[] ps2 =
            { ps[1], ps[2], ps[3] };
        faces.add(makeTriangle(ps2, refPoint));
        GPoint3d[] ps3 =
            { ps[2], ps[3], ps[0] };
        faces.add(makeTriangle(ps3, refPoint));
        GPoint3d[] ps4 =
            { ps[3], ps[0], ps[1] };
        faces.add(makeTriangle(ps4, refPoint));
        makeConfig();
        // Add new vertices
        for (GPoint3d p : pointMap.values()) {
            if (points.containsKey(p.getLabel()))
                continue;
            addPoint(p, refPoint);
            points.put(p.getLabel(), p);
            makeConfig();
        }
        Set<String> vertexLabels = new LinkedHashSet<String>();
        for (GFace face : faces) {
            for (int i = 0; i < face.sideCount(); i++) {
                String label = face.labelAt(i);
                vertexLabels.add(label);
            }
        }
        points.keySet().retainAll(vertexLabels);
        makeConfig();
        // Add non-vertex points to faces' perimeters
        for (GPoint3d p : pointMap.values()) {
            if (points.containsKey(p.getLabel()))
                continue;
            boolean added = false;
            for (GFace face : faces) {
                if (face.addPoint(p, this))
                    added = true;
            }
            if (added)
                points.put(p.getLabel(), p);
        }
        makeConfig();
        // Add non-side lines to faces
        for (GLine line : lineList) {
            boolean lineAdded = false;
            for (GFace face : faces) {
                GPoint3d p1 = pointMap.get(line.firstLabel());
                GPoint3d p2 = pointMap.get(line.lastLabel());
                if (face.covers(p1.coords, this)
                        && face.covers(p2.coords, this)) {
                    if (!points.containsKey(p1.getLabel()))
                        points.put(p1.getLabel(), p1);
                    if (!points.containsKey(p2.getLabel()))
                        points.put(p2.getLabel(), p2);
                    GLine l =
                        face.addLine(p1, p2, new ArrayList<GLine>(), this);
                    if (l != null) {
                        lineAdded = true;
                        break;
                    }
                }
            }
            if (!lineAdded) {
                logger.error("Line " + line.toString()
                        + " is contained in no face");
                throw new Exception();
            }
        }
        // Add remaining points to faces
        for (GPoint3d p : pointMap.values()) {
            if (!points.containsKey(p.getLabel())) {
                boolean pointAdded = false;
                for (GFace face : faces) {
                    if (face.addPoint(p, this))
                        pointAdded = true;
                }
                if (!pointAdded) {
                    logger.error("Point " + p.getLabel()
                            + " is contained in no face");
                    throw new Exception();
                }
                points.put(p.getLabel(), p);
            }
        }
        makeConfig();
    }

    private boolean addPoint(GPoint3d p, Point3d refPoint) throws Exception {
        Set<GFace> front = new LinkedHashSet<GFace>();
        Set<GFace> rear = new LinkedHashSet<GFace>();
        Set<GFace> rim = new LinkedHashSet<GFace>();
        for (GFace face : faces) {
            int orientation = face.getOrientation(this, p.coords);
            if (orientation == GMath.NEGATIVE)
                rear.add(face);
            else if (orientation == GMath.POSITIVE)
                front.add(face);
            else
                rim.add(face);
        }
        if (front.isEmpty())
            return false;
        List<GFace> fs = new ArrayList<GFace>();
        for (GFace face : rim) {
            face.addExternalPoint(p, this);
            fs.add(face);
        }
        for (GFace face : rear) {
            for (int i = 0; i < face.sideCount(); i++) {
                GLine line = face.lineAt(i);
                GFace f = line.getTwin().getFace();
                if (front.contains(f)) {
                    GPoint3d[] ps = { getPoint(line.firstLabel()),
                            getPoint(line.lastLabel()), p };
                    GFace ff = makeTriangle(ps, refPoint);
                    fs.add(ff);
                }
            }
            fs.add(face);
        }
        faces = fs;
        return true;
    }

    private GFace makeTriangle(GPoint3d[] ps, Point3d refPoint) {
        List<GLine> lines = new ArrayList<GLine>();
        lines.add(new GLine(ps[0].getLabel(), ps[1].getLabel()));
        lines.add(new GLine(ps[1].getLabel(), ps[2].getLabel()));
        lines.add(new GLine(ps[2].getLabel(), ps[0].getLabel()));
        GFace face = new GFace(3, lines);
        if (GMath.getOrientation(ps[0].coords, ps[1].coords, ps[2].coords,
                refPoint) == GMath.POSITIVE)
            face.reverse();
        return face;
    }

    public void toOff(StringBuffer buf) {
        Map<String, Integer> vertices = new LinkedHashMap<String, Integer>();
        int index = 0;
        for (GPoint3d p : points.values())
            if (p.isVertex())
                vertices.put(p.getLabel(), index++);
        int vertexCount = vertices.size();
        int faceCount = faces.size();
        buf.append("OFF")
            .append("\n")
            .append(vertexCount)
            .append(" ")
            .append(faceCount)
            .append(" ")
            .append(0);
        for (String label : vertices.keySet()) {
            Point3d coords = getPoint(label).coords;
            buf.append("\n")
                .append(coords.x)
                .append(" ")
                .append(coords.y)
                .append(" ")
                .append(coords.z);
        }
        for (GFace face : faces) {
            int sideCount = face.sideCount();
            buf.append("\n")
                .append(sideCount);
            for (int i = 0; i < sideCount; i++) {
                String label = face.labelAt(i);
                int ind = vertices.get(label);
                buf.append(" ")
                    .append(ind);
            }
        }
    }

    public boolean isSimilar(GSolid solid) {
        if (solid.points.size() != points.size())
            return false;
        if (solid.faces.size() != faces.size())
            return false;
        List<Double> angles = getDihedralAngles();
        List<Double> solidAngles = solid.getDihedralAngles();
        for (int i = 0; i < angles.size(); i++) {
            double angle = angles.get(i);
            double solidAngle = solidAngles.get(i);
            if (Math.abs(angle - solidAngle) >= GMath.EPSILON)
                return false;
        }
        List<Double> areas = getFaceAreas();
        List<Double> solidAreas = solid.getFaceAreas();
        double factor = areas.get(0) / solidAreas.get(0);
        for (int i = 0; i < areas.size(); i++) {
            double area = areas.get(i);
            double solidArea = solidAreas.get(i);
            if (Math.abs(solidArea * factor / area - 1) >= GMath.EPSILON)
                return false;
        }
        return true;
    }

    List<Double> getDihedralAngles() {
        List<Double> angles = new ArrayList<Double>();
        for (GFace face1 : faces) {
            Vector3d n1 = face1.getNormal(this);
            for (int i = 0; i < face1.sideCount(); i++) {
                GLine line = face1.lineAt(i);
                GFace face2 = line.getTwin().getFace();
                Vector3d n2 = face2.getNormal(this);
                double angle = n1.angle(n2);
                angles.add(angle);
            }
        }
        Collections.sort(angles);
        List<Double> nonDuplicatedAngles = new ArrayList<Double>();
        for (int i = 0; i < angles.size(); i += 2) {
            double angle = angles.get(i);
            nonDuplicatedAngles.add(angle);
        }

        return nonDuplicatedAngles;
    }

    List<Double> getFaceAreas() {
        List<Double> areas = new ArrayList<Double>();
        for (GFace face : faces) {
            double area = face.computeArea(this);
            areas.add(area);
        }
        Collections.sort(areas);
        return areas;
    }

    public void RenamePoint(String oldLabel, String newLabel) {
        GPoint3d point = points.get(oldLabel);
        point.setLabel(newLabel);
        points.remove(oldLabel);
        points.put(newLabel, point);
        for (GFace face : faces) {
            face.pointRenamed(oldLabel, newLabel);
        }
    }

    public void serialize(StringBuffer buf) {
        serialize(buf, false);
    }
    
    public void serialize(StringBuffer buf, boolean preamble) {
        logger.info(preamble);
        if (preamble) {
            buf.append(PREAMBLE);
            buf.append("\n<solid xmlns=\"");
            buf.append(APPLICATION_NAMESPACE);
            buf.append("\">");
            buf.append("\n<version>");
            buf.append(GApplicationManager.getInstance().getVersion());
            buf.append("</version>");
        }
        else
            buf.append("\n<solid>");
        buf.append("\n<points>");
        double epsilon = getEpsilon();
        for (GPoint3d p : points.values()) {
            p.trimCoords(epsilon);
            p.serialize(buf);
        }
        buf.append("\n</points>");
        buf.append("\n<lines>");
        for (GFace face : faces) {
            for (int i = face.sideCount(); i < face.lineCount(); i++) {
                GLine line = face.lineAt(i);
                line.serialize(buf);
            }
        }
        buf.append("\n</lines>");
        buf.append("\n</solid>");
    }
   
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        for (GPoint3d p : points.values()) {
            if (p.isVertex())
                buf.append(p.getLabel());
        }
        buf.append("]");
        return String.valueOf(buf);
    }
}
