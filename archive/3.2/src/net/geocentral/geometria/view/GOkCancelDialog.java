/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

public interface GOkCancelDialog {

    public final int OK_OPTION = 0;

    public final int CANCEL_OPTION = 1;

    public void ok();

    public void cancel();

    public int getOption();
}
