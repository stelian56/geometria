/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFace;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.model.GMeasurement;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSelectable;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GStick;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.view.GAreaDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GAreaAction implements GLoggable, GFigureAction, GActionWithHelp {

	private String figureName;

	private String[] fLabels;

	private String variableName;

    private GSolid solid;

    private String helpId;
    
	private static Logger logger = Logger.getLogger("net.geocentral.geometria");

	public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
		GDocument document = documentHandler.getActiveDocument();
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
			solid = figure.getSolid();
			Set<GSelectable> selection = solid.getSelection();
			prefill(selection);
			String[] fLabelStrings = new String[solid.faceCount()];
			for (int i = 0; i < solid.faceCount(); i++) {
				GFace face = solid.faceAt(i);
				StringBuffer buf = new StringBuffer();
				buf.append(face.labelAt(0)).append(face.labelAt(1)).append(
						face.labelAt(2));
				fLabelStrings[i] = String.valueOf(buf);
			}
			GAreaDialog dialog = new GAreaDialog(
                    documentHandler.getOwnerFrame(), this, fLabelStrings);
			if (fLabels != null)
				dialog.prefill(fLabels[0] + fLabels[1] + fLabels[2]);
			dialog.setVisible(true);
			if (!dialog.getResult())
				return false;
			solid.clearSelection();
		}
		document.setSelectedFigure(figureName);
		document.getSelectedFigure().repaint();
		if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(figureName + ", " + Arrays.asList(fLabels));
		return true;
	}

	private void prefill(Set<GSelectable> selection) {
        logger.info(selection);
		if (selection.isEmpty())
			return;
		GDocument document = GDocumentHandler.getInstance().getActiveDocument();
		GFigure figure = document.getSelectedFigure();
		figureName = figure.getName();
		GSolid solid = figure.getSolid();
		Iterator<GSelectable> it = selection.iterator();
		Set<String> labelSet = new LinkedHashSet<String>();
		Set<GFace> faces = new LinkedHashSet<GFace>();
		while (it.hasNext()) {
			GSelectable element = it.next();
			if (element instanceof GPoint3d)
				labelSet.add(((GPoint3d)element).getLabel());
			else if (element instanceof GStick) {
				labelSet.add(((GStick)element).label1);
				labelSet.add(((GStick)element).label2);
			}
            else if (element instanceof GFace)
				faces.add((GFace)element);
		}
		String[] pLabels = new String[labelSet.size()];
		labelSet.toArray(pLabels);
		Collection<GFace> fs = solid.facesThroughPoints(pLabels);
		faces.addAll(fs);
		if (faces.size() != 1)
			return;
		GFace face = faces.iterator().next();
		fLabels = new String[3];
		fLabels[0] = face.labelAt(0);
		fLabels[1] = face.labelAt(1);
		fLabels[2] = face.labelAt(2);
	}

	public void validateApply() throws Exception {
		logger.info("");
		GDocumentHandler documentHandler = GDocumentHandler.getInstance();
		GDocument document = documentHandler.getActiveDocument();
		if (variableName.length() == 0) {
            logger.info("No variable");
			throw new Exception(GDictionary.get("EnterVariable"));
        }
        if (Arrays.asList(GVariable.RESERVED).contains(
                variableName.toLowerCase())) {
            logger.info("Reserved variable: " + variableName);
            throw new Exception(GDictionary.get("ReservedVariable",
                    variableName));
        }
        if (!variableName.matches(GLabelFactory.VARIABLE_NAME_PATTERN)) {
            logger.info("Bad variable: " + variableName);
            throw new Exception(
                    GDictionary.get("InvalidVariable", variableName));
        }
		if (document.getVariable(variableName) != null) {
            logger.info("Duplicate variable: " + variableName);
			throw new Exception(
                    GDictionary.get("DuplicateVariable", variableName));
        }
		GFigure figure = document.getFigure(figureName);
		solid = figure.getSolid();
		GFace face = solid.facesThroughPoints(fLabels).iterator().next();
		double area = face.computeArea(solid);
		GMeasurement expression = GMeasurement.newArea(fLabels, figureName);
		GNotepadVariable variable = new GNotepadVariable(variableName, area);
		GNotepadRecord record = new GNotepadRecord(variable, expression);
        GNotepad notepad = document.getNotepad();
		notepad.add(record);
	}

	public void undo(GDocumentHandler documentHandler) {
	    logger.info("");
        solid.clearSelection();
		GDocument document = documentHandler.getActiveDocument();
		GNotepad notepad = document.getNotepad();
		notepad.removeLastRecord();
		logger.info(figureName);
	}

	public GLoggable clone() {
		GAreaAction action = new GAreaAction();
		action.figureName = figureName;
		action.fLabels = fLabels;
		for (int i = 0; i < 3; i++)
			action.fLabels[i] = fLabels[i];
		action.variableName = variableName;
		return action;
	}

	public String toLogString() {
		return GDictionary.get("MeasureAreaOfFaceInFigure", variableName,
                fLabels[0] + fLabels[1] + fLabels[2], figureName);
	}

	public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("figureName");
        if (ns.getLength() == 0) {
            logger.error("No figure name");
            throw new Exception();
        }
        figureName = ns.item(0).getTextContent();
        fLabels = new String[3];
        ns = node.getElementsByTagName("fLabel1");
        if (ns.getLength() == 0) {
            logger.error("No fLabel1");
            throw new Exception();
        }
        fLabels[0] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("fLabel2");
        if (ns.getLength() == 0) {
            logger.error("No fLabel2");
            throw new Exception();
        }
        fLabels[1] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("fLabel3");
        if (ns.getLength() == 0) {
            logger.error("No fLabel3");
            throw new Exception();
        }
        fLabels[2] = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("variableName");
        if (ns.getLength() == 0) {
            logger.error("No variable name");
            throw new Exception();
        }
        variableName = ns.item(0).getTextContent();
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
		buf.append("\n<fLabel1>");
		buf.append(fLabels[0]);
		buf.append("</fLabel1>");
		buf.append("\n<fLabel2>");
		buf.append(fLabels[1]);
		buf.append("</fLabel2>");
		buf.append("\n<fLabel3>");
		buf.append(fLabels[2]);
		buf.append("</fLabel3>");
		buf.append("\n<variableName>");
		buf.append(variableName);
		buf.append("</variableName>");
		buf.append("\n</action>");
	}

	public void setInput(String fLabelsString, String recordName) {
        logger.info(fLabelsString + ", " + recordName);
		this.variableName = recordName;
		fLabels = new String[3];
		StringBuffer buf = new StringBuffer();
		buf.append("(").append(GLabelFactory.LABEL_PATTERN).append(")").append(
				"(").append(GLabelFactory.LABEL_PATTERN).append(")")
				.append("(").append(GLabelFactory.LABEL_PATTERN).append(")");
		Pattern pattern = Pattern.compile(String.valueOf(buf));
		Matcher matcher = pattern.matcher(fLabelsString);
		matcher.matches();
		for (int i = 0; i < 3; i++)
			fLabels[i] = matcher.group(i + 1);
	}

	public String getShortDescription() {
		return GDictionary.get("measureAreaOfFace",
		        fLabels[0] + fLabels[1] + fLabels[2]);
	}

    public String getFigureName() {
        return figureName;
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
