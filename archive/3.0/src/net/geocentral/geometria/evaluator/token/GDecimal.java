/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.token;

public class GDecimal implements GValueToken {

    public double value;

    public GDecimal(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}
