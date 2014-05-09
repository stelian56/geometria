/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.geocentral.geometria.evaluator.function.GAcos;
import net.geocentral.geometria.evaluator.function.GAsin;
import net.geocentral.geometria.evaluator.function.GAtan;
import net.geocentral.geometria.evaluator.function.GCos;
import net.geocentral.geometria.evaluator.function.GSin;
import net.geocentral.geometria.evaluator.function.GSqrt;
import net.geocentral.geometria.evaluator.function.GTan;
import net.geocentral.geometria.evaluator.operator.GAdd;
import net.geocentral.geometria.evaluator.operator.GDivide;
import net.geocentral.geometria.evaluator.operator.GNegate;
import net.geocentral.geometria.evaluator.operator.GPower;
import net.geocentral.geometria.evaluator.operator.GSubtract;
import net.geocentral.geometria.evaluator.operator.GTimes;
import net.geocentral.geometria.evaluator.token.GConstant;
import net.geocentral.geometria.evaluator.token.GDecimal;
import net.geocentral.geometria.evaluator.token.GFunction;
import net.geocentral.geometria.evaluator.token.GLeftParanthesis;
import net.geocentral.geometria.evaluator.token.GOperator;
import net.geocentral.geometria.evaluator.token.GPi;
import net.geocentral.geometria.evaluator.token.GRightParanthesis;
import net.geocentral.geometria.evaluator.token.GToken;
import net.geocentral.geometria.evaluator.token.GTokenBounds;
import net.geocentral.geometria.evaluator.token.GValueToken;
import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.model.GLabelFactory;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GTokenizer {

    public static final String CHARSET =
        "[0-9\\." + GLabelFactory.VARIABLE_CHARSET + "]";
    
    private Map<String, GConstant> constants;

    private Map<String, GVariable> variables;

    private GTokenBounds errorTokenBounds;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public <T extends GVariable> GTokenizer(Collection<T> variableList) {
        logger.info(variableList);
        makeConstants();
        variables = new LinkedHashMap<String, GVariable>();
        if (variableList != null) {
            for (GVariable variable : variableList)
                variables.put(variable.getName(), variable);
        }
    }

    private void makeConstants() {
        logger.info("");
        constants = new LinkedHashMap<String, GConstant>();
        GPi pi = new GPi();
        constants.put(pi.getName().toLowerCase(), pi);
    }

    public Map<GToken, GTokenBounds> tokenize(String input) throws Exception {
        logger.info(input);
        List<GToken> tokenList = new ArrayList<GToken>();
        List<GTokenBounds> tokenBoundsList = new ArrayList<GTokenBounds>();
        StringBuffer buf = new StringBuffer();
        for (int pos = 0; pos < input.length(); pos++) {
            char ch = input.charAt(pos);
            if (String.valueOf(ch).matches(CHARSET)) {
                buf.append(ch);
                if (pos == input.length() - 1) {
                    GTokenBounds tokenBounds = new GTokenBounds(pos
                            - buf.length() + 1, buf.length());
                    GToken token;
                    try {
                        token = makeAlphaNumericToken(buf);
                    }
                    catch (Exception exception) {
                        errorTokenBounds = tokenBounds;
                        throw exception;
                    }
                    tokenList.add(token);
                    tokenBoundsList.add(tokenBounds);
                }
                continue;
            }
            else if (buf.length() > 0) {
                GTokenBounds tokenBounds = new GTokenBounds(pos
                        - buf.length(), buf.length());
                GToken token;
                try {
                    token = makeAlphaNumericToken(buf);
                }
                catch (Exception exception) {
                    errorTokenBounds = tokenBounds;
                    throw exception;
                }
                tokenList.add(token);
                tokenBoundsList.add(tokenBounds);
                buf = new StringBuffer();
            }
            if (ch == ' ')
                continue;
            else if (ch == '(') {
                tokenList.add(new GLeftParanthesis());
                tokenBoundsList.add(new GTokenBounds(pos, 1));
            }
            else if (ch == ')') {
                tokenList.add(new GRightParanthesis());
                tokenBoundsList.add(new GTokenBounds(pos, 1));
            }
            else {
                GToken prevToken = tokenList.isEmpty() ? null
                        : tokenList.get(tokenList.size() - 1);
                GTokenBounds tokenBounds = new GTokenBounds(pos, 1);
                GToken token;
                try {
                    token = makeOperator(ch, prevToken);
                }
                catch (Exception exception) {
                    errorTokenBounds = tokenBounds;
                    throw exception;
                }
                tokenList.add(token);
                tokenBoundsList.add(tokenBounds);
            }
        }
        Iterator<GToken> tokenIterator = tokenList.iterator();
        Iterator<GTokenBounds> tokenBoundsIterator = tokenBoundsList.iterator();
        Map<GToken, GTokenBounds> tokenMap =
            new LinkedHashMap<GToken, GTokenBounds>();
        while (tokenIterator.hasNext()) {
            GToken token = tokenIterator.next();
            GTokenBounds tokenBounds = tokenBoundsIterator.next();
            tokenMap.put(token, tokenBounds);
        }
        return tokenMap;
    }

    private GToken makeAlphaNumericToken(StringBuffer buf) throws Exception {
        logger.info("");
        String bufValue = String.valueOf(buf);
        if (bufValue.equalsIgnoreCase("acos"))
            return new GAcos();
        else if (bufValue.equalsIgnoreCase("asin"))
            return new GAsin();
        else if (bufValue.equalsIgnoreCase("atan"))
            return new GAtan();
        else if (bufValue.equalsIgnoreCase("cos"))
            return new GCos();
        else if (bufValue.equalsIgnoreCase("sin"))
            return new GSin();
        else if (bufValue.equalsIgnoreCase("sqrt"))
            return new GSqrt();
        else if (bufValue.equalsIgnoreCase("tan"))
            return new GTan();
        else {
            GConstant constant = constants.get(bufValue.toLowerCase());
            if (constant != null)
                return copyOf(constant);
            GVariable variable = variables.get(bufValue);
            if (variable != null)
                return copyOf(variable);
            double value;
            try {
                value = Double.parseDouble(bufValue);
            }
            catch (Exception exception) {
                logger.info("Bad token: " + bufValue);
                throw new Exception(GDictionary.get("BadToken", bufValue));
            }
            return new GDecimal(value);
        }
    }

    private GToken copyOf(final GVariable variable) {
        logger.info(variable);
        GVariable v = new GVariable() {
            public String getName() {
                return variable.getName();
            }

            public double getValue() {
                return variable.getValue();
            }

            public String toString() {
                return variable.toString();
            }
        };
        return v;
    }

    private GToken copyOf(final GConstant constant) {
        logger.info(constant);
        GConstant c = new GConstant() {
            public String getName() {
                return constant.getName();
            }

            public double getValue() {
                return constant.getValue();
            }

            public String toString() {
                return constant.toString();
            }
        };
        return c;
    }

    private GOperator makeOperator(char ch, GToken prevToken) throws Exception {
        logger.info(ch + " " + prevToken);
        if (ch == '+')
            return new GAdd();
        else if (ch == '*')
            return new GTimes();
        else if (ch == '/')
            return new GDivide();
        else if (ch == '^')
            return new GPower();
        else if (ch == '-') {
            if (prevToken == null)
                return new GNegate();
            else if (prevToken instanceof GValueToken)
                return new GSubtract();
            else if (prevToken instanceof GRightParanthesis)
                return new GSubtract();
            else if (prevToken instanceof GFunction) {
                logger.info("Misplaced operator -");
                throw new Exception(GDictionary.get("MisplacedOperator", "-"));
            }
            else
                return new GNegate();
        }
        else {
            logger.info("Bad token: " + ch);
            throw new Exception(GDictionary.get("BadToken", String.valueOf(ch)));
        }
    }

    public Map<String, GConstant> getConstants() {
        return constants;
    }

    public Map<String, GVariable> getVariables() {
        return variables;
    }

    public GTokenBounds getErrorTokenBounds() {
        return errorTokenBounds;
    }

}
