/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.model.answer.GConditionPlaneAnswer;
import net.geocentral.geometria.model.answer.GFixedPlaneAnswer;
import net.geocentral.geometria.model.answer.GLineSetAnswer;
import net.geocentral.geometria.model.answer.GMultipleChoiceAnswer;
import net.geocentral.geometria.model.answer.GNumberAnswer;
import net.geocentral.geometria.model.answer.GPointSetAnswer;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.util.GVersionManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GProblem extends GDocument {

    private GDocumentEnvelope envelope;

    private Document text;

    private GAnswer answer;

    private boolean locked;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GProblem() {
        envelope = new GDocumentEnvelope();
        text = new PlainDocument();
        answer = new GNumberAnswer();
    }

    public void make(Element node, GXmlEntity parent) throws Exception {
        logger.info("");
        String attribute = node.getAttribute("locked");
        if (!attribute.isEmpty()) {
            locked = Boolean.valueOf(attribute);
        }
        makeVersion(node, parent);
        envelope.make((Element)node.getElementsByTagName("envelope").item(0));
        String s = node.getElementsByTagName("text").item(0).getTextContent();
        s = GStringUtils.fromXml(s);
        text.insertString(0, s, null);
        makeFigures(node);
        notepad.make((Element)node.getElementsByTagName("notepad").item(0));
        makeAnswer((Element)node.getElementsByTagName("answer").item(0));
    }

    private void makeAnswer(Element node) throws Exception {
        logger.info("");
        String type =
            node.getElementsByTagName("type").item(0).getTextContent();
        if (type.equals("number"))
            answer = new GNumberAnswer();
        else if (type.equals("pointSet"))
            answer = new GPointSetAnswer();
        else if (type.equals("lineSet")) {
            answer = new GLineSetAnswer();
        }
        else if (type.equals("fixedPlane"))
            answer = new GFixedPlaneAnswer();
        else if (type.equals("conditionPlane"))
            answer = new GConditionPlaneAnswer();
        else if (type.equals("multipleChoice"))
            answer = new GMultipleChoiceAnswer();
        else {
            logger.error(type);
            throw new Exception();
        }
        answer.make(node, this);
    }

    private void makeFigures(Element node) throws Exception {
        logger.info("");
        NodeList ns = ((Element)node.getElementsByTagName("figures").item(0)).
            getElementsByTagName("figure");
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            GFigure figure = new GFigure();
            figure.make(n, this);
            if (figures.containsKey(figure.getName())) {
                logger.error("Duplicate figure name: " + figure.getName());
                throw new Exception();
            }
            addFigure(figure);
        }
        setSelectedFigure(0);
    }

    public boolean isLocked() {
        return locked;
    }
    
    public void serialize(StringBuffer buf, boolean preamble) {
        serialize(buf, preamble, false);
    }
    
    public void serialize(StringBuffer buf, boolean preamble, boolean saveAsLocked) {
        logger.info("");
        if (preamble) {
            buf.append(PREAMBLE);
        }
        buf.append("\n<problem xmlns=\"")
               .append(APPLICATION_NAMESPACE)
               .append("\"");
        if (locked || saveAsLocked) {
            buf.append(" locked=\"true\"");
        }
        buf.append(">")
        .append("\n<version>")
        .append(GVersionManager.getInstance().getApplicationVersion())
        .append("</version>");
        envelope.serialize(buf);
        buf.append("\n<text>");
        String s;
        try {
            s = text.getText(0, text.getLength()).trim();
        }
        catch (Exception exception) {
            logger.error(text);
            s = "";
        }
        s = GStringUtils.toXml(s);
        buf.append(s);
        buf.append("</text>");
        notepad.serialize(buf);
        buf.append("\n<figures>");
        for (GFigure figure : figures.values()) {
            figure.serialize(buf);
        }
        buf.append("\n</figures>");
        answer.serialize(buf, locked || saveAsLocked);
        buf.append("\n</problem>\n");
    }

    public void clearNotepad() {
    	logger.info("");
        notepad.clear();
    }

    public String getSchemaFile(String version) {
        return GVersionManager.getInstance().getProblemSchema(version);
    }
    
    public GDocumentEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(GDocumentEnvelope envelope) {
        logger.info(envelope);
        this.envelope = envelope;
    }

    public Document getText() {
        return text;
    }

    public GAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(GAnswer answer) {
        this.answer = answer;
    }
}
