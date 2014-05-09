/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLine;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GEraseSelectionAction implements GLoggable, GFigureAction {

    private String figureName;

    private List<String> pointLabels;

    private List<StringPair> stickLabels;

    private GDocument document;

    private GFigure figure;

    private GSolid solid;

    private Map<GFace, List<GLine>> removedLines;

    private Map<GPoint3d, List<GLine>> removedPoints;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info("");
        document = documentHandler.getActiveDocument();
        if (quietMode) {
            try {
                validateApply();
            }
            catch (Exception exception) {
                return false;
            }
        }
        else {
            GFigure figure = document.getSelectedFigure();
            figureName = figure.getName();
            GSolid solid = figure.getSolid();
            Set<GSelectable> selection = solid.getSelection();
            if (selection.isEmpty())
                return false;
            pointLabels = new ArrayList<String>();
            stickLabels = new ArrayList<StringPair>();
            for (GSelectable element : selection) {
                if (element instanceof GStick) {
                    StringPair sp = new StringPair(((GStick)element).label1,
                            ((GStick)element).label2);
                    stickLabels.add(sp);
                }
                else if (element instanceof GPoint3d) {
                    pointLabels.add(((GPoint3d)element).getLabel());
                }
            }
            try {
                validateApply();
            }
            catch (Exception exception) {
                GGraphicsFactory.getInstance().showErrorDialog(exception.getMessage());
                return false;
            }
            solid.clearSelection();
        }
        document.getNotepad().selectionErased(this);
        documentHandler.notepadChanged();
        document.setSelectedFigure(figureName);
        document.getSelectedFigure().repaint();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + pointLabels + ", " + stickLabels);
        return true;
    }

    public void validateApply() throws Exception {
        logger.info("");
        figure = document.getFigure(figureName);
        solid = figure.getSolid();
        removedLines = new LinkedHashMap<GFace, List<GLine>>();
        for (StringPair sp : stickLabels) {
            Collection<GFace> faces = solid.facesThroughPoints(new String[] {
                    sp.s1, sp.s2 });
            if (faces.size() > 1) {
                logger.info("Edge: " + sp);
                throw new Exception(GDictionary.get("CannotEraseEdgeInFigure",
                        sp.s1 + sp.s2, figureName));
            }
            GFace face = faces.iterator().next();
            List<GLine> removedLs = removedLines.get(face);
            if (removedLines.get(face) == null) {
                removedLs = new ArrayList<GLine>();
                removedLines.put(face, removedLs);
            }
            GLine line = face.lineThroughPoints(sp.s1, sp.s2);
            removedLs.add(line);
        }
        removedPoints = new LinkedHashMap<GPoint3d, List<GLine>>();
        for (String s : pointLabels) {
            GPoint3d p = solid.getPoint(s);
            removedPoints.put(p, null);
        }
        for (String label : pointLabels) {
            Collection<GFace> faces = solid.facesThroughPoint(label);
            for (GFace face : faces) {
                List<GLine> lines = face.linesThroughPoint(label);
                List<GLine> removedLs = removedLines.get(face);
                if (removedLs != null)
                    lines.removeAll(removedLs);
                for (GLine line : lines) {
                    if (line.firstLabel().equals(label)
                            || line.lastLabel().equals(label)) {
                        logger.info("End of line: " + label);
                        throw new Exception(GDictionary.get(
                            "CannotEraseEndOfLineInFigure", label, figureName));
                    }
                }
            }
        }
        // Apply
        Set<GPoint3d> danglingPoints = new LinkedHashSet<GPoint3d>();
        for (GFace face : removedLines.keySet()) {
            List<GLine> removedLs = removedLines.get(face);
            for (GLine line : removedLs) {
                List<String> danglingLabels = new ArrayList<String>();
                face.removeLine(line, danglingLabels);
                for (String label : danglingLabels)
                    danglingPoints.add(solid.getPoint(label));
            }
        }
        for (GPoint3d p : removedPoints.keySet()) {
            List<GLine> affectedLines = new ArrayList<GLine>();
            Collection<GFace> faces = solid.facesThroughPoint(p.getLabel());
            for (GFace face : faces) {
                List<GLine> lines = face.linesThroughPoint(p.getLabel());
                if (lines.isEmpty())
                    continue;
                List<GLine> removedLs = removedLines.get(face);
                if (removedLs != null)
                    lines.removeAll(removedLs);
                affectedLines.addAll(lines);
            }
            if (!affectedLines.isEmpty()) {
                for (GLine line : affectedLines)
                    line.remove(p.getLabel());
                removedPoints.put(p, affectedLines);
            }
            solid.removePoint(p.getLabel());
        }
        for (GPoint3d p : danglingPoints) {
            solid.removePoint(p.getLabel());
            removedPoints.put(p, null);
        }
        solid.makeConfig();
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid.clearSelection();
        for (GPoint3d p : removedPoints.keySet())
            solid.undoRemovePoint(p);
        for (GFace face : removedLines.keySet()) {
            List<GLine> removedLs = removedLines.get(face);
            for (GLine line : removedLs)
                face.addLine(line);
        }
        for (GPoint3d p : removedPoints.keySet()) {
            List<GLine> affectedLines = removedPoints.get(p);
            if (affectedLines != null) {
                for (GLine line : affectedLines)
                    line.insert(p, solid);
            }
        }
        document.getNotepad().eraseSelectionUndone(this);
        documentHandler.notepadChanged();
        solid.makeConfig();
        figure.repaint();
        document.setSelectedFigure(figureName);
        logger.info(figureName + ", " + pointLabels + ", " + stickLabels);
    }

    public GLoggable clone() {
        GEraseSelectionAction action = new GEraseSelectionAction();
        action.figureName = figureName;
        action.pointLabels = new ArrayList<String>();
        for (String label : pointLabels)
            action.pointLabels.add(label);
        action.stickLabels = new ArrayList<StringPair>();
        for (StringPair sp : stickLabels)
            action.stickLabels.add(new StringPair(sp.s1, sp.s2));
        return action;
    }

    public String toLogString() {
        return GDictionary.get("EraseSelectedElementsInFigure", figureName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("pLabel");
        pointLabels = new ArrayList<String>();
        for (int i = 0; i < ns.getLength(); i++) {
            String label = ns.item(i).getTextContent();
            pointLabels.add(label);
        }
        NodeList nns = node.getElementsByTagName("sLabels");
        stickLabels = new ArrayList<StringPair>();
        for (int i = 0; i < nns.getLength(); i++) {
            String value = nns.item(i).getTextContent();
            String[] tokens = value.split(" ");
             if (tokens.length != 2) {
                 logger.error(Arrays.asList(tokens));
                 throw new Exception();
             }
            StringPair sp = new StringPair(tokens[0], tokens[1]);
            stickLabels.add(sp);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<figureName>");
        buf.append(figureName);
        buf.append("</figureName>");
        for (String label : pointLabels) {
            buf.append("\n<pLabel>");
            buf.append(label);
            buf.append("</pLabel>");
        }
        for (StringPair sp : stickLabels) {
            buf.append("\n<sLabels>");
            buf.append(sp.s1);
            buf.append(" ");
            buf.append(sp.s2);
            buf.append("</sLabels>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("eraseSelection");
    }

    public String getFigureName() {
        return figureName;
    }

    public Collection<GPoint3d> getRemovedPoints() {
        return removedPoints.keySet();
    }

    class StringPair {

        public String s1;

        public String s2;

        public StringPair(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        public String toString() {
            return s1 + ", " + s2;
        }
    }
}
