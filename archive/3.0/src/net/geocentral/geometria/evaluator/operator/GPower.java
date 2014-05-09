/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.operator;

import net.geocentral.geometria.evaluator.token.GBinaryOperator;
import net.geocentral.geometria.evaluator.token.GValueToken;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GPower implements GBinaryOperator {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public double evaluate(GValueToken token1, GValueToken token2)
            throws Exception {
        logger.info(token1 + ", " + token2);
        double value = Math.pow(token1.getValue(), token2.getValue());
        if (value != Double.NaN)
            return value;
        logger.info("Bad argument");
        throw new Exception(GDictionary.get("BadArgumentsInPowerOperator"));
    }

    public int getPrecedence() {
        return 40;
    }

    public boolean isAssociative() {
        return false;
    }

    public boolean isLeftAssociative() {
        return false;
    }

    public boolean isRightAssociative() {
        return true;
    }

    public String toString() {
        return "^";
    }
}
