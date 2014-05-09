/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import java.util.List;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GNotepadVariable;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GNumberAnswer implements GAnswer {

    private double value;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
    public GNumberAnswer() {
        value = 0;
    }

    public GNumberAnswer(double value) {
        logger.info(value);
        this.value = value;
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("value");
        if (ns.getLength() != 1) {
            logger.error(ns.getLength());
            throw new Exception();
        }
        String valueString = ns.item(0).getTextContent();
        if (document.isLocked()) {
            String decoded = GStringUtils.decode(valueString);
            if (decoded == null) {
                logger.error(String.format("Unencoded number answer %s in locked problem", valueString));
                throw new Exception();
            }
            valueString = decoded;
        }
        if (valueString.length() > 0) {
            try {
                value = Double.parseDouble(valueString);
            }
            catch (Exception exception) {
                logger.error(valueString);
                throw new Exception();
            }
        }
    }
    
    public void validateInput(String inputString, GDocument document)
            throws Exception {
        logger.info(inputString);
        if (inputString.length() == 0)
            throw new Exception(GDictionary.get("EnterValidExpression"));
        GNotepadVariable variable = document.getVariable(inputString);
        if (variable != null)
            value = variable.getValue();
        else {
            List<GNotepadVariable> variables =
                    document.getNotepad().getVariables();
            Double v = GMath.evaluate(inputString, variables);
            if (v == null) {
                logger.info("Bad expression: " + inputString);
                throw new Exception(GDictionary.get("EnterValidExpression"));
            }
            value = v;
        }
    }

    public boolean validate(String valueString, String figureName,
            GDocument document) {
        logger.info(valueString);
        List<GNotepadVariable> variables = document.getNotepad().getVariables();
        Double v = GMath.evaluate(valueString, variables);
        if (v == null)
            return false;
        return Math.abs(value - v) < GMath.EPSILON;
    }

    public boolean verify(GNumberAnswer answer, double epsilon) {
        logger.info(answer.value + ", " + epsilon);
        return Math.abs(value - answer.value) < epsilon;
    }

    public void serialize(StringBuffer buf, boolean lock) {
        logger.info("");
        String stringValue = String.valueOf(value);
        if (lock) {
            stringValue = GStringUtils.encode(stringValue);
        }
        buf.append("\n<answer>");
        buf.append("\n<type>number</type>");
        buf.append("\n<value>");
        buf.append(String.valueOf(stringValue));
        buf.append("</value>");
        buf.append("\n</answer>");
    }

    public Double getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}
