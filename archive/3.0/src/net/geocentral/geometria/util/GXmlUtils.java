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
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.geocentral.geometria.model.GXmlEntity;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class GXmlUtils {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public static Element read(InputStream in) throws Exception {
        logger.info("");
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        try {
            return builder.parse(in).getDocumentElement();
        }
        catch (SAXException exception) {
            logger.error(exception);
            throw new Exception(GDictionary.get("FileCorruptedSeeLog"));
        }
    }


    public static LSResourceResolver getLSResourceResolver() throws Exception {
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
                    exception.printStackTrace();
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
