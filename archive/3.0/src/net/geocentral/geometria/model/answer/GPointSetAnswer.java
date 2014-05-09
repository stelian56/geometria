/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetFactory;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GPointSetAnswer implements GAnswer {

    private Point3d[] coords;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GPointSetAnswer() {}

    public GPointSetAnswer(Point3d[] coords) {
        logger.info(Arrays.asList(coords));
        this.coords = new Point3d[coords.length];
        for (int i = 0; i < coords.length; i++)
            this.coords[i] = new Point3d(coords[i]);
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("coords");
        if (ns.getLength() < 1) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        coords = new Point3d[ns.getLength()];
        for (int i = 0; i < ns.getLength(); i++) {
            String coordsString = ns.item(i).getTextContent();
            coords[i] = GStringUtils.coordsFromString(coordsString);
            if (coords[i] == null) {
                logger.error(coordsString);
                throw new Exception();
            }
        }
    }

    public boolean validate(String valueString, String figureName,
            GDocument document) {
        Point3d[] cs;
        try {
            cs = GPointSetFactory.getInstance().fromString(
                valueString, figureName, document);
        }
        catch (Exception exception) {
            return false;
        }
        GFigure figure = document.getFigure(figureName);
        double epsilon = figure == null ? GMath.EPSILON 
                : figure.getSolid().getEpsilon();
        return verify(cs, epsilon);
    }

    public boolean verify(Point3d[] cs, double epsilon) {
        logger.info(Arrays.asList(cs) + ", " + epsilon);
        if (cs.length != coords.length)
            return false;
        List<Integer> indexes = new ArrayList<Integer>();
        for (int index = 0; index < coords.length; index++)
            indexes.add(index);
        List<List<Integer>> permutations = allPermutations(indexes);
        for (List<Integer> permutation : permutations) {
            boolean diff = false;
            for (int i = 0; i < coords.length; i++) {
                int index = permutation.get(i);
                diff = !cs[index].epsilonEquals(coords[i], epsilon);
                if (diff)
                    break;
            }
            if (!diff)
                return true;
        }
        return false;
    }

    private List<List<Integer>> allPermutations(List<Integer> array) {
        logger.info(array);
        List<List<Integer>> permutations = new ArrayList<List<Integer>>();
        if (array.size() == 1) {
            permutations.add(array);
            return permutations;
        }
        List<Integer> arrayCopy = array.subList(0, array.size() - 1);
        List<Integer> head = new ArrayList<Integer>();
        for (int item : arrayCopy)
            head.add(item);
        List<List<Integer>> headPermutations = allPermutations(head);
        for (List<Integer> headPermutation : headPermutations) {
            for (int i = 0; i < array.size(); i++) {
                List<Integer> permutation = new ArrayList<Integer>();
                for (int item : headPermutation)
                    permutation.add(item);
                permutation.add(i, array.get(array.size() - 1));
                permutations.add(permutation);
            }
        }
        return permutations;
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<answer>");
        buf.append("\n<type>pointSet</type>");
        for (int i = 0; i < coords.length; i++) {
            buf.append("\n<coords>");
            buf.append(GStringUtils.coordsToString(coords[i]));
            buf.append("</coords>");
        }
        buf.append("\n</answer>");
    }

    public Point3d[] getCoords() {
        Point3d[] cs = new Point3d[coords.length];
        for (int i = 0; i < coords.length; i++)
            cs[i] = new Point3d(coords[i]);
        return cs;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < coords.length; i++) {
            buf.append(GStringUtils.coordsToString(coords[i], true));
            if (i < coords.length - 1)
                buf.append(" , ");
        }
        return String.valueOf(buf);
    }
}
