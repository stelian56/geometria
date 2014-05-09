/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import net.geocentral.geometria.evaluator.token.GVariable;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GNotepadVariable implements GVariable {

    private String name;

    private Double value;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GNotepadVariable() {}

    public GNotepadVariable(String name, Double value) {
        this.name = name;
        this.value = value;
        logger.info(this);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        name = node.getElementsByTagName("name").item(0).getTextContent();
        String valueString =
            node.getElementsByTagName("value").item(0).getTextContent();
        try {
            value = Double.parseDouble(valueString);
        }
        catch (Exception exception) {
            logger.error(valueString);
            throw exception;
        }
    }

//    public void serialize(Writer out) throws Exception {
//        logger.info("");
//        out.write("\n<variable>");
//        out.write("\n<name>");
//        out.write(name);
//        out.write("</name>");
//        out.write("\n<value>");
//        out.write(String.valueOf(value));
//        out.write("</value>");
//        out.write("\n</variable>");
//    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String toString() {
        return name + "=" + value;
    }
}
