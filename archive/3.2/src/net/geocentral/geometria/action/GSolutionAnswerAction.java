/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.action;

import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.model.answer.GConditionPlaneAnswer;
import net.geocentral.geometria.model.answer.GFixedPlaneAnswer;
import net.geocentral.geometria.model.answer.GLineSetAnswer;
import net.geocentral.geometria.model.answer.GMultipleChoiceAnswer;
import net.geocentral.geometria.model.answer.GNumberAnswer;
import net.geocentral.geometria.model.answer.GPointSetAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GStringUtils;
import net.geocentral.geometria.view.GSolutionConditionPlaneAnswerDialog;
import net.geocentral.geometria.view.GSolutionFixedPlaneAnswerDialog;
import net.geocentral.geometria.view.GSolutionLineSetAnswerDialog;
import net.geocentral.geometria.view.GSolutionMultipleChoiceAnswerDialog;
import net.geocentral.geometria.view.GSolutionNumberAnswerDialog;
import net.geocentral.geometria.view.GSolutionPointSetAnswerDialog;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GSolutionAnswerAction implements GLoggable, GActionWithHelp, GFigureAction {

    private String valueString;

    private String figureName;
    
    private String helpId;

    private String comments;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public boolean execute() {
        return execute(false);
    }

    public boolean execute(boolean silent) {
        logger.info(silent);
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        GSolution document = (GSolution) documentHandler.getMasterSolution();
        GProblem problem = document.getProblem();
        GAnswer problemAnswer = problem.getAnswer();
        if (silent) {
            boolean validated = problemAnswer.validate(valueString, figureName, document);
            if (validated) {
                documentHandler.setActiveDocument(document);
                documentHandler.documentChanged();
            }
            return validated;
        }
        else {
            if (problemAnswer instanceof GNumberAnswer) {
                GSolutionNumberAnswerDialog dialog = new GSolutionNumberAnswerDialog(documentHandler.getOwnerFrame(),
                        document, this, (GNumberAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
            else if (problemAnswer instanceof GPointSetAnswer) {
                GSolutionPointSetAnswerDialog dialog = new GSolutionPointSetAnswerDialog(
                        documentHandler.getOwnerFrame(), document, this, (GPointSetAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
            else if (problemAnswer instanceof GLineSetAnswer) {
                GSolutionLineSetAnswerDialog dialog = new GSolutionLineSetAnswerDialog(
                        documentHandler.getOwnerFrame(), document, this, (GLineSetAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
            else if (problemAnswer instanceof GFixedPlaneAnswer) {
                GSolutionFixedPlaneAnswerDialog dialog = new GSolutionFixedPlaneAnswerDialog(
                        documentHandler.getOwnerFrame(), document, this, (GFixedPlaneAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
            else if (problemAnswer instanceof GConditionPlaneAnswer) {
                GSolutionConditionPlaneAnswerDialog dialog = new GSolutionConditionPlaneAnswerDialog(
                        documentHandler.getOwnerFrame(), document, this, (GConditionPlaneAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
            else if (problemAnswer instanceof GMultipleChoiceAnswer) {
                GSolutionMultipleChoiceAnswerDialog dialog = new GSolutionMultipleChoiceAnswerDialog(
                        documentHandler.getOwnerFrame(), this, (GMultipleChoiceAnswer)problemAnswer);
                dialog.setVisible(true);
                if (!dialog.getResult()) {
                    return false;
                }
            }
        }
        documentHandler.setDocumentModified(true);
        logger.info(valueString);
        return true;
    }

    public void undo(GDocumentHandler documentHandler) {
        logger.info(valueString + " : " + figureName);
    }

    public GLoggable clone() {
        GSolutionAnswerAction action = new GSolutionAnswerAction();
        action.valueString = valueString;
        action.figureName = figureName;
        return action;
    }

    public String toLogString() {
        return GDictionary.get("CorrectAnswer", valueString);
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void make(Element node) throws Exception {
        logger.info("");
        NodeList ns = node.getElementsByTagName("value");
        if (ns.getLength() == 0) {
            logger.info("No value");
            throw new Exception();
        }
        valueString = ns.item(0).getTextContent();
        ns = node.getElementsByTagName("figureName");
        if (ns.getLength() > 0) {
            figureName = ns.item(0).getTextContent();
        }
        ns = node.getElementsByTagName("comments");
        if (ns.getLength() > 0) {
            String s = ns.item(0).getTextContent();
            comments = GStringUtils.fromXml(s);
        }
    }

    public void serialize(StringBuffer buf) {
        logger.info("");
            buf.append("\n<action>")
            .append("\n<className>")
            .append(this.getClass().getSimpleName())
            .append("</className>")
            .append("\n<value>")
            .append(valueString)
            .append("</value>");
        if (figureName != null) {
            buf.append("\n<figureName>")
                .append(figureName)
                .append("</figureName>");
        }
        if (comments != null) {
            String s = GStringUtils.toXml(comments);
            buf.append("\n<comments>")
                .append(s)
                .append("</comments>");
        }
        buf.append("\n</action>");
    }

    public void setInput(String valueString, String figureName) {
        logger.info(valueString + " : " + figureName);
        this.valueString = valueString;
        this.figureName = figureName;
    }

    public String getShortDescription() {
        return GDictionary.get("correctAnswer");
    }

    public String getFigureName() {
        return figureName;
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }
}
