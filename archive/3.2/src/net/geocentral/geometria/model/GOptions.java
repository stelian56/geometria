package net.geocentral.geometria.model;

import java.awt.Font;

import net.geocentral.geometria.util.GVersionManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class GOptions extends GXmlEntity {

    private static final Font DEFAULT_FONT = new Font("Sans-serif", Font.BOLD, 12); 
    
    private static final String DEFAULT_LANGUAGE = "en";

    private Font font;
    
    private String language;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GOptions() {
    }
    
    public Font getFont() {
        return font;
    }
    
    public void setFontSize(int fontSize) {
        String fontName = font.getName();
        int fontStyle = font.getStyle();
        font = new Font(fontName, fontStyle, fontSize);
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public void make(Element node, GXmlEntity parent) throws Exception {
        logger.info("");
        makeVersion(node, parent);
        makeFont(node);
        makeLanguage(node);
    }

    private void makeFont(Element node) throws Exception {
        String fontName = node.getElementsByTagName("name").item(0).getTextContent();
        String fontStyleString = node.getElementsByTagName("style").item(0).getTextContent();
        int fontStyle;
        if (fontStyleString.equalsIgnoreCase("bold")) {
            fontStyle = Font.BOLD;
        }
        else if (fontStyleString.equalsIgnoreCase("italic")) {
            fontStyle = Font.ITALIC;
        }
        else {
            fontStyle = Font.PLAIN;
        }
        int fontSize = Integer.valueOf(node.getElementsByTagName("size").item(0).getTextContent());
        font = new Font(fontName, fontStyle, fontSize);
    }

    private void makeLanguage(Element node) throws Exception {
        language = node.getElementsByTagName("language").item(0).getTextContent();
    }

    public void serialize(StringBuffer buf, boolean preamble) {
        logger.info("");
        if (preamble) {
            buf.append(PREAMBLE)
                .append("\n<options xmlns=\"")
                .append(APPLICATION_NAMESPACE)
                .append("\">")
                .append("\n<version>")
                .append(GVersionManager.getInstance().getApplicationVersion())
                .append("</version>");
        }
        else {
            buf.append("\n<options>");
        }
        String fontStyleString; 
        switch (font.getStyle()) {
        case Font.BOLD:
            fontStyleString = "bold";
            break;
        case Font.ITALIC:
            fontStyleString = "italic";
            break;
        default:
            fontStyleString = "plain";
        }
        buf.append("\n<font>")
            .append("\n<name>")
            .append(font.getName())
            .append("</name>")
            .append("\n<style>")
            .append(fontStyleString)
            .append("</style>")
            .append("\n<size>")
            .append(font.getSize())
            .append("</size>")
            .append("\n</font>")
            .append("\n<language>")
            .append(language)
            .append("</language>")
            .append("\n</options>");
    }

    public void setDefaults() {
        logger.info("Setting default options");
        font = DEFAULT_FONT;
        language = System.getProperty("language");
        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }
    }
    
    public String getSchemaFile(String version) {
        return GVersionManager.getInstance().getOptionsSchema(version);
    }
}
