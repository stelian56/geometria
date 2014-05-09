/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;

public class GFigurePopupMenu extends JPopupMenu {

    public GFigurePopupMenu(GFigure figure) {
        init(figure);
    }

    private void init(GFigure figure) {
        Set<String> viewGroup = new LinkedHashSet<String>();
        Set<String> measureGroup = new LinkedHashSet<String>();
        Set<String> drawGroup = new LinkedHashSet<String>();
        Set<String> transformGroup = new LinkedHashSet<String>();
        GSolid solid = figure.getSolid();
        Set<GSelectable> selection = solid.getSelection();
        viewGroup.add("view.toggleSelector");
        viewGroup.add("view.fitToView");
        viewGroup.add("view.initialAttitude");
        Iterator<GSelectable> it = selection.iterator();
        if (selection.size() == 1) {
            GSelectable element = it.next();
            if (element instanceof GPoint3d) {
                String pLabel = ((GPoint3d)element).getLabel();
                Collection<GFace> faces = solid.facesThroughPoint(pLabel);
                if (faces.size() == 1) {
                    measureGroup.add("measure.area");
                }
            }
            else if (element instanceof GStick) {
                transformGroup.add("transform.scale");
                drawGroup.add("draw.drawMidpoint");
                drawGroup.add("draw.divideLine");
                measureGroup.add("measure.distance");
                String p1Label = ((GStick)element).label1;
                String p2Label = ((GStick)element).label2;
                Collection<GFace> faces =
                    solid.facesThroughPoints(new String[] { p1Label, p2Label });
                if (faces.size() == 1)
                    measureGroup.add("measure.area");
            }
            else if (element instanceof GFace)
                measureGroup.add("measure.area");
        }
        else if (selection.size() == 2) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            if (element1 instanceof GPoint3d
                    && element2 instanceof GPoint3d) {
                transformGroup.add("transform.scale");
                String p1Label = ((GPoint3d)element1).getLabel();
                String p2Label = ((GPoint3d)element2).getLabel();
                Collection<GFace> faces =
                    solid.facesThroughPoints(new String[] { p1Label, p2Label });
                if (!faces.isEmpty()) {
                    drawGroup.add("draw.drawLine");
                    measureGroup.add("measure.distance");
                    if (faces.size() == 1)
                        measureGroup.add("measure.area");
                }
            }
            else if (element1 instanceof GStick
                    && element2 instanceof GStick) {
                String[] labels = {
                        ((GStick)element1).label1, ((GStick) element1).label2,
                        ((GStick)element2).label1, ((GStick) element2).label2 };
                Collection<GFace> faces = solid.facesThroughPoints(labels);
                if (faces.isEmpty())
                    transformGroup.add("transform.cut");
                else {
                    if ((((GStick)element1).label1)
                            .equals(((GStick)element2).label1)
                            || (((GStick)element1).label1)
                            .equals(((GStick)element2).label2)
                            || (((GStick)element1).label2)
                            .equals(((GStick)element2).label1)
                            || (((GStick)element1).label2)
                            .equals(((GStick)element2).label2)) {
                        measureGroup.add("measure.angle");
                        drawGroup.add("draw.drawBisector");
                        drawGroup.add("draw.divideAngle");
                    }
                    drawGroup.add("draw.intersectLines");
                    if (faces.size() == 1)
                        measureGroup.add("measure.area");
                }
            }
            else if (element1 instanceof GPoint3d
                    && element2 instanceof GStick
                    || element2 instanceof GPoint3d
                    && element1 instanceof GStick) {
                String label;
                GStick s;
                String[] labels;
                if (element1 instanceof GPoint3d
                        && element2 instanceof GStick) {
                    label = ((GPoint3d)element1).getLabel();
                    s = (GStick)element2;
                    labels = new String[] { label, s.label1, s.label2 };
                }
                else {
                    label = ((GPoint3d) element2).getLabel();
                    s = (GStick) element1;
                    labels = new String[] { label, s.label1, s.label2 };
                }
                Collection<GFace> faces = solid.facesThroughPoints(labels);
                if (faces.isEmpty())
                    transformGroup.add("transform.cut");
                else {
                    drawGroup.add("draw.drawPerpendicular");
                    drawGroup.add("draw.layDistance");
                    if (label.equals(s.label1) || label.equals(s.label2))
                        drawGroup.add("draw.layAngle");
                    if (faces.size() == 1)
                        measureGroup.add("measure.area");
                }
            }
        }
        else if (selection.size() == 3) {
            GSelectable element1 = it.next();
            GSelectable element2 = it.next();
            GSelectable element3 = it.next();
            if (element1 instanceof GPoint3d
                    && element2 instanceof GPoint3d
                    && element3 instanceof GPoint3d) {
                String p1Label = ((GPoint3d)element1).getLabel();
                String p2Label = ((GPoint3d)element2).getLabel();
                String p3Label = ((GPoint3d)element3).getLabel();
                Collection<GFace> faces = solid
                .facesThroughPoints(new String[] { p1Label, p2Label, p3Label });
                if (faces.size() == 1)
                    measureGroup.add("measure.area");
                if (faces.isEmpty())
                    transformGroup.add("transform.cut");
            }
        }
        for (String item : viewGroup)
            addItem(item);
        if (!drawGroup.isEmpty())
            addSeparator();
        for (String item : drawGroup)
            addItem(item);
        if (!measureGroup.isEmpty())
            addSeparator();
        for (String item : measureGroup)
            addItem(item);
        if (!transformGroup.isEmpty())
            addSeparator();
        for (String item : transformGroup)
            addItem(item);
    }

    private void addItem(String actionHandlerName) {
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        AbstractAction actionHandler = documentHandler.getActionHandler(actionHandlerName);
        JMenuItem menuItem = new JMenuItem(actionHandler);
        menuItem.setText(String.valueOf(actionHandler.getValue(AbstractAction.SHORT_DESCRIPTION)));
        add(menuItem);
    }

    private static final long serialVersionUID = 1L;
}
