/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.Reader;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.action.GOpenFigureAction;
import net.geocentral.geometria.action.GOpenProblemAction;
import net.geocentral.geometria.action.GOpenSolutionAction;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.GXmlEntity;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class GDragAndDropHandler extends TransferHandler {

    DataFlavor dataFlavor = DataFlavor.stringFlavor;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");
    
    public void init(JComponent target) throws Exception {
        logger.info("");
        target.setTransferHandler(this);
    }

    public boolean canImport(TransferHandler.TransferSupport info) {
        return info.isDataFlavorSupported(dataFlavor);
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        Transferable transferable = info.getTransferable();
        if (!transferable.isDataFlavorSupported(dataFlavor)) {
            return false;
        }
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        String filePath;
        GXmlEntity xmlEntity;
        try {
            Reader reader = dataFlavor.getReaderForText(transferable);
            int c;
            StringBuffer buf = new StringBuffer();
            while ((c = reader.read()) >= 0) {
                buf.append((char)c);
            }
            String uri = String.valueOf(buf).trim();
            InputSource source = new InputSource(uri);
            xmlEntity = GXmlUtils.readXmlEntity(source);
            if (!(xmlEntity instanceof GDocument) && !(xmlEntity instanceof GSolid)) {
                return false;
            }
            filePath = new File(new URI(uri)).getPath();
        } 
        catch (Exception exception) {
            return false;
        }
        if (xmlEntity instanceof GProblem) {
            if (!documentHandler.onCloseDocument()) {
                return false;
            }
            GOpenProblemAction action = new GOpenProblemAction();
            action.execute((GProblem)xmlEntity);
            action.setFilePath(filePath);
            documentHandler.updateActionHandlerStates();
            return true;
        }
        if (xmlEntity instanceof GSolution) {
            if (!documentHandler.onCloseDocument()) {
                return false;
            }
            GOpenSolutionAction action = new GOpenSolutionAction();
            action.execute((GSolution)xmlEntity);
            action.setFilePath(filePath);
            documentHandler.updateActionHandlerStates();
            return true;
        }
        if (xmlEntity instanceof GSolid) {
            GDocument document = documentHandler.getActiveDocument();
            if (document instanceof GProblem) {
                GOpenFigureAction action = new GOpenFigureAction();
                action.execute((GSolid)xmlEntity);
                action.setFilePath(filePath);
                documentHandler.addAction(action);
                documentHandler.setDocumentModified(true);
                documentHandler.updateActionHandlerStates();
                return true;
            }
        }
        return false;
    }

    private static final long serialVersionUID = 1L;
}

