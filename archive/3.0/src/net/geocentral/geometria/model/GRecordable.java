/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import org.w3c.dom.Element;

public interface GRecordable {

    public void make(Element node) throws Exception;

    public void serialize(StringBuffer buf);

    public String getExpression();
}
