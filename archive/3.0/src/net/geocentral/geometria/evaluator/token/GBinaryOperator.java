/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.token;

public interface GBinaryOperator extends GOperator {

    public double evaluate(GValueToken token1, GValueToken token2)
        throws Exception;

    public boolean isAssociative();

    public boolean isLeftAssociative();

    public boolean isRightAssociative();
}
