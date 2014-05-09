/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.geocentral.geometria.util.GApplicationManager;
import net.geocentral.geometria.util.GXmlUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;

public abstract class GXmlEntity {

    public static final String PREAMBLE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String APPLICATION_NAMESPACE = "http://geocentral.net";

    protected Schema schema;
    
    protected Element docElement;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public void readSchema() throws Exception {
        logger.info("");
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        LSResourceResolver resourceResolver = GXmlUtils.getLSResourceResolver();
        schemaFactory.setResourceResolver(resourceResolver);
        String schemaFile = getSchemaFile();
        InputStream in = getClass().getResourceAsStream(schemaFile);
        Source schemaSource = new StreamSource(in);
        schema = schemaFactory.newSchema(schemaSource);
        in.close();
    }

    public void validateWithSchema() throws Exception {
        logger.info("");
        try {
            if (schema == null)
                readSchema();
            DOMSource source = new DOMSource(docElement);
            Validator validator = schema.newValidator();
            validator.validate(source);
        }
        catch (Exception exception) {
            logger.error(exception);
            throw new Exception();
        }
    }

    public String getVersion() {
        NodeList ns = docElement.getElementsByTagName("version");
        if (ns.getLength() == 0)
            return null;
        return ns.item(0).getTextContent();
    }
    
    public void serialize(StringBuffer buf) {
        logger.info("");
        serialize(buf, false);
    }

    public abstract void serialize(StringBuffer buf, boolean preamble);

    public abstract String getSchemaFile();

    public Schema getSchema() {
        return schema;
    }
    
    public void make() throws Exception {
        make(docElement);
    }
    
    abstract public void make(Element node) throws Exception; 
    
    public void validateVersion() throws Exception {
        logger.info("");
        String version = getVersion();
        if (version == null) {
            logger.error("Null version");
            throw new Exception();
        }
        String applicationVersion =
            GApplicationManager.getInstance().getVersion();
        if (!version.equals(applicationVersion)) {
            logger.error(version + ", " + applicationVersion);
            throw new Exception();
        }
    }
}
