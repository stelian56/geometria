/**
 * Copyright 2000-2013 Geometria Contributors
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
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GCalcEvaluateAction implements GLoggable {

    private String variableName;
    
    private String expression;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GCalculator calculator = documentHandler.getCalculator();
        GDocument document = documentHandler.getActiveDocument();
        GNotepadRecord record;
        if (silent) {
            List<GNotepadVariable> variables = document.getNotepad().getVariables();
            Double value;
            try {
                value = GMath.evaluate(expression, variables);
            }
            catch (Exception exception) {
                return false;
            }
            GCalculation calculation = new GCalculation(expression);
            GNotepadVariable variable = new GNotepadVariable(variableName,value);
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
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
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

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public String toLogString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GDictionary.get("Calculate", variableName, expression));
        return String.valueOf(buf);
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
        ns = node.getElementsByTagName("comments");
        if (ns.getLength() > 0) {
            String s = ns.item(0).getTextContent();
            comments = GStringUtils.fromXml(s);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>")
            .append("\n<className>")
            .append(this.getClass().getSimpleName())
            .append("</className>")
            .append("\n<variableName>")
            .append(variableName)
            .append("</variableName>")
            .append("\n<expression>")
            .append(expression)
            .append("</expression>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }
}
