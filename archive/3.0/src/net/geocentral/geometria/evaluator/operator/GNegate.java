/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.operator;

import net.geocentral.geometria.evaluator.token.GUnaryOperator;
import net.geocentral.geometria.evaluator.token.GValueToken;

import org.apache.log4j.Logger;

public class GNegate implements GUnaryOperator {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public double evaluate(GValueToken token) {
        logger.info(token);
        return -token.getValue();
    }

    public int getPrecedence() {
        return 30;
    }

    public String toString() {
        return "neg";
    }
}
