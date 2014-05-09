/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.vecmath.Point3d;

import net.geocentral.geometria.action.GProblemAnswerAction;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.model.answer.GConditionAnswer;
import net.geocentral.geometria.model.answer.GFixedPlaneAnswer;
import net.geocentral.geometria.model.answer.GNumberAnswer;
import net.geocentral.geometria.model.answer.GPointSetAnswer;
import net.geocentral.geometria.model.answer.condition.GCondition;
import net.geocentral.geometria.model.answer.condition.GEquilateralTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GIsoscellesTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughLineCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughPointCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToLineCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GParallelogramCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToLineCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GRectangleCondition;
import net.geocentral.geometria.model.answer.condition.GRectangularTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GRhombusCondition;
import net.geocentral.geometria.model.answer.condition.GSquareCondition;
import net.geocentral.geometria.model.answer.condition.GThroughLineCondition;
import net.geocentral.geometria.model.answer.condition.GThroughNoEdgeCondition;
import net.geocentral.geometria.model.answer.condition.GThroughNoVertexCondition;
import net.geocentral.geometria.model.answer.condition.GThroughPointCondition;
import net.geocentral.geometria.model.answer.condition.GVolumeCutInRatioCondition;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GProblemAnswerDialog extends JDialog implements
        GHelpOkCancelDialog, ActionListener {

    private int option = CANCEL_OPTION;

    private GProblem document;

    private JPanel valuePane;

    private JRadioButton numberButton;

    private JRadioButton pointSetButton;

    private JRadioButton fixedPlaneButton;

    private JRadioButton conditionButton;

    private JTextField numberTextField;

    private JTextField pointSetTextField;

    private JTextField[] conditionTextFields;

    private JComboBox[] conditionComboBoxes;

    private GCondition[][] conditionGroups = new GCondition[][] {
            new GCondition[] {
                    new GThroughPointCondition(), new GThroughLineCondition(),
                    new GNotThroughPointCondition(),
                    new GNotThroughLineCondition(),
                    new GThroughNoVertexCondition(),
                    new GThroughNoEdgeCondition(),
                    new GParallelToLineCondition(),
                    new GParallelToPlaneCondition(),
                    new GPerpendicularToLineCondition(),
                    new GPerpendicularToPlaneCondition() },

                    new GCondition[] { new GVolumeCutInRatioCondition() },

                    new GCondition[] {
                    new GIsoscellesTriangleCondition(),
                    new GRectangularTriangleCondition(),
                    new GEquilateralTriangleCondition(),
                    new GParallelogramCondition(), new GRhombusCondition(),
                    new GRectangleCondition(), new GSquareCondition() } };

    private GAnswer answer;

    private GProblemAnswerAction action;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GProblemAnswerDialog(Frame ownerFrame, GProblemAnswerAction action, GProblem document) {
        super(ownerFrame, true);
        logger.info("");
        this.document = document;
        this.action = action;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("Answer"));
        setResizable(true);
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(400, 320));
        getContentPane().add(tabbedPane);
        JPanel typePane = new JPanel();
        valuePane = new JPanel();
        tabbedPane.addTab(GDictionary.get("Type"), typePane);
        tabbedPane.addTab(GDictionary.get("Value"), valuePane);
        layoutTypePane(typePane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    private void layoutTypePane(JPanel typePane) {
        logger.info("");
        typePane.setLayout(new BoxLayout(typePane, BoxLayout.X_AXIS));
        numberButton = new JRadioButton(GDictionary.get("Number"));
        numberButton.addActionListener(this);
        pointSetButton = new JRadioButton(GDictionary.get("PointSet"));
        pointSetButton.addActionListener(this);
        fixedPlaneButton = new JRadioButton(GDictionary.get("FixedPlane"));
        fixedPlaneButton.addActionListener(this);
        conditionButton = new JRadioButton(GDictionary.get("ConditionPlane"));
        conditionButton.addActionListener(this);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(numberButton);
        buttonGroup.add(pointSetButton);
        buttonGroup.add(fixedPlaneButton);
        buttonGroup.add(conditionButton);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
        buttonPane.add(Box.createVerticalGlue());
        buttonPane.add(numberButton);
        buttonPane.add(pointSetButton);
        buttonPane.add(fixedPlaneButton);
        buttonPane.add(conditionButton);
        buttonPane.add(Box.createVerticalGlue());
        typePane.add(Box.createRigidArea(new Dimension(20, 20)));
        typePane.add(buttonPane);
        typePane.add(Box.createHorizontalGlue());
        GAnswer answer = document.getAnswer();
        if (answer instanceof GNumberAnswer)
            numberButton.setSelected(true);
        else if (answer instanceof GPointSetAnswer)
            pointSetButton.setSelected(true);
        else if (answer instanceof GFixedPlaneAnswer)
            fixedPlaneButton.setSelected(true);
        else if (answer instanceof GConditionAnswer)
            conditionButton.setSelected(true);
        layoutValuePane();
    }

    private void layoutValuePane() {
        logger.info("");
        valuePane.removeAll();
        if (numberButton.isSelected())
            layoutNumberValuePane();
        else if (pointSetButton.isSelected())
            layoutPointSetValuePane();
        else if (fixedPlaneButton.isSelected())
            layoutFixedPlaneValuePane();
        else if (conditionButton.isSelected())
            layoutConditionValuePane();
    }

    private void layoutNumberValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        numberTextField =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        JPanel inputPane = layoutPane(GDictionary.get("Value"),
                new JComponent[] { numberTextField });
        valuePane.add(inputPane);
        valuePane.add(Box.createVerticalGlue());
        GAnswer ans = document.getAnswer();
        if (ans instanceof GNumberAnswer
                && ((GNumberAnswer)ans).getValue() != null
                && ((GNumberAnswer)ans).getValue() != 0) {
            String stringValue = String.valueOf(((GNumberAnswer) ans).getValue());
            numberTextField.setText(stringValue);
        }
        else {
            GNotepadRecord record = document.getNotepad().getSelectedRecord();
            if (record != null) {
                String variableName = record.getVariable().getName();
                numberTextField.setText(variableName);
            }
        }
    }

    private void layoutPointSetValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        pointSetTextField =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        JPanel inputPane = layoutPane(GDictionary.get("Points"),
                new JComponent[] { pointSetTextField });
        valuePane.add(inputPane);
        valuePane.add(Box.createVerticalGlue());
        GAnswer ans = document.getAnswer();
        if (ans instanceof GPointSetAnswer) {
            Point3d[] coords = ((GPointSetAnswer)ans).getCoords();
            String valueString;
            if (coords == null)
                valueString = "";
            else
                valueString = ans.toString();
            pointSetTextField.setText(valueString);
        }
        else {
            String[] selectedLabels =
                GPointSetFactory.getInstance().fromSelection(document);
            if (selectedLabels.length > 0) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < selectedLabels.length; i++) {
                    buf.append(selectedLabels[i]);
                    if (i < selectedLabels.length - 1)
                        buf.append(",");
                }
                pointSetTextField.setText(String.valueOf(buf));
            }
        }
    }

    private void layoutFixedPlaneValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        pointSetTextField =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        JPanel inputPane = layoutPane(GDictionary.get("ReferencePoints"),
                new JComponent[] { pointSetTextField });
        valuePane.add(inputPane);
        valuePane.add(Box.createVerticalGlue());
        GAnswer ans = document.getAnswer();
        if (ans instanceof GFixedPlaneAnswer) {
            Point3d[] coords = ((GFixedPlaneAnswer)ans).getCoords();
            String valueString;
            if (coords == null)
                valueString = "";
            else
                valueString = ans.toString();
            pointSetTextField.setText(valueString);
        }
        else {
            String[] selectedLabels =
                GPointSetFactory.getInstance().fromSelection(document);
            if (selectedLabels.length > 0) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < selectedLabels.length; i++) {
                    buf.append(selectedLabels[i]);
                    if (i < selectedLabels.length - 1)
                        buf.append(",");
                }
                pointSetTextField.setText(String.valueOf(buf));
            }
        }
    }

    private void layoutConditionValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        conditionComboBoxes = new JComboBox[conditionGroups.length];
        conditionTextFields = new JTextField[conditionGroups.length];
        conditionTextFields[0] =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        conditionTextFields[1] =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        String[] captions = {
                GDictionary.get("Passes"),
                GDictionary.get("Miscellaneous"),
                GDictionary.get("SectionShapedLike") };
        for (int i = 0; i < conditionGroups.length; i++) {
            String[] labels = new String[conditionGroups[i].length + 1];
            labels[0] = "-- " + GDictionary.get("NotSelected") + " --";
            for (int j = 0; j < conditionGroups[i].length; j++)
                labels[j + 1] = conditionGroups[i][j].getDescription();
            conditionComboBoxes[i] =
                GGraphicsFactory.getInstance().createComboBox(labels);
            JComponent[] components;
            if (conditionTextFields[i] == null)
                components = new JComponent[] { conditionComboBoxes[i] };
            else
                components = new JComponent[]
                    { conditionComboBoxes[i], conditionTextFields[i] };
            JPanel pane = layoutPane(captions[i], components);
            valuePane.add(pane);
            valuePane.add(Box.createVerticalGlue());
        }
        GAnswer ans = document.getAnswer();
        if (ans instanceof GConditionAnswer) {
            GCondition[] conditions = ((GConditionAnswer)ans).getConditions();
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i] == null)
                    continue;
                for (int j = 0; j < conditionGroups[i].length; j++) {
                    if (conditionGroups[i][j].getClass().equals(
                            conditions[i].getClass())) {
                        conditionComboBoxes[i].setSelectedIndex(j + 1);
                        if (conditionTextFields[i] != null) {
                            String stringValue = conditions[i].getStringValue();
                            conditionTextFields[i].setText(stringValue);
                        }
                    }
                }
            }
        }
        else {
            String[] selectedLabels =
                GPointSetFactory.getInstance().fromSelection(document);
            if (selectedLabels.length > 0) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < selectedLabels.length; i++) {
                    buf.append(selectedLabels[i]);
                    if (i < selectedLabels.length - 1)
                        buf.append(",");
                }
                conditionTextFields[0].setText(String.valueOf(buf));
            }
        }
    }

    private JPanel layoutPane(String caption, JComponent[] components) {
        Dimension padding = new Dimension(10, 10);
        JPanel pane =
            GGraphicsFactory.getInstance().createTitledBorderPane(caption);
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        for (int i = 0; i < components.length; i++) {
            pane.add(components[i]);
            if (i < components.length - 1)
                pane.add(Box.createRigidArea(padding));
        }
        return pane;
    }

    public void ok() {
        logger.info("");
        GAnswer ans = null;
        if (numberButton.isSelected()) {
            ans = new GNumberAnswer();
            try {
                ((GNumberAnswer)ans).validateInput(
                        numberTextField.getText().trim(), document);
            }
            catch (Exception exception) {
                GGraphicsFactory.getInstance().showErrorDialog(
                        this, exception.getMessage());
                return;
            }
        }
        else if (pointSetButton.isSelected()) {
            Point3d[] coords;
            try {
                GFigure figure = document.getSelectedFigure();
                coords = GPointSetFactory.getInstance().fromString(
                        pointSetTextField.getText().trim(), figure);
            }
            catch (Exception exception) {
                GGraphicsFactory.getInstance().showErrorDialog(
                        this, exception.getMessage());
                return;
            }
            if (coords.length < 1) {
                logger.info("No points");
                GGraphicsFactory.getInstance().showErrorDialog(this,
                        GDictionary.get("EnterPoints"));
                return;
            }
            ans = new GPointSetAnswer(coords);
        }
        else if (fixedPlaneButton.isSelected()) {
            GFigure figure = document.getSelectedFigure();
            Point3d[] coords;
            try {
                coords = GPointSetFactory.getInstance().fromString(
                        pointSetTextField.getText().trim(), figure);
            }
            catch (Exception exception) {
                GGraphicsFactory.getInstance().showErrorDialog(
                        this, exception.getMessage());
                return;
            }
            if (coords.length != 3) {
                GGraphicsFactory.getInstance().showErrorDialog(this,
                        GDictionary.get("RefPlaneBy3Points"));
                return;
            }
            if (GMath.areCollinear(coords, GMath.EPSILON)) {
                GGraphicsFactory.getInstance().showErrorDialog(this,
                        GDictionary.get("RefPointsAreCollinear"));
                return;
            }
            ans = new GFixedPlaneAnswer(coords);
        }
        else if (conditionButton.isSelected()) {
            GCondition[] conditions = new GCondition[conditionGroups.length];
            for (int i = 0; i < conditionGroups.length; i++) {
                int j = conditionComboBoxes[i].getSelectedIndex();
                if (j > 0) {
                    conditions[i] = conditionGroups[i][j - 1];
                    try {
                        String input = conditionTextFields[i] == null ? null
                                : conditionTextFields[i].getText().trim();
                        conditions[i].validate(input, document);
                    }
                    catch (Exception exception) {
                        GGraphicsFactory.getInstance().showErrorDialog(this,
                                exception.getMessage());
                        return;
                    }
                }
            }
            ans = new GConditionAnswer(conditions);
        }
        answer = ans;
        option = OK_OPTION;
        dispose();
    }

    public void cancel() {
        logger.info("");
        dispose();
    }

    public int getOption() {
        return option;
    }

    public GAnswer getAnswer() {
        return answer;
    }

    public void actionPerformed(ActionEvent e) {
        layoutValuePane();
    }

    private static final long serialVersionUID = 1L;
}
