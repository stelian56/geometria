/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GVersionHistoryItem {

    private String version;
    
    private String solidSchema;
    
    private String problemSchema;
    
    private String solutionSchema;
    
    private String optionsSchema;
    
    public String getVersion() {
        return version;
    }

    public String getSolidSchema() {
        return String.format("/conf/%s", solidSchema);
    }

    public String getProblemSchema() {
        return String.format("/conf/%s", problemSchema);
    }

    public String getSolutionSchema() {
        return String.format("/conf/%s", solutionSchema);
    }

    public String getOptionsSchema() {
        return String.format("/conf/%s", optionsSchema);
    }
    
    public void make(Element node) {
        version = node.getElementsByTagName("number").item(0).getTextContent();
        solidSchema = node.getElementsByTagName("solidSchema").item(0).getTextContent();
        problemSchema = node.getElementsByTagName("problemSchema").item(0).getTextContent();
        solutionSchema = node.getElementsByTagName("solutionSchema").item(0).getTextContent();
        NodeList ns = node.getElementsByTagName("optionsSchema");
        if (ns.getLength() > 0) {
            optionsSchema = ns.item(0).getTextContent();
        }
    }
}
