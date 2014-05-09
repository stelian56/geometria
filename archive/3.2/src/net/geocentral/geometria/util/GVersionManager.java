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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GVersionManager {

    private static final String FILE = "/conf/versions.xml"; 
    
    private String vendor = "http://geocentral.net/geometria"; // Overwritten with application package data
    
    private String applicationName = "Geometria"; // Overwritten with application package data
    
    private String applicationVersion = "3.2"; // Overwritten with application package data
    
    private String svnRevision = "Unknown"; // Overwritten with application package data
    
    private Map<String, GVersionHistoryItem> versionHistory;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private static GVersionManager instance;
    
    private GVersionManager() {
        loadVersionHistory();
        loadApplicationData();
    }

    public static GVersionManager getInstance() {
        if (instance == null) {
            instance = new GVersionManager();
        }
        return instance;
    }
    
    private void loadVersionHistory() {
        versionHistory = new HashMap<String, GVersionHistoryItem>();
        InputStream in = GVersionManager.class.getResourceAsStream(FILE);
        Element docElement = null;
        try {
            docElement = GXmlUtils.read(in);
        }
        catch (Exception exception) {}
        NodeList nodes = docElement.getElementsByTagName("version");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element)nodes.item(i);
            GVersionHistoryItem versionHistoryItem = new GVersionHistoryItem();
            versionHistoryItem.make(node);
            String version = versionHistoryItem.getVersion();
            versionHistory.put(version, versionHistoryItem);
        }
    }
    
    private void loadApplicationData() {
        logger.info("");
        Package p = GVersionManager.class.getPackage();
        String s;
        vendor = (s = p.getSpecificationVendor()) != null ? s : vendor;
        applicationName = (s = p.getSpecificationTitle()) != null ? s : applicationName;
        applicationVersion = (s = p.getSpecificationVersion()) != null ? s : applicationVersion;
        svnRevision = (s = p.getImplementationVersion()) != null ? s : svnRevision;
    }
 
    public boolean versionExists(String version) {
        return versionHistory.containsKey(version);
    }
    
    public String getSolidSchema(String version) {
        return versionHistory.get(version).getSolidSchema();
    }
    
    public String getProblemSchema(String version) {
        return versionHistory.get(version).getProblemSchema();
    }

    public String getSolutionSchema(String version) {
        return versionHistory.get(version).getSolutionSchema();
    }

    public String getOptionsSchema(String version) {
        return versionHistory.get(version).getOptionsSchema();
    }

    public String getVendor() {
        return vendor;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public String getSvnRevision() {
        return svnRevision;
    }
}
