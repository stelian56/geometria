/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GIconManager;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GActionLoader {

    public static final String FILE = "/conf/actions.xml";

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public Map<String, AbstractAction> loadActions() throws Exception {
        logger.info("");
        Map<String, AbstractAction> actionHandlers = new LinkedHashMap<String, AbstractAction>();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        InputStream in = getClass().getResourceAsStream(FILE);
        Element element = builder.parse(in).getDocumentElement();
        in.close();
        NodeList nodes = element.getElementsByTagName("action");
        String packageName = GActionLoader.class.getPackage().getName();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element)nodes.item(i);
            String name = node.getElementsByTagName("name").item(0).getTextContent();
            String cName = node.getElementsByTagName("className").item(0).getTextContent();
            final String className = packageName + "." + cName;
            final List<String> parameters = new ArrayList<String>();
            NodeList ns = node.getElementsByTagName("parameter");
            if (ns.getLength() > 0) {
                for (int j = 0; j < ns.getLength(); j++) {
                    String parameter = ns.item(j).getTextContent();
                    parameters.add(parameter);
                }
            }
            ns = node.getElementsByTagName("helpId");
            String hId = null;
            if (ns.getLength() > 0) {
                hId = ns.item(0).getTextContent();
            }
            final String helpId = hId;
            AbstractAction actionHandler = new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    GDocumentHandler documentHandler = GDocumentHandler.getInstance();
                    GDocument document = documentHandler.getActiveDocument();
                    GAction action = null;
                    try {
                        Constructor<?>[] constructors = Class.forName(className).getConstructors();
                        for (Constructor<?> constructor : constructors) {
                            if (constructor.getParameterTypes().length == parameters.size()) {
                                switch (parameters.size()) {
                                case 1:
                                    action = (GAction)constructor.newInstance(parameters.get(0));
                                    break;
                                default:
                                    action = (GAction)constructor.newInstance();
                                }
                                break;
                            }
                        }
                    }
                    catch (Exception exception) {
                        logger.error(GStringUtils.stackTraceToString(exception));
                        return;
                    }
                    if (action instanceof GActionWithHelp) {
                        ((GActionWithHelp)action).setHelpId(helpId);
                    }
                    if (action instanceof GLoggable && solutionConcluded(documentHandler, document)) {
                        return;
                    }
                    Frame ownerFrame = documentHandler.getOwnerFrame();
                    ownerFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    boolean result = action.execute();
                    ownerFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (!result) {
                        return;
                    }
                    if (!(action instanceof GNavigationAction))
                    {
                        if (action instanceof GUndoable) {
                            documentHandler.addAction((GUndoable)action);
                        }
                        if (document instanceof GSolution && action instanceof GLoggable) {
                            ((GSolution)document).getLog().add((GLoggable)action);
                        }
                    }
                    documentHandler.updateActionHandlerStates();
                }

                private static final long serialVersionUID = 1L;
            };
            if (node.getElementsByTagName("icon").getLength() == 0) {
                actionHandler.putValue(AbstractAction.SMALL_ICON,  GIconManager.getInstance().getEmptyIcon());
            }
            else {
                String iconFile = node.getElementsByTagName("icon").item(0).getTextContent(); 
                ImageIcon icon = GIconManager.getInstance().get24x24Icon(iconFile);
                actionHandler.putValue(AbstractAction.SMALL_ICON, icon);
            }
            String shortDesc = node.getElementsByTagName("shortDesc").item(0).getTextContent();
            actionHandler.putValue(AbstractAction.SHORT_DESCRIPTION, GDictionary.get(shortDesc));
            actionHandlers.put(name, actionHandler);
        }
        return actionHandlers;
    }

    private boolean solutionConcluded(GDocumentHandler documentHandler, GDocument document) {
        logger.info("");
        if (document instanceof GSolution) {
            GLog log = ((GSolution)document).getLog();
            int logSize = log.size();
            if (logSize > 0 && log.actionAt(logSize - 1) instanceof GAnswerAction) {
                GGraphicsFactory.getInstance().showErrorDialog( GDictionary.get("SolutionConcluded"));
                return true;
            }
        }
        return false;
    }
}
