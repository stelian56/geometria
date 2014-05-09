/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GApplicationManager {

    public final String SCHEMA = "/conf/geometria.xsd";

    public final String FILE = "/conf/geometria.xml";

    private String applicationName;
    
    private String version;

    private String homeUrl;
    
    private static GApplicationManager instance;
    
    private Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GApplicationManager() {}
    
    public static GApplicationManager getInstance() {
        if (instance == null)
            instance = new GApplicationManager();
        return instance;
    }
    
    public void init() throws Exception {
        logger.info("");
        SchemaFactory sf =
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        InputStream in = GApplicationManager.class.getResourceAsStream(SCHEMA);
        Source schemaSource = new StreamSource(in);
        Schema actionSchema = sf.newSchema(schemaSource);
        in.close();
        in = GApplicationManager.class.getResourceAsStream(FILE);
        StreamSource actionSource = new StreamSource(in);
        Validator actionValidator = actionSchema.newValidator();
        actionValidator.validate(actionSource);
        in.close();
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        in = GApplicationManager.class.getResourceAsStream(FILE);
        Element docElement = builder.parse(in).getDocumentElement();
        in.close();
        applicationName =
            docElement.getElementsByTagName("name").item(0).getTextContent();
        version =
            docElement.getElementsByTagName("version").item(0).getTextContent();
        homeUrl =
            docElement.getElementsByTagName("homeUrl").item(0).getTextContent();
        Element node =
            (Element)docElement.getElementsByTagName("dictionary").item(0);
        node = (Element)docElement.getElementsByTagName("font").item(0);
        GGraphicsFactory.getInstance().initFont(node);
        GDictionary.init();
    }
 
    public String getApplicationName() {
        return applicationName;
    }

    public String getVersion() {
        return version;
    }

    public String getHomeUrl() {
        return homeUrl;
    }
}


