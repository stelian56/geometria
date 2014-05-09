/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model.answer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.util.GStringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GMultipleChoiceAnswer implements GAnswer {

    private LinkedHashMap<String, Boolean> options;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GMultipleChoiceAnswer() {}
    
    public GMultipleChoiceAnswer(LinkedHashMap<String, Boolean> options) {
        logger.info(options);
        this.options = options;
    }

    public void make(Element node, GProblem document) throws Exception {
        logger.info("");
        options = new LinkedHashMap<String, Boolean>();
        NodeList ns = node.getElementsByTagName("option");
        if (ns.getLength() < 1) {
            logger.error("No options");
            throw new Exception();
        }
        for (int i = 0; i < ns.getLength(); i++) {
            Element n = (Element)ns.item(i);
            String option = n.getTextContent().trim();
            if (option.isEmpty()) {
                logger.error(String.format("Empty option at index %s", i));
                throw new Exception();
            }
            if (document.isLocked()) {
                String decodedOption = GStringUtils.decode(option);
                if (decodedOption == null) {
                    logger.error(String.format("Unencoded multiple choice answer option %s in locked problem", option));
                    throw new Exception();
                }
                boolean selected = decodedOption.charAt(decodedOption.length() - 1) == '1';
                options.put(decodedOption.substring(0, decodedOption.length() - 1), selected);
            }
            else {
                String attributeValue = n.getAttribute("selected");
                boolean selected = false;
                if (!attributeValue.isEmpty()) {
                    try {
                        selected = Boolean.valueOf(attributeValue);
                    }
                    catch (Exception exception) {
                        logger.error(String.format("Invalid option attribute: %s", attributeValue));
                        throw new Exception();
                    }
                }
                options.put(option, selected);
            }
        }
    }

    public boolean validate(String valueString, String figureName, GDocument document) {
        return options.containsValue(true);
    }

    public boolean verify(int selectedOptionIndex) {
        int optionIndex = 0;
        for (Entry<String, Boolean> entry : options.entrySet()) {
            boolean selected = entry.getValue();
            if (selected) {
                return optionIndex == selectedOptionIndex;
            }
            optionIndex++;
        }
        return false;
    }

    public void serialize(StringBuffer buf, boolean lock) {
        logger.info("");
        buf.append("\n<answer>");
        buf.append("\n<type>multipleChoice</type>");
        for (Entry<String, Boolean> entry : options.entrySet()) {
            String option = entry.getKey();
            buf.append("\n<option");
            boolean selected = entry.getValue();
            if (selected && !lock) {
                buf.append(" selected=\"true\"");
            }
            buf.append(String.format(">", option));
            if (lock) {
                String encodedOption = GStringUtils.encode(option + (selected ? 1 : 0));
                buf.append(encodedOption);
            }
            else {
                buf.append(option);
            }
            buf.append(String.format("</option>", option));
        }
        buf.append("\n</answer>");
    }

    public Map<String, Boolean> getOptions() {
        return options;
    }

    public static Map<String, Boolean> getDefaultOptions() {
        Map<String, Boolean> defaultOptions = new LinkedHashMap<String, Boolean>();
        defaultOptions.put("",  true);
        return defaultOptions;
    }
    
    public String toString() {
        for (Entry<String, Boolean> entry : options.entrySet()) {
            boolean selected = entry.getValue();
            if (selected) {
                return entry.getKey();
            }
        }
        return null;
    }
}
