/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.io.InputStream;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GPlatonicAction implements GLoggable {

    private String type;
    
    private String figureName;

    private String comments;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GPlatonicAction() {
    }
    
    public GPlatonicAction(String parameter) {
        type = parameter;
    }
    
    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GSolid solid = new GSolid();
        try {
            InputStream in = getClass().getResourceAsStream("/gallery/" + type);
            Element docElement = GXmlUtils.read(in);
            in.close();
            solid = new GSolid();
            solid.make(docElement);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            return false;
        }
        GFigure figure = documentHandler.newFigure(solid);
        if (!silent) {
            documentHandler.setDocumentModified(true);
        }
        documentHandler.notepadChanged();
        figureName = figure.getName();
        logger.info(figureName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.removeFigure(figureName);
        documentHandler.removeFigure(figureName);
        documentHandler.notepadChanged();
        logger.info(figureName);
    }

    public GLoggable clone() {
        GPlatonicAction action = new GPlatonicAction();
        action.figureName = figureName;
        action.type = type;
        return action;
    }

    public String toLogString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GDictionary.get("Create", type, figureName));
        return String.valueOf(buf);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("type");
        if (ns.getLength() == 0) {
            logger.error("No type");
            throw new Exception();
        }
        type = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("comments");
        if (ns.getLength() > 0) {
            String s = ns.item(0).getTextContent();
            comments = GStringUtils.fromXml(s);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>")
            .append("\n<className>")
            .append(this.getClass().getSimpleName())
            .append("</className>")
            .append("\n<type>")
            .append(type)
            .append("</type>");
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("create", type, figureName);
    }
}
