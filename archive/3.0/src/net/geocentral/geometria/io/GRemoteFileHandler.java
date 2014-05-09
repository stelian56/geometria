/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.io;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

abstract public class GRemoteFileHandler {

    public static final String DOCTREE_PATH = "docTree.php";

    protected Element docTreeElement;

    protected URL baseUrl;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRemoteFileHandler(URL baseUrl) {
        logger.info(baseUrl);
        this.baseUrl = baseUrl;
    }

    public void init() throws Exception {
        logger.info(baseUrl);
        setDocTreeElement(baseUrl);
    }
    
    private void setDocTreeElement(URL baseUrl) throws Exception {
        logger.info(baseUrl);
        HttpURLConnection connection;
        InputStream in;
        try {
            URL url = new URL(baseUrl.getProtocol(), baseUrl.getHost(),
                baseUrl.getPort(), baseUrl.getPath() + DOCTREE_PATH);
            logger.info(url);
            connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            in = connection.getInputStream();
        }
        catch (Exception exception) {
            logger.error(baseUrl + ", " + exception);
            throw new Exception(
                    GDictionary.get("CannotConnect", String.valueOf(baseUrl)));
        }
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        try {
            docTreeElement = builder.parse(in).getDocumentElement();
        }
        catch (Exception exception) {
            logger.error(docTreeElement + ", " + exception);
            throw new Exception(GDictionary.get(
                    "CannotReadRepository", String.valueOf(baseUrl)));
        }
        connection.disconnect();
    }
}
