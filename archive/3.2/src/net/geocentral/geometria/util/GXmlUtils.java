/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.geocentral.geometria.converter.GConverter;
import net.geocentral.geometria.model.GOptions;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolid;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.GXmlEntity;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GXmlUtils {

    public enum XmlEntityType { SOLID, PROBLEM, SOLUTION, OPTIONS };
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public static GSolid readSolid(InputSource source) throws Exception {
        return (GSolid)readXmlEntity(source, XmlEntityType.SOLID);
    }
    
    public static GProblem readProblem(InputSource source) throws Exception {
        return (GProblem)readXmlEntity(source, XmlEntityType.PROBLEM);
    }
    
    public static GSolution readSolution(InputSource source) throws Exception {
        return (GSolution)readXmlEntity(source, XmlEntityType.SOLUTION);
    }

    public static GOptions readOptions(InputSource source) throws Exception {
        return (GOptions)readXmlEntity(source, XmlEntityType.OPTIONS);
    }

    public static GXmlEntity readXmlEntity(InputSource source) throws Exception {
        return readXmlEntity(source, null);
    }
    
    private static GXmlEntity readXmlEntity(InputSource source, XmlEntityType targetType) throws Exception {
        Element docElement;
        try {
            docElement = read(source);
        }
        catch (SAXException exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
        String nodeName = docElement.getNodeName();
        NodeList nodes = docElement.getElementsByTagName("version");
        if (nodes.getLength() < 1) {
            logger.error("No version");
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
        String version = nodes.item(0).getTextContent();
        GVersionManager versionManager = GVersionManager.getInstance();
        if (!versionManager.versionExists(version)) {
            logger.error(String.format("Unknown version %s", version));
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
        GXmlEntity xmlEntity = null;
        if (targetType == null) {
            if ("solid".equals(nodeName)) {
                xmlEntity = new GSolid();
            }
            else if ("problem".equals(nodeName)) {
                xmlEntity = new GProblem();
            }            
            else if ("solution".equals(nodeName)) {
                xmlEntity = new GSolution();
            }            
            else if ("options".equals(nodeName)) {
                xmlEntity = new GOptions();
            }
            else {
                logger.error(String.format("Unrecognized root element: %s", nodeName));
                throw new NotAGeometriaFileException();
            }
        }
        else {
            switch (targetType) {
            case SOLID:
                if ("solid".equals(nodeName)) {
                    xmlEntity = new GSolid();
                    break;
                }
                if ("problem".equals(nodeName)) {
                    logger.error("Problem file, open figure");
                    throw new Exception(GDictionary.get("FileContainsProblemFigure"));
                }            
                if ("solution".equals(nodeName)) {
                    logger.error("Solution file, open figure");
                    throw new Exception(GDictionary.get("FileContainsSolutionFigure"));
                }            
                if ("options".equals(nodeName)) {
                    logger.error("Options file, open figure");
                }
                else {
                    logger.error(String.format("Unrecognized root element: %s", nodeName));
                    throw new NotAGeometriaFileException();
                }
                throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
            case PROBLEM:
                if ("problem".equals(nodeName)) {
                    xmlEntity = new GProblem();
                    break;
                }
                if ("solid".equals(nodeName)) {
                    logger.error("Figure file, open problem");
                    throw new Exception(GDictionary.get("FileContainsFigureProblem"));
                }            
                if ("solution".equals(nodeName)) {
                    logger.error("Solution file, open problem");
                    throw new Exception(GDictionary.get("FileContainsSolutionProblem"));
                }            
                if ("options".equals(nodeName)) {
                    logger.error("Options file, open problem");
                }
                else {
                    logger.error(String.format("Unrecognized root element: %s", nodeName));
                    throw new NotAGeometriaFileException();
                }
                throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
            case SOLUTION:
                if ("solution".equals(nodeName)) {
                    xmlEntity = new GSolution();
                    break;
                }
                if ("solid".equals(nodeName)) {
                    logger.error("Figure file, open solution");
                    throw new Exception(GDictionary.get("FileContainsFigureSolution"));
                }            
                if ("problem".equals(nodeName)) {
                    logger.error("Problem file, open solution");
                    throw new Exception(GDictionary.get("FileContainsProblemSolution"));
                }            
                if ("options".equals(nodeName)) {
                    logger.error("Options file, open solution");
                }
                else {
                    logger.error(String.format("Unrecognized root element: %s", nodeName));
                    throw new NotAGeometriaFileException();
                }
                throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
            case OPTIONS:
                if ("options".equals(nodeName)) {
                    xmlEntity = new GOptions();
                    break;
                }
                if ("solid".equals(nodeName)) {
                    logger.error("Figure file, open options");
                }            
                if ("problem".equals(nodeName)) {
                    logger.error("Problem file, open options");
                }            
                if ("solution".equals(nodeName)) {
                    logger.error("Solution file, open options");
                }
                else {
                    logger.error(String.format("Unrecognized root element: %s", nodeName));
                    throw new NotAGeometriaFileException();
                }
                throw new Exception();
            }
        }
        String schemaFile = xmlEntity.getSchemaFile(version);
        logger.info(String.format("Schema file %s", schemaFile));
        try {
            validate(docElement, schemaFile);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
        String applicationVersion = versionManager.getApplicationVersion();
        if (version.compareTo(applicationVersion) > 0) {
            logger.error(String.format("File version %s, application version %s", version, applicationVersion));
            throw new Exception(GDictionary.get("UnsupportedFileVersion", version));
        }
        if (version.compareTo(applicationVersion) < 0) {
            logger.warn(String.format("Converting file from %s to %s", version, applicationVersion));
            try {
                GConverter converter = new GConverter();
                if ("problem".equals(nodeName)) {
                    docElement = converter.convertProblem(docElement);
                }
                if ("solution".equals(nodeName)) {
                    docElement = converter.convertSolution(docElement);
                }
            }
            catch (Exception exception) {
                logger.error(GStringUtils.stackTraceToString(exception));
                throw new Exception(GDictionary.get("CannotConvertFile", version, applicationVersion));
            }
        }
        try {
            xmlEntity.make(docElement);
        }
        catch (Exception exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
        return xmlEntity;
    }
    
    public static Element read(InputStream in) throws Exception {
        logger.info("");
        InputSource source = new InputSource(in);
        return read(source);
    }
    
    private static Element read(InputSource source) throws Exception {
        logger.info("");
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        try {
            return builder.parse(source).getDocumentElement();
        }
        catch (SAXException exception) {
            logger.error(GStringUtils.stackTraceToString(exception));
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
    }

    private static void validate(Element docElement, String schemaFile) throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        LSResourceResolver resourceResolver = GXmlUtils.getLSResourceResolver();
        sf.setResourceResolver(resourceResolver);
        InputStream in = GXmlUtils.class.getResourceAsStream(schemaFile);
        Source schemaSource = new StreamSource(in);
        Schema schema = sf.newSchema(schemaSource);
        in.close();
        DOMSource source = new DOMSource(docElement);
        Validator validator = schema.newValidator();
        validator.validate(source);
    }

    private static LSResourceResolver getLSResourceResolver() throws Exception {
        logger.info("");
        LSResourceResolver resourceResolver = new LSResourceResolver() {
            public LSInput resolveResource(String type, String namespaceUri,
                    String publicId, String systemId, String baseUri) {
                try {
                    final InputStream in = GXmlEntity.class.getResource(
                            "/" + systemId).openStream();
                    LSInputAdapter adapter = new LSInputAdapter(in);
                    return adapter;
                }
                catch (Exception exception) {
                    logger.error(GStringUtils.stackTraceToString(exception));
                    return null;
                }
            }
        };
        return resourceResolver;
    }

    static class LSInputAdapter implements LSInput {

        private InputStream in;
        
        public LSInputAdapter(InputStream in) {
            this.in = in;
        }
        
        public InputStream getByteStream() {
            return in;
        }

        public String getBaseURI() {return null;}
        public boolean getCertifiedText() {return false;}
        public Reader getCharacterStream() {return null;}
        public String getEncoding() {return null;}
        public String getPublicId() {return null;}
        public String getStringData() {return null;}
        public String getSystemId() {return null;}
        public void setBaseURI(String baseURI) {}
        public void setByteStream(InputStream byteStream) {}
        public void setCertifiedText(boolean certifiedText) {}
        public void setCharacterStream(Reader characterStream) {}
        public void setEncoding(String encoding) {}
        public void setPublicId(String publicId) {}
        public void setStringData(String stringData) {}
        public void setSystemId(String systemId) {}
    }
}
