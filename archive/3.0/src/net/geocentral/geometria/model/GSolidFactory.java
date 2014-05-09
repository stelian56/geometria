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
import java.util.List;

import javax.vecmath.Point3d;

import org.apache.log4j.Logger;

public class GSolidFactory {

    private static GSolidFactory instance;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GSolidFactory() {}
    
    public static GSolidFactory getInstance() {
        if (instance == null)
            instance = new GSolidFactory();
        return instance;
    }

    public GSolid newPrism(int sideCount) {
        logger.info(sideCount);
        final double dAngle = 2 * Math.PI / sideCount;
        double angle = 0;
        List<GPoint3d> ps = new ArrayList<GPoint3d>();
        String label = null;
        for (int i = 0; i < sideCount; i++) {
            label = GLabelFactory.getInstance().nextLabel(label);
            GPoint3d p = new GPoint3d(Math.cos(angle), Math.sin(angle), 0,
                    label);
            p.setVertex(true);
            ps.add(p);
            angle += dAngle;
        }
        for (int i = 0; i < sideCount; i++) {
            label = GLabelFactory.getInstance().nextLabel(label);
            Point3d pBase = ps.get(i).coords;
            GPoint3d p = new GPoint3d(pBase.x, pBase.y, 1, label);
            p.setVertex(true);
            ps.add(p);
        }
        List<GFace> faces = new ArrayList<GFace>();
        List<GLine> upperEdges = new ArrayList<GLine>();
        List<GLine> lowerEdges = new ArrayList<GLine>();
        for (int i = 0; i < sideCount; i++) {
            GPoint3d pBase1 = ps.get(i);
            GPoint3d pBase2 = ps.get((i + 1) % sideCount);
            GPoint3d p1 = ps.get(sideCount + i);
            GPoint3d p2 = ps.get(sideCount + (i + 1) % sideCount);
            List<GLine> sideEdges = new ArrayList<GLine>();
            GLine line = new GLine(pBase1.getLabel(), pBase2.getLabel());
            sideEdges.add(line);
            line = new GLine(pBase1.getLabel(), pBase2.getLabel());
            lowerEdges.add(line);
            line = new GLine(pBase2.getLabel(), p2.getLabel());
            sideEdges.add(line);
            line = new GLine(p2.getLabel(), p1.getLabel());
            sideEdges.add(line);
            line = new GLine(p1.getLabel(), p2.getLabel());
            upperEdges.add(line);
            line = new GLine(p1.getLabel(), pBase1.getLabel());
            sideEdges.add(line);
            GFace face = new GFace(4, sideEdges);
            faces.add(face);
        }
        GFace face = new GFace(sideCount, lowerEdges);
        faces.add(face);
        face = new GFace(sideCount, upperEdges);
        faces.add(face);
        return new GSolid(ps, faces);
    }

    public GSolid newPyramid(int sideCount) {
        logger.info(sideCount);
        final double dAngle = 2 * Math.PI / sideCount;
        double angle = 0;
        List<GPoint3d> ps = new ArrayList<GPoint3d>();
        String label = null;
        for (int i = 0; i < sideCount; i++) {
            label = GLabelFactory.getInstance().nextLabel(label);
            GPoint3d p =
                new GPoint3d(Math.cos(angle), Math.sin(angle), 0, label);
            p.setVertex(true);
            ps.add(p);
            angle += dAngle;
        }
        label = GLabelFactory.getInstance().nextLabel(label);
        GPoint3d apex = new GPoint3d(0, 0, 2 * Math.sqrt(2.0 / 3), label);
        apex.setVertex(true);
        ps.add(apex);
        List<GFace> faces = new ArrayList<GFace>();
        List<GLine> baseEdges = new ArrayList<GLine>();
        for (int i = 0; i < sideCount; i++) {
            GPoint3d p1 = ps.get(i);
            GPoint3d p2 = ps.get((i + 1) % sideCount);
            List<GLine> sideEdges = new ArrayList<GLine>();
            GLine edge = new GLine(p1.getLabel(), p2.getLabel());
            sideEdges.add(edge);
            edge = new GLine(p1.getLabel(), p2.getLabel());
            baseEdges.add(edge);
            edge = new GLine(p2.getLabel(), apex.getLabel());
            sideEdges.add(edge);
            edge = new GLine(apex.getLabel(), p1.getLabel());
            sideEdges.add(edge);
            GFace face = new GFace(3, sideEdges);
            faces.add(face);
        }
        GFace face = new GFace(sideCount, baseEdges);
        faces.add(face);
        return new GSolid(ps, faces);
    }
}
