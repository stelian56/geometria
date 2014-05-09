/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import org.w3c.dom.Element;

public interface GLoggable extends GUndoable, Cloneable {

    public GLoggable clone();

    public String toLogString();

    public void setComments(String comments);

    public String getComments();
    
    public void make(Element node) throws Exception;
    
    public void serialize(StringBuffer buf);

}
