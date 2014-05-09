/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.geocentral.geometria.action.GRenameVariableAction;
import net.geocentral.geometria.evaluator.GEvaluator;
import net.geocentral.geometria.evaluator.GTokenizer;
import net.geocentral.geometria.evaluator.token.GToken;
import net.geocentral.geometria.evaluator.token.GTokenBounds;
import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;

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

    public double getValue(Map<String, GNotepadVariable> variables) throws Exception {
        logger.info(variables + ", " + expression);
        GEvaluator evaluator = new GEvaluator(variables.values());
        try {
            return evaluator.evaluate(expression);
        }
        catch (Exception exception) {
            logger.error(expression);
            throw new Exception(GDictionary.get("BadNotepadRecord", expression));
        }
    }

    public void variableRenamed(GRenameVariableAction action) {
        String oldName = action.getOldName();
        String newName = action.getNewName();
        List<GNotepadVariable> variables = action.getVariables();
        replaceVariableName(oldName, newName, variables);
    }

    public void renameVariableUndone(GRenameVariableAction action) {
        String oldName = action.getOldName();
        String newName = action.getNewName();
        List<GNotepadVariable> variables = action.getVariables();
        replaceVariableName(newName, oldName, variables);
    }

    void replaceVariableName(String fromName, String toName, List<GNotepadVariable> variables) {
        GTokenizer tokenizer = new GTokenizer(variables);
        Map<GToken, GTokenBounds> tokens;
        try {
            tokens = tokenizer.tokenize(expression);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return;
        }
        List<GTokenBounds> tokenBoundList = new ArrayList<GTokenBounds>();
        for (GToken token : tokens.keySet()) {
            if (token instanceof GVariable && ((GVariable)token).getName().equals(fromName)) {
                GTokenBounds tokenBounds = tokens.get(token);
                tokenBoundList.add(tokenBounds);
                Collections.sort(tokenBoundList);
            }
        }
        for (int i = tokenBoundList.size() - 1; i >= 0; i--) {
            GTokenBounds tokenBounds = tokenBoundList.get(i);
            String head = expression.substring(0, tokenBounds.offset);
            String tail = expression.substring(tokenBounds.offset + tokenBounds.size);
            expression = head + toName + tail;
        }
    }

    public String getExpression() {
        return expression;
    }

    public String toString() {
        return expression;
    }
}
