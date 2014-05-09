/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GAuthor implements Cloneable {

    private String name;

    private String email;

    private String web;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GAuthor() {
        name = GDictionary.get("Anonymous");
        email = "";
        web = "";
    }

    public GAuthor(String name, String email, String web) {
        logger.info(name + ", " + email + ", " + web);
        this.name = name;
        this.email = email;
        this.web = web;
    }

    public GAuthor clone() {
        GAuthor author = new GAuthor();
        author.name = name;
        author.email = email;
        author.web = web;
        return author;
    }

    public void set(GAuthor author) {
        logger.info(author);
        name = author.name;
        email = author.email;
        web = author.web;
    }

    public void make(Element node) {
        logger.info("");
        String s = node.getElementsByTagName("name").item(0).getTextContent();
        name = GStringUtils.fromXml(s);
        s = node.getElementsByTagName("email").item(0).getTextContent();
        email = GStringUtils.fromXml(s);
        s = node.getElementsByTagName("web").item(0).getTextContent();
        web = GStringUtils.fromXml(s);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<author>");
        if (name.length() == 0) {
            buf.append("\n<name/>");
        }
        else {
            buf.append("\n<name>");
            String s = GStringUtils.toXml(name);
            buf.append(s);
            buf.append("</name>");
        }
        if (email.length() == 0) {
            buf.append("\n<email/>");
        }
        else {
            buf.append("\n<email>");
            String s = GStringUtils.toXml(email);
            buf.append(s);
            buf.append("</email>");
        }
        if (web.length() == 0) {
            buf.append("\n<web/>");
        }
        else {
            buf.append("\n<web>");
            String s = GStringUtils.toXml(web);
            buf.append(s);
            buf.append("</web>");
        }
        buf.append("\n</author>");
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getWeb() {
        return web;
    }

    public String toString() {
        return "Name: " + name + ", email: " + email + ", web:" + web;
    }
}
