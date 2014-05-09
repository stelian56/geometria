/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.vecmath.Matrix3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GNoMoreJoinMatchesException;
import net.geocentral.geometria.util.GUndefinedJoinException;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;
import net.geocentral.geometria.view.GJoinDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GJoinAction implements GLoggable, GFigureAction, GActionWithHelp {

    private GDocument document;

    private String figure1Name;

    private String figure2Name;

    private String jointFigureName;

    private String[] matchLabels1;

    private String[] matchLabels2;

    private GSolid solid1;

    private GSolid solid2;

    private String helpId;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        document = documentHandler.getActiveDocument();
        if (quietMode) {
            try {
                validateApply(null);
            }
            catch (Exception exception) {
            	exception.printStackTrace();
            	return false;
            }
        }
        else {
            String[] figureNames = document.getFigureNames();
            GJoinDialog dialog = new GJoinDialog(documentHandler.getOwnerFrame(), this, figureNames);
            String figureName = document.getSelectedFigure().getName();
            dialog.prefill(figureName);
            dialog.setVisible(true);
            if (!dialog.getResult())
                return false;
        }
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figure1Name + ", " + figure2Name + ", "
            + Arrays.asList(matchLabels1) + ", " + Arrays.asList(matchLabels2)
            + ", " + jointFigureName);
        return true;
    }

    public void setFigureNames(String figure1Name, String figure2Name) {
        logger.info(figure1Name + ", " + figure2Name);
        this.figure1Name = figure1Name;
        this.figure2Name = figure2Name;
    }

    public void setMatchLabels(String[] labels1, String[] labels2) {
        logger.info(Arrays.asList(labels1) + ", " + Arrays.asList(labels2));
        this.matchLabels1 = labels1;
        this.matchLabels2 = labels2;
    }

    public void validateApply(GJoinDialog dialog) throws Exception {
        logger.info("");
        GFace face1 = null;
        GFace face2 = null;
        int matchIndex = -1;
        Map<GFace, Map<GFace, List<Integer>>> joinMatches = null;
        if (matchLabels1 == null) {
            GFigure figure = document.getFigure(figure1Name);
            solid1 = figure.getSolid();
            figure = document.getFigure(figure2Name);
            solid2 = figure.getSolid();
            joinMatches = solid1.getJoinMatches(solid2);
            if (joinMatches.isEmpty()) {
                logger.info(
                    "Cannot be joined: " + figure1Name + ", " + figure2Name);
                throw new Exception(GDictionary.get("FiguresCannotBeJoined",
                    figure1Name, figure2Name));
            }
            if (joinMatches.keySet().size() == 1) {
                face1 = joinMatches.keySet().iterator().next();
                Map<GFace, List<Integer>> face1Matches = joinMatches.get(face1);
                if (face1Matches.keySet().size() == 1) {
                    face2 = face1Matches.keySet().iterator().next();
                    List<Integer> matchIndexes = face1Matches.get(face2);
                    if (matchIndexes.size() == 1)
                        matchIndex = matchIndexes.get(0);
                }
            }
            Set<GSelectable> selection = solid1.getSelection();
            Set<GFace> faces1 = new LinkedHashSet<GFace>();
            Set<GPoint3d> ps1 = new LinkedHashSet<GPoint3d>();
            for (GSelectable element : selection) {
                if (element instanceof GFace)
                    faces1.add((GFace)element);
                else if (element instanceof GPoint3d)
                    ps1.add((GPoint3d)element);
            }
            if (faces1.size() > 1) {
                logger.info("More than 1 face selected: " + figure1Name);
                throw new Exception(GDictionary.get(
                        "MoreThanOneFaceSelected", figure1Name));
            }
            if (ps1.size() > 1) {
                logger.info(
                        "More than 1 reference point selected: " + figure1Name);
                throw new Exception(GDictionary.get(
                        "MoreThanOneRefPointSelected", figure1Name));
            }
            GFace selectedFace1 = faces1.isEmpty() ? null
                    : faces1.iterator().next();
            GPoint3d selectedPoint1 = ps1.isEmpty() ? null
                    : ps1.iterator().next();
            int selectedIndex1 = -1;
            if (selectedPoint1 != null) {
                if (!selectedPoint1.isVertex()) {
                    logger.info(
                        "Not vertex: " + selectedPoint1 + ", " + figure1Name);
                    throw new Exception(
                        GDictionary.get("SelectedPointIsNotVertex",
                            selectedPoint1.getLabel(), figure1Name));
                }
                if (selectedFace1 != null) {
                    for (int i = 0; i < selectedFace1.sideCount(); i++) {
                        if (selectedFace1.labelAt(i).equals(
                                selectedPoint1.getLabel())) {
                            selectedIndex1 = i;
                            break;
                        }
                    }
                    if (selectedIndex1 < 0) {
                        logger.info("Not in selected face: " + selectedPoint1
                                + ", " + selectedFace1 + ", " + figure1Name);
                        throw new Exception(
                            GDictionary.get("SelectedPointNotInSelectedFace",
                                selectedPoint1.getLabel(), figure1Name));
                    }
                }
            }
            selection = solid2.getSelection();
            Set<GFace> faces2 = new LinkedHashSet<GFace>();
            Set<GPoint3d> ps2 = new LinkedHashSet<GPoint3d>();
            for (GSelectable element : selection) {
                if (element instanceof GFace)
                    faces2.add((GFace)element);
                else if (element instanceof GPoint3d)
                    ps2.add((GPoint3d)element);
            }
            if (faces2.size() > 1) {
                logger.info("More than 1 face selected: " + figure2Name);
                throw new Exception(GDictionary.get(
                        "MoreThanOneFaceSelected", figure2Name));
            }
            if (ps2.size() > 1) {
                logger.info(
                        "More than 1 reference point selected: " + figure2Name);
                throw new Exception(GDictionary.get(
                        "MoreThanOneRefPointSelected", figure2Name));
            }
            GFace selectedFace2 = faces2.isEmpty() ? null
                    : faces2.iterator().next();
            GPoint3d selectedPoint2 = ps2.isEmpty() ? null
                    : ps2.iterator().next();
            int selectedIndex2 = -1;
            if (selectedPoint2 != null) {
                if (!selectedPoint2.isVertex()) {
                    logger.info(
                        "Not vertex: " + selectedPoint2 + ", " + figure2Name);
                    throw new Exception(
                        GDictionary.get("SelectedPointIsNotVertex",
                                selectedPoint2.getLabel(), figure2Name));
                }
                if (selectedFace2 != null) {
                    for (int i = 0; i < selectedFace2.sideCount(); i++) {
                        if (selectedFace2.labelAt(i).equals(
                                selectedPoint2.getLabel())) {
                            selectedIndex2 = i;
                            break;
                        }
                    }
                    if (selectedIndex2 < 0) {
                        logger.info("Not in selected face: " + selectedPoint2
                                + ", " + selectedFace2 + ", " + figure2Name);
                        throw new Exception(
                            GDictionary.get("SelectedPointNotInSelectedFace",
                                selectedPoint2.getLabel(), figure2Name));
                    }
                }
            }
            if (selectedFace1 != null) {
                joinMatches.keySet().retainAll(faces1);
                if (joinMatches.isEmpty()) {
                    logger.info("Cannot be joined at face: " + selectedFace1
                            + ", " + figure1Name);
                    throw new Exception(GDictionary.get(
                        "FiguresCannotBeJoinedAtSelectedFaces",
                        figure1Name, figure2Name));
                }
            }
            if (selectedFace2 != null) {
                Set<GFace> unmatchedFaces1 = new LinkedHashSet<GFace>();
                for (GFace f1 : joinMatches.keySet()) {
                    Map<GFace, List<Integer>> f1Matches = joinMatches.get(f1);
                    f1Matches.keySet().retainAll(faces2);
                    if (f1Matches.isEmpty())
                        unmatchedFaces1.add(f1);
                }
                joinMatches.keySet().removeAll(unmatchedFaces1);
                if (joinMatches.isEmpty()) {
                    logger.info("Cannot be joined at faces: " + selectedFace1
                            + ", " + figure1Name + ", " + selectedFace2 + ", "
                            + figure2Name);
                    throw new Exception(GDictionary.get(
                            "FiguresCannotBeJoinedAtSelectedFaces",
                            figure1Name, figure2Name));
                }
            }
            if (selectedIndex1 >= 0
                    && selectedIndex2 >= 0) {
                List<Integer> matchIndexes =
                    joinMatches.get(selectedFace1).get(selectedFace2);
                int sc = selectedFace1.sideCount();
                int selectedMatchIndex;
                boolean flipFace2 = selectedFace1.getOrientation(solid1,
                        solid1.getGCenter()) == selectedFace2.getOrientation(
                                solid2, solid2.getGCenter());
                if (flipFace2)
                    selectedMatchIndex = (selectedIndex1 + selectedIndex2) % sc;
                else
                    selectedMatchIndex =
                        (selectedIndex1 - selectedIndex2 + sc) % sc;
                if (!matchIndexes.contains(selectedMatchIndex)) {
                    logger.info("Cannot be joined at points: " + matchIndexes
                            + ", " + selectedMatchIndex);
                    throw new Exception(GDictionary.get(
                            "FiguresCannotBeJoinedAtSelectedPoints",
                            figure1Name, figure2Name));
                }
                matchIndex = selectedMatchIndex;
                face1 = selectedFace1;
                face2 = selectedFace2;
            }
            else if (matchIndex < 0) {
                int option = GGraphicsFactory.getInstance().showQuestionDialog( GDictionary.get("JoinAnyMatchingFaces"),
                        JOptionPane.YES_NO_OPTION, LocationType.TOP_LEFT);
                logger.info(option);
                if (option != JOptionPane.YES_OPTION) {
                    GGraphicsFactory.getInstance().showMessageDialog(
                        GDictionary.get("SelectFaceRefPoint"));
                    throw new GUndefinedJoinException();
                }
            }
        }
        else {
            GFigure figure = document.getFigure(figure1Name);
            solid1 = figure.getSolid();
            figure = document.getFigure(figure2Name);
            solid2 = figure.getSolid();
            face1 = solid1.getFace(matchLabels1);
            face2 = solid2.getFace(matchLabels2);
            int index1 = -1;
            for (int i = 0; i < face1.sideCount(); i++) {
                if (face1.labelAt(i).equals(matchLabels1[0])) {
                    index1 = i;
                    break;
                }
            }
            if (index1 == -1) {
                logger.info("No point: " + matchLabels1[0] + ", "
                        + figure1Name);
                throw new Exception(
                        GDictionary.get("FigureContainsNoPoint", figure1Name, 
                        matchLabels1[0]));
            }
            int index2 = -1;
            for (int i = 0; i < face2.sideCount(); i++) {
                if (face2.labelAt(i).equals(matchLabels2[0])) {
                    index2 = i;
                    break;
                }
            }
            if (index2 == -1) {
                logger.info("No point: " + matchLabels2[0] + ", "
                        + figure2Name);
                throw new Exception(
                        GDictionary.get("FigureContainsNoPoint", figure2Name, 
                        matchLabels2[0]));
            }
            int sc = face1.sideCount();
            boolean flipFace2 = face1.getOrientation(solid1,
                solid1.getGCenter()) == face2.getOrientation(solid2,
                        solid2.getGCenter());
            if (flipFace2)
                matchIndex = (index1 + index2) % sc;
            else
                matchIndex = (index1 - index2 + sc) % sc;
        }

        GSolid solid = null;
        if (matchIndex >= 0) {
            solid = solid1.join(solid2, face1, face2, matchIndex);
            if (solid == null) {
                logger.info("Not convex: " + figure1Name + ", " + figure1Name);
                throw new Exception(
                    GDictionary.get("FiguresCannotBeJoinedNotConvex", figure1Name, figure2Name));
            }
            showMatch(face1, face2, matchIndex, solid);
        }
        else {
            List<GSolid> solids = new ArrayList<GSolid>();
        	for (GFace f1 : joinMatches.keySet()) {
                Map<GFace, List<Integer>> f1Matches = joinMatches.get(f1);
                for (GFace f2 : f1Matches.keySet()) {
                    List<Integer> matchIndexes = f1Matches.get(f2);
                    for (int mIndex : matchIndexes) {
                        solid = solid1.join(solid2, f1, f2, mIndex);
                        if (solid == null)
                            continue;
                        boolean duplicate = false;
                        for (GSolid s : solids) {
                            if (solid.isSimilar(s)) {
                                duplicate = true;
                                solid = null;
                                break;
                            }
                        }
                        if (duplicate)
                            continue;
                        solids.add(solid);
                        face1 = f1;
                        face2 = f2;
                        matchIndex = mIndex;
                        showMatch(face1, face2, matchIndex, solid);
                        int option = GGraphicsFactory.getInstance().showQuestionDialog(
                                GDictionary.get("KeepThisResult"), JOptionPane.YES_NO_CANCEL_OPTION,
                                LocationType.TOP_LEFT);
                        if (option == JOptionPane.YES_OPTION)
                            return;
                        GDocumentHandler documentHandler =
                            GDocumentHandler.getInstance();
                        undo(documentHandler);
                        solid = null;
                        if (option == JOptionPane.NO_OPTION)
                            continue;
                        if (option == JOptionPane.CANCEL_OPTION)
                            throw new GUndefinedJoinException();
                    }
                }
            }
        	if (solid == null) {
                logger.info("No more matches");
                GGraphicsFactory.getInstance().showErrorDialog(
                        GDictionary.get("NoMoreMatches"));
                throw new GNoMoreJoinMatchesException();
        	}
        }
    }

    void showMatch(GFace face1, GFace face2, int matchIndex, GSolid solid) {
        matchLabels1 = new String[3];
        matchLabels2 = new String[3];
        int sc = face1.sideCount();
        boolean flipFace2 = face1.getOrientation(solid1, solid1.getGCenter())
            == face2.getOrientation(solid2, solid2.getGCenter());
        for (int i = 0; i < 3; i++) {
            matchLabels1[i] = face1.labelAt((matchIndex + i) % sc);
            if (flipFace2)
                matchLabels2[i] = face2.labelAt((sc - i) % sc);
            else
                matchLabels2[i] = face2.labelAt(i);
        }
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GFigure figure1 = document.getFigure(figure1Name);
        GFigure jointFigure = documentHandler.newFigure(solid);
        jointFigure.setTransparent(figure1.isTransparent());
        jointFigure.setLabelled(figure1.isLabelled());
        Matrix3d attitude = new Matrix3d(figure1.getCamera().getAttitude());
        jointFigure.getCamera().setAttitude(attitude);
        Color baseColor = figure1.getBaseColor();
        jointFigure.setBaseColor(new Color(baseColor.getRGB()));
        jointFigureName = jointFigure.getName();
    }
    
    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        solid1.clearSelection();
        solid2.clearSelection();
        documentHandler.removeFigure(jointFigureName);
        document.removeFigure(jointFigureName);
        document.setSelectedFigure(figure1Name);
        logger.info("Join figures " + figure1Name + ", " + figure2Name
                + " into figure " + jointFigureName + " undone");
        logger.info(figure1Name + ", " + figure2Name + ", "
            + Arrays.asList(matchLabels1) + ", " + Arrays.asList(matchLabels2)
            + jointFigureName);
    }

    public GLoggable clone() {
        GJoinAction action = new GJoinAction();
        action.figure1Name = figure1Name;
        action.figure2Name = figure2Name;
        action.jointFigureName = jointFigureName;
        action.matchLabels1 = matchLabels1.clone();
        action.matchLabels2 = matchLabels2.clone();
        return action;
    }

    public String toLogString() {
        return GDictionary.get("JoinFiguresAtPoints", figure1Name, figure2Name,
                matchLabels1[0], matchLabels1[1], matchLabels1[2],
                matchLabels2[0], matchLabels2[1], matchLabels2[2]);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figure1Name");
        if (ns.getLength() == 0) {
            logger.error("No figure1 name");
            throw new Exception();
        }
        figure1Name = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("figure2Name");
        if (ns.getLength() == 0) {
            logger.error("No figure2 name");
            throw new Exception();
        }
        figure2Name = ns.item(0).getTextContent();
        matchLabels1 = new String[3];
        matchLabels2 = new String[3];
        ns = node.getElementsByTagName("matchLabel10");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel10");
            throw new Exception();
        }
        matchLabels1[0] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("matchLabel11");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel11");
            throw new Exception();
        }
        matchLabels1[1] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("matchLabel12");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel12");
            throw new Exception();
        }
        matchLabels1[2] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("matchLabel20");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel20");
            throw new Exception();
        }
        matchLabels2[0] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("matchLabel21");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel21");
            throw new Exception();
        }
        matchLabels2[1] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("matchLabel22");
        if (ns.getLength() == 0) {
            logger.error("No matchLabel22");
            throw new Exception();
        }
        matchLabels2[2] = ns.item(0).getTextContent();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<figure1Name>");
        buf.append(figure1Name);
        buf.append("</figure1Name>");
        buf.append("\n<figure2Name>");
        buf.append(figure2Name);
        buf.append("</figure2Name>");
        buf.append("\n<matchLabel10>");
        buf.append(matchLabels1[0]);
        buf.append("</matchLabel10>");
        buf.append("\n<matchLabel11>");
        buf.append(matchLabels1[1]);
        buf.append("</matchLabel11>");
        buf.append("\n<matchLabel12>");
        buf.append(matchLabels1[2]);
        buf.append("</matchLabel12>");
        buf.append("\n<matchLabel20>");
        buf.append(matchLabels2[0]);
        buf.append("</matchLabel20>");
        buf.append("\n<matchLabel21>");
        buf.append(matchLabels2[1]);
        buf.append("</matchLabel21>");
        buf.append("\n<matchLabel22>");
        buf.append(matchLabels2[2]);
        buf.append("</matchLabel22>");
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("joinFigures", figure1Name, figure2Name);
    }

    public String getFigureName() {
        return jointFigureName;
    }
    
    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
