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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GRenameVariableAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GRenameVariableDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GRenameVariableAction action;

    private JComboBox oldNameInput;

    private JTextField newNameInput;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRenameVariableDialog(Frame ownerFrame, GRenameVariableAction action, String[] variableNames) {
        super(ownerFrame, true);
        this.action = action;
        logger.info("");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents(variableNames);
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("RenameVariable"));
    }

    private void layoutComponents(String[] variableNames) {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = GGraphicsFactory.getInstance().createTitledBorderPane(GDictionary.get("Variables"));
        inputPane.add(Box.createHorizontalGlue());
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
        JPanel oldNamePane = new JPanel();
        oldNamePane.setLayout(new BoxLayout(oldNamePane, BoxLayout.X_AXIS));
        JLabel label1 = new JLabel(GDictionary.get("OldVariable"));
        oldNamePane.add(label1);
        oldNamePane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        oldNameInput = GGraphicsFactory.getInstance().createComboBox(variableNames);
        oldNamePane.add(oldNameInput);
        inputPane.add(oldNamePane);
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel newNamePane = new JPanel();
        newNamePane.setLayout(new BoxLayout(newNamePane, BoxLayout.X_AXIS));
        JLabel label2 = new JLabel(GDictionary.get("NewVariable"));
        newNamePane.add(label2);
        newNamePane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        newNameInput = GGraphicsFactory.getInstance().createVariableInput(null);
        newNamePane.add(newNameInput);
        inputPane.add(newNamePane);
        inputPane.add(Box.createHorizontalGlue());
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String oldName) {
        logger.info(oldName);
        if (oldName != null) {
            oldNameInput.setSelectedItem(oldName);
        }
    }

    public void ok() {
        logger.info("");
        action.setInput(oldNameInput.getSelectedItem().toString(), newNameInput.getText().trim());
        try {
            action.validateApply();
        }
        catch (Exception exception) {
            GGraphicsFactory.getInstance().showErrorDialog(this, exception.getMessage());
            return;
        }
        option = OK_OPTION;
        result = true;
        dispose();
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
