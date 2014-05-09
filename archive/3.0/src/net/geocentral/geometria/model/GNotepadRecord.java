/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.Map;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.action.GEraseLineAction;
import net.geocentral.geometria.action.GEraseSelectionAction;
import net.geocentral.geometria.action.GRemoveFigureAction;
import net.geocentral.geometria.action.GRenameFigureAction;
import net.geocentral.geometria.action.GRenamePointAction;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GNotepadRecord {

    private GNotepadVariable variable;

    private GRecordable recordable;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GNotepadRecord() {
    }

    public GNotepadRecord(GNotepadVariable variable, GCalculation calculation) {
        this.variable = variable;
        this.recordable = calculation;
        logger.info(this);
    }

    public GNotepadRecord(GNotepadVariable variable, GMeasurement measurement) {
        this.variable = variable;
        this.recordable = measurement;
        logger.info(this);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        String variableName =
            node.getElementsByTagName("variable").item(0).getTextContent();
        NodeList ns = node.getElementsByTagName("measurement");
        if (ns.getLength() > 0) {
            Element n = (Element)ns.item(0);
            recordable = new GMeasurement();
            recordable.make(n);
            variable = new GNotepadVariable(variableName, null);
        }
        NodeList nns = node.getElementsByTagName("calculation");
        if (nns.getLength() > 0) {
            Element n = (Element)nns.item(0);
            recordable = new GCalculation();
            recordable.make(n);
            variable = new GNotepadVariable(variableName, null);
        }
    }

    public void validate(GDocument document,
            Map<String, GNotepadVariable> variables) throws Exception {
        logger.info(this);
        if (variables.containsKey(variable.getName())) {
            logger.error(variable);
            throw new Exception();
        }
        if (recordable instanceof GMeasurement) {
            if (!((GMeasurement)recordable).isDeprecated()) {
                double value = ((GMeasurement)recordable).getValue(document);
                variable.setValue(value);
            }
        }
        else {
            double value = ((GCalculation)recordable).getValue(variables);
            variable.setValue(value);
        }
        logger.info(this);
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
        buf.append("\n<record>");
        buf.append("\n<variable>");
        buf.append(variable.getName());
        buf.append("</variable>");
        if (recordable instanceof GCalculation)
            recordable.serialize(buf);
        else if (!((GMeasurement)recordable).isDeprecated())
            recordable.serialize(buf);
        else {
            GCalculation calculation =
                new GCalculation(String.valueOf(variable.getValue()));
            calculation.serialize(buf);
        }
        buf.append("\n</record>");
    }

    public void figureRenamed(GRenameFigureAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).figureRenamed(action);
    }

    public void renameFigureUndone(GRenameFigureAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).renameFigureUndone(action);
    }

    public void pointRenamed(GRenamePointAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).pointRenamed(action);
    }

    public void renamePointUndone(GRenamePointAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).renamePointUndone(action);
    }

    public void figureRemoved(GRemoveFigureAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).figureRemoved(action);
    }

    public void removeFigureUndone(GRemoveFigureAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).removeFigureUndone(action);
    }

    public void lineErased(GEraseLineAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).lineErased(action);
    }

    public void eraseLineUndone(GEraseLineAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).eraseLineUndone(action);
    }

    public void selectionErased(GEraseSelectionAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).selectionErased(action);
    }

    public void eraseSelectionUndone(GEraseSelectionAction action) {
        if (recordable instanceof GMeasurement)
            ((GMeasurement)recordable).eraseSelectionUndone(action);
    }

    public GNotepadVariable getVariable() {
        return variable;
    }

    public String getExpression() {
        return recordable.getExpression();
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(variable.getName());
        if (recordable instanceof GCalculation)
            buf.append("=").append(recordable.getExpression());
        else if (!((GMeasurement)recordable).isDeprecated()) {
            buf.append("=")
                .append(((GMeasurement)recordable).getExpressionString());
            GDocument document =
                GDocumentHandler.getInstance().getActiveDocument();
            if (document != null && document.getFigureCount() > 1)
                buf.append(" : ")
                    .append(((GMeasurement)recordable).getFigureName());
        }
        return String.valueOf(buf);
    }
}
