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

import net.geocentral.geometria.view.GNotepadPane;

public class GNotepadMouseAdapter extends MouseAdapter {

    private GNotepadPane notepadPane;

    public GNotepadMouseAdapter(GNotepadPane notepadPane, JList recordList) {
        this.notepadPane = notepadPane;
        recordList.addMouseListener(this);
    }

    public void mousePressed(MouseEvent event) {
        if (event.isPopupTrigger() && !notepadPane.isSelectionEmpty())
            notepadPane.popupMenu(event.getX(), event.getY());
    }

    public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger() && !notepadPane.isSelectionEmpty())
            notepadPane.popupMenu(event.getX(), event.getY());
    }
}
