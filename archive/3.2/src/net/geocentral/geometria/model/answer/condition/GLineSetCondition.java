/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import java.util.List;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GPoint3d;

public interface GLineSetCondition extends GCondition {

    public boolean verify(List<GPoint3d[]> lines, GDocument document);
}
