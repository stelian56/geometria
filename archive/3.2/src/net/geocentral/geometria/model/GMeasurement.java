/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Vector3d;

import net.geocentral.geometria.action.GEraseLineAction;
import net.geocentral.geometria.action.GEraseSelectionAction;
import net.geocentral.geometria.action.GRemoveFigureAction;
import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.action.GRenamePointAction;
import net.geocentral.geometria.action.GUndoable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GMeasurement implements GRecordable {

    public static final Pattern DISTANCE_PATTERN =
        Pattern.compile("\\s*\\|\\s*("
            + GLabelFactory.LABEL_PATTERN + ")\\s*("
            + GLabelFactory.LABEL_PATTERN + ")\\s*\\|\\s*");

    public static final Pattern ANGLE_PATTERN =
        Pattern.compile("\\s*<\\s*("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*");

    public static final Pattern AREA_PATTERN =
        Pattern.compile("\\s*area\\s*\\[("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*("
            + (GLabelFactory.LABEL_PATTERN) + ")\\s*\\]\\s*");

    public static final Pattern VOLUME_PATTERN =
        Pattern.compile("\\s*volume\\s*");

    private String expression;

    private String figureName;

    private boolean deprecated;

    private GUndoable deprecatingAction;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GMeasurement() {}

    private GMeasurement(String expression, String figureName) {
        logger.info(expression + ", " + figureName);
        this.expression = expression;
        this.figureName = figureName;
    }

    public static GMeasurement newDistance(String[] labels, String figureName) {
        logger.info(Arrays.asList(labels) + ", " + figureName);
        String expression = "|"
            + labels[0] + labels[1] + "|";
        return new GMeasurement(expression, figureName);
    }

    public static GMeasurement newAngle(String[] labels, String figureName) {
        logger.info(Arrays.asList(labels) + ", " + figureName);
        String expression = "<"
            + labels[1] + labels[0] + labels[2];
        return new GMeasurement(expression, figureName);
    }

    public static GMeasurement newArea(String[] labels, String figureName) {
        logger.info(Arrays.asList(labels) + ", " + figureName);
        String expression = "area["
            + labels[0] + labels[1] + labels[2] + "]";
        return new GMeasurement(expression, figureName);
    }

    public static GMeasurement newVolume(String figureName) {
        logger.info(figureName);
        String expression = "volume";
        return new GMeasurement(expression, figureName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        String exp = node.getElementsByTagName("expression").item(0).getTextContent();
        expression = GStringUtils.fromXml(exp);
        figureName =
            node.getElementsByTagName("figureName").item(0).getTextContent();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<measurement>");
        buf.append("\n<expression>");
        buf.append(GStringUtils.toXml(expression));
        buf.append("</expression>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        buf.append("\n</measurement>");
    }

    public String getExpression() {
        return expression;
    }

    public double getValue(GDocument document) throws Exception {
        GFigure figure = document.getFigure(figureName);
        if (figure == null) {
            logger.error("Figure: " + figureName);
            throw new Exception();
        }
        GSolid solid = figure.getSolid();
        Matcher matcher = DISTANCE_PATTERN.matcher(expression);
        if (matcher.matches())
            return getDistance(matcher, solid);
        matcher = ANGLE_PATTERN.matcher(expression);
        if (matcher.matches())
            return getAngle(matcher, solid);
        matcher = AREA_PATTERN.matcher(expression);
        if (matcher.matches())
            return getArea(matcher, solid);
        matcher = VOLUME_PATTERN.matcher(expression);
        if (matcher.matches())
            return solid.computeVolume();
        logger.error(this);
        throw new Exception();
    }

    private double getDistance(Matcher matcher, GSolid solid) throws Exception {
        String[] labels = { matcher.group(1), matcher.group(2) };
        GPoint3d p1 = solid.getPoint(labels[0]);
        GPoint3d p2 = solid.getPoint(labels[1]);
        if (p1 == null || p2 == null) {
            logger.error(this);
            throw new Exception();
        }
        double distance = p1.coords.distance(p2.coords);
        return distance;
    }

    private double getAngle(Matcher matcher, GSolid solid) throws Exception {
        String[] labels = {
                matcher.group(1), matcher.group(2), matcher.group(3) };
        GPoint3d p1 = solid.getPoint(labels[0]);
        GPoint3d p2 = solid.getPoint(labels[1]);
        GPoint3d p3 = solid.getPoint(labels[2]);
        if (p1 == null || p2 == null || p3 == null) {
            logger.error(this);
            throw new Exception();
        }
        Vector3d v1 = new Vector3d(p1.coords);
        v1.sub(p2.coords);
        Vector3d v2 = new Vector3d(p3.coords);
        v2.sub(p2.coords);
        double angle = v1.angle(v2);
        return angle;
    }

    private double getArea(Matcher matcher, GSolid solid) throws Exception {
        String[] labels = {
                matcher.group(1), matcher.group(2), matcher.group(3) };
        Collection<GFace> faces = solid.facesThroughPoints(labels);
        if (faces.size() != 1) {
            logger.error(this);
            throw new Exception();
        }
        double area = faces.iterator().next().computeArea(solid);
        return area;
    }

    public void figureRemoved(GRemoveFigureAction action) {
        if (deprecated)
            return;
        if (action.getFigureName().equals(figureName)) {
            deprecated = true;
            deprecatingAction = action;
            logger.info(this);
        }
    }

    public void removeFigureUndone(GRemoveFigureAction action) {
        if (deprecated && deprecatingAction == action) {
            deprecated = false;
            logger.info(this);
        }
    }

    public void figureRenamed(GRenameFigureAction action) {
        if (action.getOldFigureName().equals(figureName)) {
            logger.info(this);
            figureName = action.getNewFigureName();
            logger.info(figureName);
        }
    }

    public void renameFigureUndone(GRenameFigureAction action) {
        if (action.getNewFigureName().equals(figureName)) {
            logger.info(this);
            figureName = action.getOldFigureName();
            logger.info(figureName);
        }
    }

    public void pointRenamed(GRenamePointAction action) {
        if (deprecated) {
            return;
        }
        if (!action.getFigureName().equals(figureName))
        {
            return;
        }
        String oldLabel = action.getOldLabel();
        String newLabel = action.getNewLabel();
        replaceLabel(oldLabel, newLabel);
    }

    public void renamePointUndone(GRenamePointAction action) {
        if (deprecated) {
            return;
        }
        if (!action.getFigureName().equals(figureName))
        {
            return;
        }
        String oldLabel = action.getOldLabel();
        String newLabel = action.getNewLabel();
        replaceLabel(newLabel, oldLabel);
    }

    private void replaceLabel(String fromLabel, String toLabel) {
        Map<Integer, String> labels = getLabels();
        StringBuffer buf = new StringBuffer();
        int expressionIndex = 0;
        String token;
        for (int labelIndex : labels.keySet())
        {
            if (labelIndex > expressionIndex) {
                token = expression.substring(expressionIndex, labelIndex);
                buf.append(token);
                expressionIndex += token.length();
            }
            token = labels.get(labelIndex);
            if (token.equals(fromLabel)) {
                token = toLabel;
            }
            buf.append(token);
            expressionIndex += token.length();
        }
        if (expressionIndex < expression.length()) {
            token = expression.substring(expressionIndex);
            buf.append(token);
        }
        expression = buf.toString();
    }

    public void lineErased(GEraseLineAction action) {
        if (deprecated) {
            return;
        }
        if (!action.getFigureName().equals(figureName))
        {
            return;
        }
        List<GPoint3d> ps = action.getRemovedPoints();
        pointsRemoved(ps, action);
    }

    public void eraseLineUndone(GEraseLineAction action) {
        if (deprecated && deprecatingAction == action) {
            deprecated = false;
            logger.info(this);
        }
    }

    public void selectionErased(GEraseSelectionAction action) {
        if (deprecated)
            return;
        if (!action.getFigureName().equals(figureName))
        {
            return;
        }
        Collection<GPoint3d> ps = action.getRemovedPoints();
        pointsRemoved(ps, action);
    }

    public void eraseSelectionUndone(GEraseSelectionAction action) {
        if (deprecated && deprecatingAction == action) {
            deprecated = false;
            logger.info(this);
        }
    }

    private Map<Integer, String> getLabels() {
        Map<Integer, String> labels = new LinkedHashMap<Integer, String>();
        Matcher matcher = DISTANCE_PATTERN.matcher(expression);
        if (matcher.matches()) {
            for (int i = 0; i < 2; i++) {
                labels.put(matcher.start(i + 1), matcher.group(i + 1));
            }
            return labels;
        }
        matcher = ANGLE_PATTERN.matcher(expression);
        if (matcher.matches()) {
            for (int i = 0; i < 3; i++) {
                labels.put(matcher.start(i + 1), matcher.group(i + 1));
            }
            return labels;
        }
        matcher = AREA_PATTERN.matcher(expression);
        matcher.matches();
        for (int i = 0; i < 3; i++) {
            labels.put(matcher.start(i + 1), matcher.group(i + 1));
        }
        return labels;
    }

    private void pointsRemoved(Collection<GPoint3d> ps, GUndoable action) {
        for (GPoint3d p : ps) {
            String l = p.getLabel();
            Collection<String> labels = getLabels().values();
            if (labels.contains(l)) {
                deprecated = true;
                deprecatingAction = action;
                logger.info(this);
                return;
            }
        }
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getFigureName() {
        return figureName;
    }

    public String getExpressionString() {
        if (expression.equals("volume"))
            return GDictionary.get("volume");
        if (expression.startsWith("area"))
            return expression.replaceFirst("area", GDictionary.get("area"));
        return expression;
    }
}
