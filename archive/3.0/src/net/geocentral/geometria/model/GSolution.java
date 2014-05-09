/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import net.geocentral.geometria.util.GApplicationManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GSolution extends GDocument {

    public static final String SCHEMA = "/conf/solution.xsd";

    private GDocumentEnvelope envelope;

    private GProblem problem;

    private GLog log;

    private Collection<GFigure> importedFigures;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSolution() {
        envelope = new GDocumentEnvelope();
        problem = new GProblem();
        log = new GLog();
    }

    public GSolution(Element node) {
        this();
        docElement = node;
    }

    public GSolution(GProblem problem) {
        this.problem = problem;
        envelope = new GDocumentEnvelope();
        log = new GLog();
    }

    public GSolution(GSolution masterSolution) {
        problem = masterSolution.getProblem();
        importFigures(problem);
    }

    public void importFigures(GDocument document) {
        logger.info("");
        removeAllFigures();
        importedFigures = new LinkedHashSet<GFigure>();
        for (Iterator<GFigure> it = document.getFigureIterator();
                it.hasNext();) {
            GFigure figure = it.next();
            GFigure f = null;
            f = figure.clone();
            f.setTransparent(true);
            f.setLabelled(true);
            addFigure(f);
            importedFigures.add(f);
        }
        setSelectedFigure(0);
    }

    public void make(Element node) throws Exception {
        logger.info("");
        Element n = (Element)node.getElementsByTagName("envelope").item(0);
        envelope.make(n);
        n = (Element)node.getElementsByTagName("problem").item(0);
        problem.make(n);
        n = (Element)node.getElementsByTagName("log").item(0);
        log.make(n);
    }

    public void serialize(StringBuffer buf, boolean preamble) {
        logger.info("");
        if (preamble) {
            buf.append(PREAMBLE);
            buf.append("\n<solution xmlns=\"");
            buf.append(APPLICATION_NAMESPACE);
            buf.append("\">");
            buf.append("\n<version>");
            buf.append(GApplicationManager.getInstance().getVersion());
            buf.append("</version>");
        }
        else
            buf.append("\n<solution>");
        envelope.serialize(buf);
        problem.serialize(buf);
        log.serialize(buf);
        buf.append("\n</solution>");
   }
    
    public GDocumentEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(GDocumentEnvelope envelope) {
        logger.info(envelope);
        this.envelope = envelope;
    }

    public GProblem getProblem() {
        return problem;
    }

    public String getSchemaFile() {
        return SCHEMA;
    }

    public GLog getLog() {
        return log;
    }

    public boolean isImported(GFigure figure) {
        return importedFigures.contains(figure);
    }
}
