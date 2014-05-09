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
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.model.GOptions;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GOptionsManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract public class GRemoteFileHandler {

    public static final String DOCTREE_PATH = "docTree.php";

    protected Element docTreeElement;

    protected URL baseUrl;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRemoteFileHandler() {
    }
    
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
        String urlString = String.format("%s%s", String.valueOf(baseUrl), DOCTREE_PATH);
        try {
            URL url = new URL(urlString);
            logger.info(url);
            connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            in = connection.getInputStream();
        }
        catch (Exception exception) {
            logger.error(baseUrl + ", " + exception);
            throw new Exception(GDictionary.get("CannotConnect", urlString));
        }
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        try {
            Element node = builder.parse(in).getDocumentElement();
            NodeList ns = node.getElementsByTagName("dir");
            for (int i = 0; i < ns.getLength(); i++) {
                Element nn = (Element)ns.item(i);
                String language = nn.getAttribute("name");
                GOptions options = GOptionsManager.getInstance().getOptions();
                if (options.getLanguage().equals(language)) {
                    docTreeElement = nn;
                }
            }
        }
        catch (Exception exception) {
            logger.error(docTreeElement + ", " + exception);
            throw new Exception(GDictionary.get("CannotReadSamples", String.valueOf(baseUrl)));
        }
        connection.disconnect();
    }
}
