/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.Map;

import net.geocentral.geometria.evaluator.GEvaluator;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GCalculation implements GRecordable {

    private String expression;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GCalculation() {}

    public GCalculation(String expression) {
        logger.info(expression);
        this.expression = expression;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        expression =
            node.getElementsByTagName("expression").item(0).getTextContent();
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<calculation>");
        buf.append("\n<expression>");
        buf.append(expression);
        buf.append("</expression>");
        buf.append("\n</calculation>");
    }

    public double getValue(Map<String, GNotepadVariable> variables)
            throws Exception {
        logger.info(variables + ", " + expression);
        GEvaluator evaluator = new GEvaluator(variables.values());
        try {
            return evaluator.evaluate(expression);
        }
        catch (Exception exception) {
            logger.error(expression);
            throw new Exception(
                    GDictionary.get("BadNotepadRecord", expression));
        }
    }

    public String getExpression() {
        return expression;
    }

    public String toString() {
        return expression;
    }
}
