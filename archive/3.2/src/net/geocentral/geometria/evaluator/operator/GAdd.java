/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.operator;

import net.geocentral.geometria.evaluator.token.GBinaryOperator;
import net.geocentral.geometria.evaluator.token.GValueToken;

import org.apache.log4j.Logger;

public class GAdd implements GBinaryOperator {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public double evaluate(GValueToken token1, GValueToken token2) {
        logger.info(token1 + ", " + token2);
        return token1.getValue() + token2.getValue();
    }

    public int getPrecedence() {
        return 10;
    }

    public boolean isAssociative() {
        return true;
    }

    public boolean isLeftAssociative() {
        return true;
    }

    public boolean isRightAssociative() {
        return false;
    }

    public String toString() {
        return "+";
    }
}
