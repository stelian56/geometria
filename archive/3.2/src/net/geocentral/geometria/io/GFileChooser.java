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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GFileChooser extends JDialog implements ActionListener {

    public static final Dimension SIZE = new Dimension(300, 400); 

    private GFileTree tree;

    private JButton selectButton;

    private JButton cancelButton;

    private int option = JFileChooser.CANCEL_OPTION;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GFileChooser(Element docElement, Frame ownerFrame, String filePath) {
        super(ownerFrame, true);
        logger.info(filePath);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(docElement);
        make(node);
        DefaultTreeModel model = new DefaultTreeModel(node);
        tree = new GFileTree(model);
        tree.setShowsRootHandles(true);
        tree.addMouseListener(new GFileChooserMouseAdapter());
        if (filePath != null) {
            tree.setSelectionPath(filePath);
        }
        setTitle(GDictionary.get("UIManager.FileChooser.openDialogTitleText"));
        layoutComponents();
        pack();
    }

    private void make(DefaultMutableTreeNode node) {
        logger.info("");
        Element element = (Element) node.getUserObject();
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                DefaultMutableTreeNode tNode =
                    new DefaultMutableTreeNode((Element) nodes.item(i));
                node.add(tNode);
                if (n.getNodeName().equals("dir"))
                    make(tNode);
            }
        }
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JScrollPane sc = new JScrollPane(tree);
        sc.setPreferredSize(SIZE);
        getContentPane().add(sc);
        getContentPane().add(
                GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(new EmptyBorder(15, 13, 17, 13));
        selectButton = GGraphicsFactory.getInstance().createButton(
                GDictionary.get("Select"));
        selectButton.addActionListener(this);
        buttonPane.add(selectButton);
        buttonPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        cancelButton = GGraphicsFactory.getInstance().createButton(
                GDictionary.get("Cancel"));
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton);
        JButton[] buttons = { selectButton, cancelButton };
        GGraphicsFactory.getInstance().adjustSize(buttons);
        getContentPane().add(GGraphicsFactory.getInstance()
                .createSmallRigidArea());
        getContentPane().add(buttonPane);
    }

    public int showOpenDialog(Component parent) {
        setVisible(true);
        return option;
    }

    public String getSelectionPath() {
        logger.info("");
        TreePath treePath = tree.getSelectionPath();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < treePath.getPathCount(); i++) {
            if (i > 0) {
                buf.append("/");
            }
            DefaultMutableTreeNode treeNode =  (DefaultMutableTreeNode)treePath.getPathComponent(i);
            Element node = (Element)treeNode.getUserObject();
            buf.append(node.getAttribute("name"));
        }
        logger.info(buf);
        return buf.toString();
    }

    public void actionPerformed(ActionEvent event) {
        logger.info("");
        if (event.getSource() == selectButton) {
            TreePath path = tree.getSelectionPath();
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)path.getLastPathComponent();
            Element element = (Element)node.getUserObject();
            if (element.getNodeName().equals("dir"))
                tree.expandPath(path);
            else {
                option = JFileChooser.APPROVE_OPTION;
                dispose();
            }
        }
        else if (event.getSource() == cancelButton) {
            option = JFileChooser.CANCEL_OPTION;
            dispose();
        }
    }

    private static final long serialVersionUID = 1L;

    class GFileChooserMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() > 1) {
                TreePath path = tree.getSelectionPath();
                DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)path.getLastPathComponent();
                Element element = (Element)node.getUserObject();
                if (!element.getNodeName().equals("dir")) {
                    option = JFileChooser.APPROVE_OPTION;
                    dispose();
                    return;
                }
            }
            super.mouseClicked(event);
        }
    }
}
