/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.net.URL;

import javax.swing.ImageIcon;

import net.geocentral.geometria.action.GDocumentHandler;

public class GIconManager {

    private static GIconManager instance;
    
    private GIconManager() {}
    
    public static GIconManager getInstance() {
        if (instance == null)
            instance = new GIconManager();
        return instance;
    }
    
    public ImageIcon getEmptyIcon() {
        URL url = GDocumentHandler.class.getResource("/images/24x24/Empty.png");
        return new ImageIcon(url);
    }

    public ImageIcon getIcon(String name) {
        URL url = GIconManager.class.getResource("/images/" + name);
        return new ImageIcon(url);
    }

    public ImageIcon get24x24Icon(String name) {
        URL url = GIconManager.class.getResource("/images/24x24/" + name);
        return new ImageIcon(url);
    }
}
