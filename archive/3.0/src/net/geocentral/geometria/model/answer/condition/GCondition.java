/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import javax.vecmath.Point3d;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GProblem;

import org.w3c.dom.Element;

public interface GCondition {

    public String getDescription();

    public void make(Element node, GProblem document) throws Exception;

    public void validate(String valueString, GDocument document)
            throws Exception;

    public void serialize(StringBuffer buf);

    public String getStringValue();

    public boolean verify(Point3d[] coords, GDocument document);
}
