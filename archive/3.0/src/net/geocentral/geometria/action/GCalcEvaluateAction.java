/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.util.List;

import net.geocentral.geometria.model.GCalculation;
import net.geocentral.geometria.model.GCalculator;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GNotepad;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GMath;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GCalcEvaluateAction implements GLoggable {

    private String variableName;
    
    private String expression;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GCalculator calculator = documentHandler.getCalculator();
        GDocument document = documentHandler.getActiveDocument();
        GNotepadRecord record;
        if (quietMode) {
            List<GNotepadVariable> variables =
                document.getNotepad().getVariables();
            Double value;
            try {
                value = GMath.evaluate(expression, variables);
            }
            catch (Exception exception) {
                return false;
            }
            GCalculation calculation = new GCalculation(expression);
            GNotepadVariable variable =
                new GNotepadVariable(variableName,value);
            record = new GNotepadRecord(variable, calculation);
        }
        else {
            if (calculator.isEmpty())
                return false;
            try {
                record = calculator.evaluate(document);
            }
            catch (Exception exception) {
                GGraphicsFactory.getInstance().
                    showErrorDialog(exception.getMessage());
                return false;
            }
            if (record == null)
                return false;
        }
        GNotepad notepad = document.getNotepad();
        notepad.add(record);
        variableName = record.getVariable().getName();
        expression = record.getExpression();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        logger.info(record);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        GNotepad notepad = document.getNotepad();
        notepad.removeLastRecord();
    }

    public GLoggable clone() {
        GCalcEvaluateAction action = new GCalcEvaluateAction();
        action.variableName = variableName;
        action.expression = expression;
        return action;
    }

    public String getShortDescription() {
        return "calculate " + variableName + "=" + expression;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("variableName");
        if (ns.getLength() == 0) {
            logger.error("No variable name");
            throw new Exception();
        }
        variableName = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("expression");
        if (ns.getLength() == 0) {
            logger.error("No expression");
            throw new Exception();
        }
        expression = ns.item(0).getTextContent();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n<variableName>");
        buf.append(variableName);
        buf.append("</variableName>");
        buf.append("\n<expression>");
        buf.append(expression);
        buf.append("</expression>");
        buf.append("\n</action>");
    }

    public String toLogString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GDictionary.get("Calculate", variableName, expression));
        return String.valueOf(buf);
    }
}
