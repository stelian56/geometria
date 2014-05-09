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
