/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Frame;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GSolutionAnswerAction;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GPoint3d;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.answer.GLineSetAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;
import net.geocentral.geometria.util.GLineSetUtils;

import org.apache.log4j.Logger;

public class GSolutionLineSetAnswerDialog extends JDialog implements
        GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GSolution document;

    private GSolutionAnswerAction action;

    private GLineSetAnswer problemAnswer;

    private JTextField inputTextField;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSolutionLineSetAnswerDialog(Frame ownerFrame, GSolution document, GSolutionAnswerAction action,
            GLineSetAnswer problemAnswer) {
        super(ownerFrame, true);
        logger.info("");
        this.document = document;
        this.action = action;
        this.problemAnswer = problemAnswer;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("Answer"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
        inputPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPane.add(new JLabel(GDictionary.get("Answer")));
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        inputTextField = GGraphicsFactory.getInstance().createVariableInput(null);
        inputPane.add(inputTextField);
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
        String[] selectedLines = GLineSetUtils.fromSelection(document);
        if (selectedLines.length > 0) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < selectedLines.length; i++) {
                buf.append(selectedLines[i]);
                if (i < selectedLines.length - 1) {
                    buf.append(",");
                }
            }
            inputTextField.setText(String.valueOf(buf));
        }
    }

    public void ok() {
        logger.info("");
        List<GPoint3d[]> lines;
        GFigure figure = document.getSelectedFigure();
        try {
            lines = GLineSetUtils.fromString(inputTextField.getText().trim(), figure);
        }
        catch (Exception exception) {
            GGraphicsFactory.getInstance().showErrorDialog( this, exception.getMessage());
            return;
        }
        String figureName = figure.getName();
        action.setInput(inputTextField.getText().trim(), figureName);
        if (!problemAnswer.verify(lines, document)) {
            dispose();
            GGraphicsFactory.getInstance().showAnswerEvaluation(false);
            return;
        }
        option = OK_OPTION;
        result = true;
        dispose();
        GGraphicsFactory.getInstance().showAnswerEvaluation(true);
    }

    public void cancel() {
        logger.info("");
        dispose();
    }

    public int getOption() {
        return option;
    }

    public boolean getResult() {
        return result;
    }

    private static final long serialVersionUID = 1L;
}
