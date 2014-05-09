/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GIconManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GMenuBar extends JMenuBar {

    public static final String SCHEMA = "/conf/menuBar.xsd";

    public static final String FILE = "/conf/menuBar.xml";

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private void validateMenus() throws Exception {
        logger.info("");
        SchemaFactory sf =
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        InputStream in = getClass().getResourceAsStream(SCHEMA);
        Source schemaSource = new StreamSource(in);
        Schema menuBarSchema = sf.newSchema(schemaSource);
        in.close();
        in = getClass().getResourceAsStream(FILE);
        StreamSource menuBarSource = new StreamSource(in);
        Validator menuBarValidator = menuBarSchema.newValidator();
        menuBarValidator.validate(menuBarSource);
        in.close();
    }

    public void loadMenus() throws Exception {
        logger.info("");
        validateMenus();
        InputStream in = GMenuBar.class.getResourceAsStream(FILE);
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Element parent = builder.parse(in).getDocumentElement();
        in.close();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element node = (Element)nodes.item(i);
                JMenu menu = makeMenu(node);
                add(menu);
            }
        }
    }

    private JMenu makeMenu(Element node) {
        JMenu menu = new JMenu();
        NodeList ns = node.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++) {
            Node n = ns.item(i);
            if (!(n instanceof Element))
                continue;
            if (((Element)n).getTagName().equals("name"))
                menu.setText(GDictionary.get(n.getTextContent()));
            else if (((Element)n).getTagName().equals("menu")) {
                JMenu subMenu = makeMenu((Element)n);
                subMenu.setIcon(GIconManager.getInstance().getEmptyIcon());
                menu.add(subMenu);
            }
            else if (((Element)n).getTagName().equals("menuItem")) {
                String name = ((Element)n).getElementsByTagName("name").item(0)
                    .getTextContent();
                String actionName = ((Element)n).getElementsByTagName("action")
                    .item(0).getTextContent();
                AbstractAction actionHandler =
                    GDocumentHandler.getInstance().getActionHandler(actionName);
                JMenuItem menuItem = new JMenuItem(actionHandler);
                menuItem.setText(GDictionary.get(name));
                menu.add(menuItem);
            }
            else if (((Element)n).getTagName().equals("separator"))
                menu.addSeparator();
        }
        return menu;
    }

    private static final long serialVersionUID = 1L;
}
