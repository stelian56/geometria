/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JTextField;

import net.geocentral.geometria.evaluator.GEvaluator;
import net.geocentral.geometria.evaluator.token.GToken;
import net.geocentral.geometria.evaluator.token.GTokenBounds;
import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GCalculator extends KeyAdapter {

    private JTextField textField;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GNotepadRecord evaluate(GDocument document) throws Exception {
        logger.info("");
        List<GNotepadVariable> variables = null;
        if (document != null) {
            variables = document.getNotepad().getVariables();
        }
        String input = textField.getText();
        int expressionOffset = 0;
        String variableName = null;
        String expression = null;
        String[] ts = input.split("=");
        if (ts.length > 2) {
            int pos = input.lastIndexOf('=');
            select(pos, pos + 1);
            logger.info(input + ", " + pos);
            throw new Exception(GDictionary.get("MisplacedEqualitySign"));
        }
        if (ts.length == 2) {
            expression = ts[1];
            expressionOffset = expression.indexOf('=') + 1;
            variableName = ts[0].trim();
            if (!variableName.matches(GLabelFactory.VARIABLE_NAME_PATTERN)) {
                logger.info(String.format("Bad variable: %s, %s", input, variableName));
                throw new Exception(GDictionary.get("InvalidVariable", variableName));
            }
            if (variables != null) {
                for (GNotepadVariable p : variables) {
                    if (p.getName().equals(variableName)) {
                        logger.info(String.format("Duplicate variable: %s, %s", input, variableName));
                        throw new Exception(GDictionary.get("DuplicateVariable", variableName));
                    }
                }
            }
        }
        else {
            expression = input;
        }
        GEvaluator evaluator = new GEvaluator(variables);
        double value;
        try {
            value = evaluator.evaluate(expression);
        }
        catch (Exception exception) {
            GTokenBounds tokenBounds = evaluator.getErrorTokenBounds();
            select(expressionOffset + tokenBounds.offset, expressionOffset + tokenBounds.offset + tokenBounds.size);
            throw exception;
        }
        logger.info("Value: " + value);
        if (document != null && variableName != null) {
            List<GToken> tokens = evaluator.getPostfixTokens();
            Set<GNotepadVariable> params = new LinkedHashSet<GNotepadVariable>();
            for (GToken token : tokens) {
                if (token instanceof GVariable) {
                    GNotepadVariable param = document.getVariable(((GVariable)token).getName());
                    params.add(param);
                }
            }
            expression = expression.replaceAll(" +", "");
            GCalculation calculation = new GCalculation(expression);
            GNotepadVariable variable = new GNotepadVariable(variableName, value);
            GNotepadRecord record = new GNotepadRecord(variable, calculation);
            logger.info("Record: " + record);
            return record;
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(15);
        String text = formatter.format(value);
        textField.setText(text);
        return null;
    }

    public void setTextField(JTextField textField) {
        this.textField = textField;
    }

    public void clear() {
        textField.setText(null);
    }

    public void select(int pos1, int pos2) {
        textField.select(pos1, pos2);
        textField.requestFocus();
    }

    public boolean isEmpty() {
        return textField.getText().trim().length() == 0;
    }

    public void keyTyped(KeyEvent arg0) {
        // TODO Evaluate on Enter key
    }
}
