/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.geocentral.geometria.model.GLabelFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GConverter {

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public Element convertFigure(Element docElement) throws Exception {
        return docElement;
    }
    
    public Element convertProblem(Element docElement) {
        logger.info("");
        return docElement;
    }
    
    public Element convertSolution(Element docElement) {
        logger.info("");
        Document doc = docElement.getOwnerDocument();
        GLabelFactory labelFactory = GLabelFactory.getInstance();
        List<String> figureNames = new ArrayList<String>();
        NodeList nodes = ((Element)docElement.getElementsByTagName("problem").item(0)).getElementsByTagName("figure");
        for (int i = 0; i < nodes.getLength(); i++) {
            String figureName = ((Element)nodes.item(i)).getElementsByTagName("name").item(0).getTextContent();
            figureNames.add(figureName);
        }
        nodes = ((Element)docElement.getElementsByTagName("log").item(0)).getElementsByTagName("action");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element actionNode = (Element)nodes.item(i);
            Element classNameNode = (Element)actionNode.getElementsByTagName("className").item(0);
            String className = classNameNode.getTextContent();
            Matcher prismMatcher = Pattern.compile("GPrism([3-6])Action").matcher(className);
            Matcher pyramidMatcher = Pattern.compile("GPyramid([3-6])Action").matcher(className);
            if (prismMatcher.matches()) {
                logger.info(String.format("Convert action %s", className));
                String sideCount = prismMatcher.group(1);
                classNameNode.setTextContent("GPrismAction");
                Element sideCountNode = doc.createElement("sideCount");
                sideCountNode.setTextContent(sideCount); 
                actionNode.appendChild(sideCountNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (classNameNode.getTextContent().equals("GPrismNAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPrismAction");
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (pyramidMatcher.matches()) {
                logger.info(String.format("Convert action %s", className));
                String sideCount = pyramidMatcher.group(1);
                classNameNode.setTextContent("GPyramidAction");
                Element sideCountNode = doc.createElement("sideCount");
                sideCountNode.setTextContent(sideCount); 
                actionNode.appendChild(sideCountNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GPyramidNAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPyramidAction");
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GTetrahedronAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPlatonicAction");
                Element typeNode = doc.createElement("type");
                typeNode.setTextContent("tetrahedron"); 
                actionNode.appendChild(typeNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GCubeAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPlatonicAction");
                Element typeNode = doc.createElement("type");
                typeNode.setTextContent("cube"); 
                actionNode.appendChild(typeNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GOctahedronAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPlatonicAction");
                Element typeNode = doc.createElement("type");
                typeNode.setTextContent("octahedron"); 
                actionNode.appendChild(typeNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GDodecahedronAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPlatonicAction");
                Element typeNode = doc.createElement("type");
                typeNode.setTextContent("dodecahedron"); 
                actionNode.appendChild(typeNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GIcosahedronAction")) {
                logger.info(String.format("Convert action %s", className));
                classNameNode.setTextContent("GPlatonicAction");
                Element typeNode = doc.createElement("type");
                typeNode.setTextContent("icosahedron"); 
                actionNode.appendChild(typeNode);
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (classNameNode.getTextContent().equals("GCloneFigureAction")) {
                logger.info(String.format("Convert action %s", className));
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GCutAction")) {
                logger.info(String.format("Convert action %s", className));
                Element figure1NameNode = doc.createElement("figure1Name");
                String figure1Name = labelFactory.newFigureName(figureNames);
                figureNames.add(figure1Name);
                figure1NameNode.setTextContent(figure1Name); 
                actionNode.appendChild(figure1NameNode);
                Element figure2NameNode = doc.createElement("figure2Name");
                String figure2Name = labelFactory.newFigureName(figureNames); 
                figure2NameNode.setTextContent(figure2Name); 
                actionNode.appendChild(figure2NameNode);
                figureNames.add(figure2Name);
            }
            else if (className.equals("GJoinAction")) {
                logger.info(String.format("Convert action %s", className));
                String figureName = labelFactory.newFigureName(figureNames);
                figureNames.add(figureName);
            }
            else if (className.equals("GRemoveFigure")) {
                logger.info(String.format("Convert action %s", className));
                String figureName = classNameNode.getElementsByTagName("figureName").item(0).getTextContent();
                figureNames.remove(figureName);
            }
            else if (className.equals("GRenameFigure")) {
                logger.info(String.format("Convert action %s", className));
                String oldFigureName = classNameNode.getElementsByTagName("oldFigureName").item(0).getTextContent();
                String newFigureName = classNameNode.getElementsByTagName("newFigureName").item(0).getTextContent();
                figureNames.remove(oldFigureName);
                figureNames.add(newFigureName);
            }
        }
        return docElement;
    }
    
    public Element convertOptions(Element docElement) {
        return docElement;
    }
}
