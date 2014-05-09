/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public abstract class GXmlEntity {

    public static final String PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String APPLICATION_NAMESPACE = "http://geocentral.net";
    
    private String version;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void makeVersion(Element docElement, GXmlEntity parent) {
        if (parent != null) {
            version = parent.getVersion();
        }
        else {
            version = docElement.getElementsByTagName("version").item(0).getTextContent();
        }
    }
    
    public void serialize(StringBuffer buf) {
        logger.info("");
        serialize(buf, false);
    }

    public String getVersion() {
        return version;
    }
    
    public abstract void serialize(StringBuffer buf, boolean preamble);

    public abstract void make(Element node, GXmlEntity parent) throws Exception;

    public void make(Element node) throws Exception {
        logger.info("");
        make(node, null);
    }
    
    public abstract String getSchemaFile(String version);
}
