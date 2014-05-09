/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GProblem;

import org.w3c.dom.Element;

public interface GAnswer {

    public void serialize(StringBuffer buf);

    public void make(Element node, GProblem document) throws Exception;

    public boolean validate(String valueString, String figureName,
            GDocument document);

}
