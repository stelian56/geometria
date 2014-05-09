/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import javax.vecmath.Point3d;

import net.geocentral.geometria.model.GDocument;

public interface GPlaneCondition extends GCondition {

    public boolean verify(Point3d[] coords, GDocument document);
}
