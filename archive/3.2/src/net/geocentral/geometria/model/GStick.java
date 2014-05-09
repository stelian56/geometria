/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GStick implements GSelectable, Comparable<GStick> {

    public String label1;

    public String label2;

    public GStick() {}
    
    public GStick(GLine line) {
        label1 = line.firstLabel();
        label2 = line.lastLabel();
    }

    public void make(Element node) throws Exception {
        NodeList ns = ((Element)node.getElementsByTagName("labels").item(0))
            .getElementsByTagName("label");
        label1 = ns.item(0).getTextContent();
        label2 = ns.item(1).getTextContent();
    }

    public int hashCode() {
        return label1.hashCode() + label2.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof GStick
            && (((GStick)o).label1.equals(label1)
            && ((GStick)o).label2.equals(label2) || ((GStick)o).label1
                .equals(label2) && ((GStick)o).label2.equals(label1));
    }

    public String toString() {
        return label1 + label2;
    }

    public int compareTo(GStick stick) {
        int result = label1.compareTo(stick.label1);
        if (result != 0)
            return result;
        return label2.compareTo(stick.label2);
    }
}
