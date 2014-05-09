/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GDocumentEnvelope implements Cloneable {

    private GAuthor author;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GDocumentEnvelope() {
        author = new GAuthor();
        comments = "";
    }

    public GDocumentEnvelope(GAuthor author, String comments) {
        logger.info(author + ", " + comments);
        this.author = author;
        this.comments = comments;
    }

    public GDocumentEnvelope clone() {
        logger.info(this);
        return new GDocumentEnvelope(author.clone(), comments);
    }

    public void make(Element node) {
        logger.info("");
        Element n = (Element)node.getElementsByTagName("author").item(0);
        author = new GAuthor();
        author.make(n);
        String s = node.getElementsByTagName("comments").item(0).getTextContent();
        comments = GStringUtils.fromXml(s);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<envelope>");
        author.serialize(buf);
        if (comments.length() == 0) {
            buf.append("\n<comments/>");
        }
        else {
            buf.append("\n<comments>");
            String s = GStringUtils.toXml(comments);
            buf.append(s)
                .append("</comments>");
        }
        buf.append("\n</envelope>");
    }

    public GAuthor getAuthor() {
        return author;
    }

    public String getComments() {
        return comments;
    }

    public String toString() {
        return String.format("Author: %s, comments: %s", author, comments);
    }
}
