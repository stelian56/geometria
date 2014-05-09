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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.action.GDocumentHandler;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GToolBarHandler {

    public static final String FILE = "/conf/toolBars.xml";

    private static GToolBarHandler instance;

    private Map<String, JToolBar> toolBars;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GToolBarHandler() {
    }

    public static GToolBarHandler getInstance() {
        if (instance == null) {
            instance = new GToolBarHandler();
        }
        return instance;
    }
    
    public void init() throws Exception {
        loadToolBars();
    }

    public void loadToolBars() throws Exception {
        logger.info("");
        InputStream in = GToolBarHandler.class.getResourceAsStream(FILE);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Element node = builder.parse(in).getDocumentElement();
        in.close();
        toolBars = new LinkedHashMap<String, JToolBar>();
        NodeList ns = node.getElementsByTagName("toolBar");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            String name = n.getElementsByTagName("name").item(0).getTextContent();
            JToolBar toolBar = new JToolBar();
            NodeList nns = n.getElementsByTagName("toolBarButton");
            for (int j = 0; j < nns.getLength(); j++) {
                Element nn = (Element)nns.item(j);
                String actionName = nn.getElementsByTagName("action").item(0).getTextContent();
                AbstractAction actionHandler = GDocumentHandler.getInstance().getActionHandler(actionName);
                JButton button = new JButton(actionHandler);
                button.setText(null);
                toolBar.add(button);
            }
            toolBars.put(name, toolBar);
        }
    }

    public JToolBar getToolBar(String name) {
        return toolBars.get(name);
    }
}
