/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import javax.swing.filechooser.FileFilter;

public interface GFileWriter {

    public void selectFile() throws Exception;

    public void write(String str) throws Exception;

    public boolean approved();

    public String getSelectedFilePath();

    public boolean fileExists();
    
    public FileFilter getFileFilter();
}
