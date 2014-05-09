/**
 * Copyright 2000-2013 Geometria Contributors
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.vecmath.Point3d;

import net.geocentral.geometria.action.GProblemAnswerAction;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GNotepadRecord;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.answer.GAnswer;
import net.geocentral.geometria.model.answer.GConditionPlaneAnswer;
import net.geocentral.geometria.model.answer.GFixedPlaneAnswer;
import net.geocentral.geometria.model.answer.GLineSetAnswer;
import net.geocentral.geometria.model.answer.GMultipleChoiceAnswer;
import net.geocentral.geometria.model.answer.GNumberAnswer;
import net.geocentral.geometria.model.answer.GPointSetAnswer;
import net.geocentral.geometria.model.answer.condition.GCondition;
import net.geocentral.geometria.model.answer.condition.GEquilateralTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GHamiltonianCycleCondition;
import net.geocentral.geometria.model.answer.condition.GIsoscellesTriangleCondition;
import net.geocentral.geometria.model.answer.condition.GLineSetCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughLineCondition;
import net.geocentral.geometria.model.answer.condition.GNotThroughPointCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToLineCondition;
import net.geocentral.geometria.model.answer.condition.GParallelToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GParallelogramCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToLineCondition;
import net.geocentral.geometria.model.answer.condition.GPerpendicularToPlaneCondition;
import net.geocentral.geometria.model.answer.condition.GPlaneCondition;
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
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;
import net.geocentral.geometria.util.GMath;
import net.geocentral.geometria.util.GPointSetUtils;

import org.apache.log4j.Logger;

public class GProblemAnswerDialog extends JDialog implements
        GHelpOkCancelDialog, ActionListener {

    private int option = CANCEL_OPTION;

    private GProblem document;

    private JPanel valuePane;

    private JRadioButton numberButton;

    private JRadioButton pointSetButton;

    private JRadioButton lineSetButton;
    
    private JRadioButton fixedPlaneButton;

    private JRadioButton conditionPlaneButton;
    
    private JRadioButton multipleChoiceButton;

    private JTextField numberTextField;

    private JTextField pointSetTextField;

    private JComboBox[] lineSetComboBoxes;
    
    private JTextField[] conditionPlaneTextFields;

    private JComboBox[] conditionPlaneComboBoxes;

    private List<JTextField> optionTextFields;
    
    private List<JRadioButton> optionRadioButtons;
    
    private GLineSetCondition[][] lineSetConditionGroups = new GLineSetCondition[][] {
            new GLineSetCondition[] {
                    new GHamiltonianCycleCondition()
            }
    };
    
    private GPlaneCondition[][] planeConditionGroups = new GPlaneCondition[][] {
            new GPlaneCondition[] {
                    new GThroughPointCondition(), new GThroughLineCondition(),
                    new GNotThroughPointCondition(),
                    new GNotThroughLineCondition(),
                    new GThroughNoVertexCondition(),
                    new GThroughNoEdgeCondition(),
                    new GParallelToLineCondition(),
                    new GParallelToPlaneCondition(),
                    new GPerpendicularToLineCondition(),
                    new GPerpendicularToPlaneCondition() },

                    new GPlaneCondition[] { new GVolumeCutInRatioCondition() },

                    new GPlaneCondition[] {
                    new GIsoscellesTriangleCondition(),
                    new GRectangularTriangleCondition(),
                    new GEquilateralTriangleCondition(),
                    new GParallelogramCondition(), new GRhombusCondition(),
                    new GRectangleCondition(), new GSquareCondition() }
    };

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
        lineSetButton = new JRadioButton(GDictionary.get("LineSet"));
        lineSetButton.addActionListener(this);
        fixedPlaneButton = new JRadioButton(GDictionary.get("FixedPlane"));
        fixedPlaneButton.addActionListener(this);
        conditionPlaneButton = new JRadioButton(GDictionary.get("ConditionPlane"));
        conditionPlaneButton.addActionListener(this);
        multipleChoiceButton = new JRadioButton(GDictionary.get("MultipleChoice"));
        multipleChoiceButton.addActionListener(this);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(numberButton);
        buttonGroup.add(pointSetButton);
        buttonGroup.add(lineSetButton);
        buttonGroup.add(fixedPlaneButton);
        buttonGroup.add(conditionPlaneButton);
        buttonGroup.add(multipleChoiceButton);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
        buttonPane.add(Box.createVerticalGlue());
        buttonPane.add(numberButton);
        buttonPane.add(pointSetButton);
        buttonPane.add(lineSetButton);
        buttonPane.add(fixedPlaneButton);
        buttonPane.add(conditionPlaneButton);
        buttonPane.add(multipleChoiceButton);
        buttonPane.add(Box.createVerticalGlue());
        typePane.add(Box.createRigidArea(new Dimension(20, 20)));
        typePane.add(buttonPane);
        typePane.add(Box.createHorizontalGlue());
        GAnswer answer = document.getAnswer();
        if (answer instanceof GNumberAnswer)
            numberButton.setSelected(true);
        else if (answer instanceof GPointSetAnswer)
            pointSetButton.setSelected(true);
        else if (answer instanceof GLineSetAnswer) {
            lineSetButton.setSelected(true);
        }
        else if (answer instanceof GFixedPlaneAnswer)
            fixedPlaneButton.setSelected(true);
        else if (answer instanceof GConditionPlaneAnswer)
            conditionPlaneButton.setSelected(true);
        else if (answer instanceof GMultipleChoiceAnswer) {
            multipleChoiceButton.setSelected(true);
        }
        layoutValuePane();
    }

    private void layoutValuePane() {
        logger.info("");
        valuePane.removeAll();
        if (numberButton.isSelected())
            layoutNumberValuePane();
        else if (pointSetButton.isSelected())
            layoutPointSetValuePane();
        else if (lineSetButton.isSelected()) {
            layoutLineSetValuePane();
        }
        else if (fixedPlaneButton.isSelected())
            layoutFixedPlaneValuePane();
        else if (conditionPlaneButton.isSelected())
            layoutConditionPlaneValuePane();
        else if (multipleChoiceButton.isSelected()) {
            layoutMultipleChoiceValuePane();
        }
    }

    private void layoutNumberValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        numberTextField =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        JPanel inputPane = layoutPane(GDictionary.get("Value"), new JComponent[] { numberTextField });
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
        JPanel inputPane = layoutPane(GDictionary.get("Points"), new JComponent[] { pointSetTextField });
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
            String[] selectedLabels = GPointSetUtils.fromSelection(document);
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

    private void layoutLineSetValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        lineSetComboBoxes = new JComboBox[lineSetConditionGroups.length];
        String[] captions = {
            GDictionary.get("Miscellaneous")
        };
        for (int i = 0; i < lineSetConditionGroups.length; i++) {
            String[] labels = new String[lineSetConditionGroups[i].length + 1];
            labels[0] = "-- " + GDictionary.get("NotSelected") + " --";
            for (int j = 0; j < lineSetConditionGroups[i].length; j++)
                labels[j + 1] = lineSetConditionGroups[i][j].getDescription();
            lineSetComboBoxes[i] = GGraphicsFactory.getInstance().createComboBox(labels);
            JComponent[] components;
            components = new JComponent[] { lineSetComboBoxes[i] };
            JPanel pane = layoutPane(captions[i], components);
            valuePane.add(pane);
            valuePane.add(Box.createVerticalGlue());
        }
        GAnswer ans = document.getAnswer();
        if (ans instanceof GLineSetAnswer) {
            GCondition[] conditions = ((GLineSetAnswer)ans).getConditions();
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i] == null)
                    continue;
                for (int j = 0; j < lineSetConditionGroups[i].length; j++) {
                    if (lineSetConditionGroups[i][j].getClass().equals(conditions[i].getClass())) {
                        lineSetComboBoxes[i].setSelectedIndex(j + 1);
                    }
                }
            }
        }
    }

    private void layoutFixedPlaneValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        pointSetTextField =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        JPanel inputPane = layoutPane(GDictionary.get("Points"), new JComponent[] { pointSetTextField });
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
            String[] selectedLabels = GPointSetUtils.fromSelection(document);
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

    private void layoutConditionPlaneValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        valuePane.add(Box.createVerticalGlue());
        conditionPlaneComboBoxes = new JComboBox[planeConditionGroups.length];
        conditionPlaneTextFields = new JTextField[planeConditionGroups.length];
        conditionPlaneTextFields[0] =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        conditionPlaneTextFields[1] =
            GGraphicsFactory.getInstance().createAnswerInput(null);
        String[] captions = {
                GDictionary.get("Passes"),
                GDictionary.get("Miscellaneous"),
                GDictionary.get("SectionShapedLike") };
        for (int i = 0; i < planeConditionGroups.length; i++) {
            String[] labels = new String[planeConditionGroups[i].length + 1];
            labels[0] = "-- " + GDictionary.get("NotSelected") + " --";
            for (int j = 0; j < planeConditionGroups[i].length; j++)
                labels[j + 1] = planeConditionGroups[i][j].getDescription();
            conditionPlaneComboBoxes[i] =
                GGraphicsFactory.getInstance().createComboBox(labels);
            JComponent[] components;
            if (conditionPlaneTextFields[i] == null)
                components = new JComponent[] { conditionPlaneComboBoxes[i] };
            else
                components = new JComponent[]
                    { conditionPlaneComboBoxes[i], conditionPlaneTextFields[i] };
            JPanel pane = layoutPane(captions[i], components);
            valuePane.add(pane);
            valuePane.add(Box.createVerticalGlue());
        }
        GAnswer ans = document.getAnswer();
        if (ans instanceof GConditionPlaneAnswer) {
            GCondition[] conditions = ((GConditionPlaneAnswer)ans).getConditions();
            for (int i = 0; i < conditions.length; i++) {
                if (conditions[i] == null)
                    continue;
                for (int j = 0; j < planeConditionGroups[i].length; j++) {
                    if (planeConditionGroups[i][j].getClass().equals(
                            conditions[i].getClass())) {
                        conditionPlaneComboBoxes[i].setSelectedIndex(j + 1);
                        if (conditionPlaneTextFields[i] != null) {
                            String stringValue = conditions[i].getStringValue();
                            conditionPlaneTextFields[i].setText(stringValue);
                        }
                    }
                }
            }
        }
        else {
            String[] selectedLabels = GPointSetUtils.fromSelection(document);
            if (selectedLabels.length > 0) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < selectedLabels.length; i++) {
                    buf.append(selectedLabels[i]);
                    if (i < selectedLabels.length - 1)
                        buf.append(",");
                }
                conditionPlaneTextFields[0].setText(String.valueOf(buf));
            }
        }
    }

    private void layoutMultipleChoiceValuePane() {
        logger.info("");
        valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.Y_AXIS));
        final JPanel optionsPane = new JPanel(); 
        optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));
        final JPanel selectionPane = new JPanel();
        selectionPane.setLayout(new BoxLayout(selectionPane, BoxLayout.Y_AXIS));
        selectionPane.add(new JScrollPane(optionsPane));
        JPanel addButtonPane = new JPanel();
        addButtonPane.setLayout(new BoxLayout(addButtonPane, BoxLayout.X_AXIS));
        addButtonPane.add(Box.createHorizontalGlue());
        JButton addButton = GGraphicsFactory.getInstance().createButton(GDictionary.get("Add"));
        addButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        addButtonPane.add(addButton);
        selectionPane.add(addButtonPane);
        JPanel inputPane = layoutPane(GDictionary.get("Options"), new JComponent[] { selectionPane });
        valuePane.add(inputPane);
        optionTextFields = new ArrayList<JTextField>();
        optionRadioButtons = new ArrayList<JRadioButton>();
        final ButtonGroup radioButtonGroup = new ButtonGroup();
        final List<JButton> removeButtons = new ArrayList<JButton>();
        GAnswer ans = document.getAnswer();
        Map<String, Boolean> options;
        if (ans instanceof GMultipleChoiceAnswer) {
            options = ((GMultipleChoiceAnswer)ans).getOptions();
        }
        else {
            options = GMultipleChoiceAnswer.getDefaultOptions();
        }
        for (Entry<String, Boolean> entry : options.entrySet()) {
            String option = entry.getKey();
            boolean selected = entry.getValue();
            JPanel optionPane = layoutOptionPane(selected, option, radioButtonGroup, removeButtons, optionsPane,
                    selectionPane);
            optionsPane.add(optionPane);
        }
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JPanel optionPane = layoutOptionPane(false, "", radioButtonGroup, removeButtons, optionsPane,
                        selectionPane);
                optionsPane.add(optionPane);
                selectionPane.validate();
                selectionPane.repaint();
            }
        });
    }

    private JPanel layoutOptionPane(boolean selected, String option, final ButtonGroup radioButtonGroup,
            final List<JButton> removeButtons, final JPanel optionsPane, final JPanel selectionPane) {
        JPanel optionPane = new JPanel();
        optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.X_AXIS));
        optionPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 10));
        JRadioButton radioButton = new JRadioButton();
        radioButtonGroup.add(numberButton);
        radioButton.setSelected(selected);
        optionRadioButtons.add(radioButton);
        radioButtonGroup.add(radioButton);
        optionPane.add(radioButton);
        JTextField optionTextField = GGraphicsFactory.getInstance().createInput(option);
        optionTextField.setText(option);
        optionTextFields.add(optionTextField);
        optionPane.add(optionTextField);
        optionPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        final JButton removeButton = GGraphicsFactory.getInstance().createButton(GDictionary.get("Remove"));
        removeButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
               int optionIndex = removeButtons.indexOf(removeButton);
               JRadioButton removedRadioButton = optionRadioButtons.remove(optionIndex);
               radioButtonGroup.remove(removedRadioButton);
               optionTextFields.remove(optionIndex);
               removeButtons.remove(optionIndex);
               optionsPane.remove(optionIndex);
               updateOptionRemoveButtons(removeButtons);
               selectionPane.validate();
               selectionPane.repaint();
           }
        });
        removeButtons.add(removeButton);
        updateOptionRemoveButtons(removeButtons);
        optionPane.add(removeButton);
        return optionPane;
    }

    private void updateOptionRemoveButtons(List<JButton> removeButtons) {
        int optionCount = removeButtons.size();
        for (JButton rButton : removeButtons) {
            rButton.setEnabled(optionCount > 1);
        }
    }
    
    private JPanel layoutPane(String caption, JComponent[] components) {
        Dimension padding = new Dimension(10, 10);
        JPanel pane = GGraphicsFactory.getInstance().createTitledBorderPane(caption);
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
                coords = GPointSetUtils.fromString(pointSetTextField.getText().trim(), figure);
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
        else if (lineSetButton.isSelected()) {
            GLineSetCondition[] conditions = new GLineSetCondition[lineSetConditionGroups.length];
            for (int i = 0; i < lineSetConditionGroups.length; i++) {
                int j = lineSetComboBoxes[i].getSelectedIndex();
                if (j > 0) {
                    conditions[i] = lineSetConditionGroups[i][j - 1];
                    try {
                        conditions[i].validate(null, document);
                    }
                    catch (Exception exception) {
                        GGraphicsFactory.getInstance().showErrorDialog(this, exception.getMessage());
                        return;
                    }
                }
            }
            ans = new GLineSetAnswer(conditions);
        }
        else if (fixedPlaneButton.isSelected()) {
            GFigure figure = document.getSelectedFigure();
            Point3d[] coords;
            try {
                coords = GPointSetUtils.fromString(pointSetTextField.getText().trim(), figure);
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
        else if (conditionPlaneButton.isSelected()) {
            GPlaneCondition[] conditions = new GPlaneCondition[planeConditionGroups.length];
            for (int i = 0; i < planeConditionGroups.length; i++) {
                int j = conditionPlaneComboBoxes[i].getSelectedIndex();
                if (j > 0) {
                    conditions[i] = planeConditionGroups[i][j - 1];
                    try {
                        String input = conditionPlaneTextFields[i] == null ? null
                                : conditionPlaneTextFields[i].getText().trim();
                        conditions[i].validate(input, document);
                    }
                    catch (Exception exception) {
                        GGraphicsFactory.getInstance().showErrorDialog(this,
                                exception.getMessage());
                        return;
                    }
                }
            }
            ans = new GConditionPlaneAnswer(conditions);
        }
        else if (multipleChoiceButton.isSelected()) {
            LinkedHashMap<String, Boolean> options = new LinkedHashMap<String, Boolean>();
            int optionCount = optionTextFields.size();
            for (int optionIndex = 0; optionIndex < optionCount; optionIndex++) {
                String option = optionTextFields.get(optionIndex).getText().trim();
                if (option.isEmpty()) {
                    String message = GDictionary.get("EmptyOptions");
                    GGraphicsFactory.getInstance().showErrorDialog(this, message);
                    return;
                }
                if (options.containsKey(option)) {
                    String message = GDictionary.get("DuplicateOption");
                    GGraphicsFactory.getInstance().showErrorDialog(this, message);
                    return;
                }
                boolean selected = optionRadioButtons.get(optionIndex).isSelected();
                options.put(option, selected);
            }
            if (!options.containsValue(true)) {
                String message = GDictionary.get("SelectCorrectOption");
                GGraphicsFactory.getInstance().showErrorDialog(message);
                return;
            }
            ans = new GMultipleChoiceAnswer(options);
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
