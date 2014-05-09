/**
 * Copyright 2000-2013 Geometria Contributors
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

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.action.GLoggable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GIconManager;

public class GLogPopupMenu extends JPopupMenu {

    private GLoggable action;
    
    public GLogPopupMenu(GLoggable action) {
        this.action = action;
        init();
    }

    private void init() {
        AbstractAction actionHandler = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                GDocumentHandler documentHandler = GDocumentHandler.getInstance();
                GLogCommentsDialog dialog = new GLogCommentsDialog(documentHandler.getOwnerFrame(), action);
                dialog.setVisible(true);
                if (dialog.getOption() == GLogCommentsDialog.OK_OPTION) {
                    documentHandler.setDocumentModified(true);
                    documentHandler.updateActionHandlerStates();
                }
            }

            private static final long serialVersionUID = 1L;
        };
        JMenuItem menuItem = new JMenuItem(actionHandler);
        menuItem.setText(GDictionary.get("Comment"));
        actionHandler.putValue(AbstractAction.SMALL_ICON,  GIconManager.getInstance().getEmptyIcon());
        add(menuItem);
    }

    private static final long serialVersionUID = 1L;
}
