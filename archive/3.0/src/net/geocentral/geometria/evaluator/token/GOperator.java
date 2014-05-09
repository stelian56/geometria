/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.token;

public interface GOperator extends GToken {

    // Level 10: +, binary -
    // Level 20: *, /
    // Level 30: unary -
    // Level 40: ^

    public int getPrecedence();
}
