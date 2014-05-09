/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;

import net.geocentral.geometria.view.GLogPane;

public class GLogMouseAdapter extends MouseAdapter {

    private JList actionList;
    
    private GLogPane logPane;

    public GLogMouseAdapter(GLogPane logPane, JList actionList) {
        this.actionList= actionList;
        this.logPane = logPane;
        actionList.addMouseListener(this);
    }

    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger() && actionList.getModel().getSize() > 0)
            logPane.popupMenu(event);
    }

    public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger() && actionList.getModel().getSize() > 0)
            logPane.popupMenu(event);
    }
}
