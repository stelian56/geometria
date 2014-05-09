/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Frame;

import javax.swing.filechooser.FileFilter;

import net.geocentral.geometria.io.GFileReader;
import net.geocentral.geometria.io.GFileWriter;

public interface GContainer {

    public GFileReader getFileReader(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter);

    public GFileWriter getFileWriter(Frame ownerFrame, String filePath,
            FileFilter[] fileFilters, boolean acceptAllFilesFilter);

    public Frame getOwnerFrame();

    public GMainPanel getMainPanel();

    public void load() throws Exception;

    public void exit();
}
