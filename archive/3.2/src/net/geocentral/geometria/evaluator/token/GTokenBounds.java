/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator.token;

public class GTokenBounds implements Comparable<GTokenBounds> {

    public int offset;

    public int size;

    public GTokenBounds(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public String toString() {
        return "(" + offset + "," + size + ")";
    }

    public int compareTo(GTokenBounds tokenBounds) {
        return Integer.valueOf(offset).compareTo(Integer.valueOf(((GTokenBounds)tokenBounds).offset));
    }
}
