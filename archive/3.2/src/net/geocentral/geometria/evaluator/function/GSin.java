/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.function;

import net.geocentral.geometria.evaluator.token.GFunction;
import net.geocentral.geometria.evaluator.token.GValueToken;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GSin implements GFunction {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public double evaluate(GValueToken token) throws Exception {
        logger.info(token);
        double value = Math.sin(token.getValue());
        if (value != Double.NaN)
            return value;
        logger.info("Bad argument");
        throw new Exception(GDictionary.get("BadArgumentInFunction",
                String.valueOf(token), "sin"));
    }

    public String toString() {
        return "sin";
    }
}
