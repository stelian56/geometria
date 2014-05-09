/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.Color;

public class GPainter {

    public static final double CONTRAST = 0.75;

    private static GPainter instance;
    
    private GPainter() {}
    
    public static GPainter getInstance() {
        if (instance == null)
            instance = new GPainter();
        return instance;
    }
    
    public Color getHue(Color baseColor, double cosine) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(),
                baseColor.getBlue(), hsb);
        double A = 1 - CONTRAST;
        double B = CONTRAST * cosine;
        hsb[2] = (float)(A * hsb[2] + B);
        Color color = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        return color;
    }
}
