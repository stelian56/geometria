/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.io.InputStream;

public interface GFileReader {

    public void init() throws Exception;
    
    public void selectFile() throws Exception;

    public InputStream getInputStream() throws Exception;

    public String getSelectedFilePath();

    public String getSelectedFileName();
}
