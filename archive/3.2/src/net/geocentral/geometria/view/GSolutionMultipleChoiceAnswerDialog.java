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
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GSolutionAnswerAction;
import net.geocentral.geometria.model.answer.GMultipleChoiceAnswer;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GSolutionMultipleChoiceAnswerDialog extends JDialog implements
        GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GSolutionAnswerAction action;

    private GMultipleChoiceAnswer problemAnswer;

    private JRadioButton[] optionButtons;
    
    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSolutionMultipleChoiceAnswerDialog(Frame ownerFrame, GSolutionAnswerAction action,
            GMultipleChoiceAnswer problemAnswer) {
        super(ownerFrame, true);
        logger.info("");
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
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
        inputPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel optionsPane = new JPanel();
        optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));
        Map<String, Boolean> options = problemAnswer.getOptions();
        ButtonGroup buttonGroup = new ButtonGroup();
        optionButtons = new JRadioButton[options.size()];
        int optionIndex = 0;
        for (Entry<String, Boolean> entry : options.entrySet()) {
            String option = entry.getKey();
            JRadioButton optionButton = new JRadioButton(option);
            buttonGroup.add(optionButton);
            optionsPane.add(optionButton);
            optionButtons[optionIndex++] = optionButton;
        }
        inputPane.add(optionsPane);
        inputPane.add(Box.createGlue());
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void ok() {
        logger.info("");
        String selectedOption = null;
        int optionIndex = 0;
        for (JRadioButton optionButton : optionButtons) {
            if (optionButton.isSelected()) {
                selectedOption = optionButtons[optionIndex].getText();
                break;
            }
            optionIndex++;
        }
        if (selectedOption == null) {
            GGraphicsFactory.getInstance().showErrorDialog( this, GDictionary.get("SelectOption"));
            return;
        }
        action.setInput(selectedOption, null);
        if (!problemAnswer.verify(optionIndex)) {
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
