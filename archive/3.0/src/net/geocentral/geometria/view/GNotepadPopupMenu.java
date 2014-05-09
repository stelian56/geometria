/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.geocentral.geometria.util.GDictionary;

public class GNotepadPopupMenu extends JPopupMenu {

    private GNotepadPane notepadPane;
    
    public GNotepadPopupMenu(GNotepadPane notepadPane) {
        this.notepadPane = notepadPane;
        init();
    }

    private void init() {
        AbstractAction actionHandler = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                notepadPane.clearSelection();
            }

            private static final long serialVersionUID = 1L;
        };
        JMenuItem menuItem = new JMenuItem(actionHandler);
        menuItem.setText(GDictionary.get("ClearSelection"));
        add(menuItem);
    }

    private static final long serialVersionUID = 1L;
}
