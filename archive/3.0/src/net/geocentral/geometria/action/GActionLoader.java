/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GLog;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GIconManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GActionLoader {

    public static final String SCHEMA = "/conf/action.xsd";

    public static final String FILE = "/conf/actions.xml";

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private void validateActions() throws Exception {
        logger.info("");
        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        InputStream in = getClass().getResourceAsStream(SCHEMA);
        Source schemaSource = new StreamSource(in);
        Schema actionSchema = sf.newSchema(schemaSource);
        in.close();
        in = getClass().getResourceAsStream(FILE);
        StreamSource actionSource = new StreamSource(in);
        Validator actionValidator = actionSchema.newValidator();
        actionValidator.validate(actionSource);
        in.close();
    }

    public Map<String, AbstractAction> loadActions() throws Exception {
        logger.info("");
        validateActions();
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
            NodeList ns = node.getElementsByTagName("helpId");
            String hId = null;
            if (ns.getLength() > 0) {
                hId = ns.item(0).getTextContent();
            }
            final String helpId = hId;
            AbstractAction actionHandler = new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    GDocumentHandler documentHandler = GDocumentHandler.getInstance();
                    GDocument document = documentHandler.getActiveDocument();
                    GAction action;
                    try {
                        action = (GAction)Class.forName(className).newInstance();
                    }
                    catch (Exception exception) {
                        logger.error(exception);
                        return;
                    }
                    if (action instanceof GActionWithHelp) {
                        ((GActionWithHelp)action).setHelpId(helpId);
                    }
                    if (action instanceof GLoggable && solutionConcluded(documentHandler, document)) {
                        return;
                    }
                    boolean quietMode = action instanceof GNavigationAction;
                    boolean result = action.execute(documentHandler, quietMode);
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
