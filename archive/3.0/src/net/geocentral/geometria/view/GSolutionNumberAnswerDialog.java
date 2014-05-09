/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GSolutionAnswerAction;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.model.answer.GNumberAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GSolutionNumberAnswerDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GSolution document;

    private GSolutionAnswerAction action;

    private GNumberAnswer problemAnswer;

    private JTextField inputTextField;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSolutionNumberAnswerDialog(Frame ownerFrame, GSolution document,
            GSolutionAnswerAction action, GNumberAnswer problemAnswer) {
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
        inputTextField =
            GGraphicsFactory.getInstance().createVariableInput(null);
        inputPane.add(inputTextField);
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
        GNotepadRecord record = document.getNotepad().getSelectedRecord();
        if (record != null) {
            String variableName = record.getVariable().getName();
            inputTextField.setText(variableName);
        }
    }

    public void ok() {
        logger.info("");
        GNumberAnswer answer = null;
        answer = new GNumberAnswer();
        try {
            answer.validateInput(inputTextField.getText().trim(), document);
        }
        catch (Exception exception) {
            GGraphicsFactory.getInstance().showErrorDialog( this, exception.getMessage());
            return;
        }
        action.setInput(inputTextField.getText().trim(), null);
        if (!problemAnswer.verify(answer, GMath.EPSILON)) {
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
