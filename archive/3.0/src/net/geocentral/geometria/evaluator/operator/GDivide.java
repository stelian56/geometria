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

public class GDivide implements GBinaryOperator {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public double evaluate(GValueToken token1, GValueToken token2)
            throws Exception {
        logger.info(token1 + ", " + token2);
        try {
            return token1.getValue() / token2.getValue();
        }
        catch (ArithmeticException exception) {
            logger.info("Divide by zero");
            throw new Exception(GDictionary.get("DivideByZeroError"));
        }
    }

    public int getPrecedence() {
        return 20;
    }

    public boolean isAssociative() {
        return false;
    }

    public boolean isLeftAssociative() {
        return true;
    }

    public boolean isRightAssociative() {
        return false;
    }

    public String toString() {
        return "/";
    }
}
