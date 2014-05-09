/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.util.Arrays;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GFileTree extends JTree {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GFileTree(DefaultTreeModel model) {
        super(model);
        GFileTreeCellRenderer renderer = new GFileTreeCellRenderer();
        setCellRenderer(renderer);
        setExpandsSelectedPaths(true);
    }

    public void setSelectionPath(String path) {
        logger.info(path);
        DefaultMutableTreeNode treeNode =
            (DefaultMutableTreeNode)getModel().getRoot();
        TreePath treePath = new TreePath(treeNode);
        String[] tokens = path.split("/");
        int index = 1;
        makeTreePath(treePath, tokens, index);
        setSelectionPath(treePath);
    }

    private void makeTreePath(TreePath treePath, String[] pathTokens,
            int index) {
        logger.info(Arrays.asList(pathTokens + ", " + index));
        if (index > pathTokens.length - 1)
            return;
        DefaultMutableTreeNode treeNode =
            (DefaultMutableTreeNode)treePath.getLastPathComponent();
        String name = pathTokens[index];
        for (int i = 0; i < treeNode.getChildCount(); i++) {
            DefaultMutableTreeNode tNode =
                (DefaultMutableTreeNode)treeNode.getChildAt(i);
            Element n = (Element)tNode.getUserObject();
            if (n.getAttribute("name").equals(name)) {
                treePath.pathByAddingChild(tNode);
                index++;
                return;
            }
        }
    }

    private static final long serialVersionUID = 1L;
}
