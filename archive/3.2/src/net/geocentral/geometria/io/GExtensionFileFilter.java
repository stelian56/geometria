/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GExtensionFileFilter extends FileFilter {

    private String extension;
    
    public GExtensionFileFilter(String extension) {
        this.extension = extension;
    }
    
    public boolean accept(File file) {
        return file.isDirectory() || file.getName().endsWith("." + extension);
    }

    public String getDescription() {
        return "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
