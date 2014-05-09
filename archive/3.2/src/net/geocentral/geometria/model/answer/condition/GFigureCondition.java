/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer.condition;

import net.geocentral.geometria.action.GRenameFigureAction;

public interface GFigureCondition extends GCondition {

    public String getFigureName();

    public void figureRenamed(GRenameFigureAction action);

    public void renameFigureUndone(GRenameFigureAction action);
}
