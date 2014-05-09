/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import java.io.InputStream;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GOctahedronAction implements GLoggable {

    public static final String FILE = "/gallery/octahedron";

    private String figureName;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute(GDocumentHandler documentHandler,
            boolean quietMode) {
        logger.info(quietMode);
        GSolid solid = new GSolid();
        try {
            InputStream in = getClass().getResourceAsStream(FILE);
            Element docElement = GXmlUtils.read(in);
            in.close();
            solid = new GSolid(docElement);
            solid.make();
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            return false;
        }
        GFigure figure = documentHandler.newFigure(solid);
        figureName = figure.getName();
        if (!quietMode)
            documentHandler.setDocumentModified(true);
        documentHandler.notepadChanged();
        logger.info(figureName);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info("");
        GDocument document = documentHandler.getActiveDocument();
        document.removeFigure(figureName);
        documentHandler.removeFigure(figureName);
        documentHandler.notepadChanged();
        logger.info(figureName);
    }

    public GLoggable clone() {
        GOctahedronAction action = new GOctahedronAction();
        action.figureName = figureName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("CreateOctahedron", figureName);
    }

    public void make(Element node) throws Exception {
        logger.info("");
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<action>");
        buf.append("\n<className>");
        buf.append(this.getClass().getSimpleName());
        buf.append("</className>");
        buf.append("\n</action>");
    }

    public String getShortDescription() {
        return GDictionary.get("createOctahedron", figureName);
    }
}
