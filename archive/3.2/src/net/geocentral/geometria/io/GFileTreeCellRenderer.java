/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Element;

public class GFileTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        Component comp = super.getTreeCellRendererComponent(tree, value,
                selected, expanded, leaf, row, hasFocus);
        Element node =
            (Element)((DefaultMutableTreeNode) value).getUserObject();
        if (node.getNodeName().equals("file"))
            setIcon(getDefaultLeafIcon());
        else if (node.getNodeName().equals("dir")) {
            setIcon(expanded ? getDefaultOpenIcon() : getDefaultClosedIcon());
        }
        setText(node.getAttribute("name"));
        return comp;
    }

    private static final long serialVersionUID = 1L;
}
