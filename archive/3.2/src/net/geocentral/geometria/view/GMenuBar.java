/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GIconManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GMenuBar extends JMenuBar {

    public static final String FILE = "/conf/menuBar.xml";
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GMenuBar() {
    }

    public void loadMenus() throws Exception {
        logger.info("");
        InputStream in = GMenuBar.class.getResourceAsStream(FILE);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Element parent = builder.parse(in).getDocumentElement();
        in.close();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element node = (Element)nodes.item(i);
                JMenu menu = makeMenu(node, false);
                add(menu);
            }
        }
    }

    private JMenu makeMenu(Element node, boolean customize) {
        JMenu menu = customize ? new JMenu() : new JMenu();
        Map<String, ButtonGroup> buttonGroups = new HashMap<String, ButtonGroup>();
        NodeList ns = node.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++) {
            Node n = ns.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            if (((Element)n).getTagName().equals("name")) {
                menu.setText(GDictionary.get(n.getTextContent()));
            }
            else if (((Element)n).getTagName().equals("menu")) {
                JMenu subMenu = makeMenu((Element)n, true);
                subMenu.setIcon(GIconManager.getInstance().getEmptyIcon());
                menu.add(subMenu);
            }
            else if (((Element)n).getTagName().equals("menuItem")) {
                JMenuItem menuItem;
                String menuItemName = ((Element)n).getElementsByTagName("name").item(0).getTextContent();
                String actionName = ((Element)n).getElementsByTagName("action").item(0).getTextContent();
                AbstractAction actionHandler = GDocumentHandler.getInstance().getActionHandler(actionName);
                NodeList nns = ((Element)n).getElementsByTagName("group");
                if (nns.getLength() > 0) {
                    String group = nns.item(0).getTextContent();
                    ButtonGroup buttonGroup = buttonGroups.get(group);
                    if (buttonGroup == null) {
                        buttonGroup = new ButtonGroup();
                        buttonGroups.put(group, buttonGroup);
                    }
                    menuItem = new JRadioButtonMenuItem(actionHandler);
                    buttonGroup.add(menuItem);
                }
                else {
                    menuItem = new JMenuItem(actionHandler);
                }
                menuItem.setText(GDictionary.get(menuItemName));
                menu.add(menuItem);
            }
            else if (((Element)n).getTagName().equals("separator")) {
                menu.addSeparator();
            }
        }
        return menu;
    }
    
    private static final long serialVersionUID = 1L;
}
