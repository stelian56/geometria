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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GRenamePointAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GRenamePointDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GRenamePointAction action;

    private JTextField oldLabelInput;

    private JTextField newLabelInput;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GRenamePointDialog(Frame ownerFrame, GRenamePointAction action) {
        super(ownerFrame, true);
        this.action = action;
        logger.info("");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("RenamePoint"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = GGraphicsFactory.getInstance().createTitledBorderPane(GDictionary.get("Labels"));
        inputPane.add(Box.createHorizontalGlue());
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
        JPanel oldLabelPane = new JPanel();
        oldLabelPane.setLayout(new BoxLayout(oldLabelPane, BoxLayout.X_AXIS));
        JLabel label1 = new JLabel(GDictionary.get("OldLabel"));
        oldLabelPane.add(label1);
        oldLabelPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        oldLabelInput = GGraphicsFactory.getInstance().createLabelInput(null);
        oldLabelPane.add(oldLabelInput);
        inputPane.add(oldLabelPane);
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel newLabelPane = new JPanel();
        newLabelPane.setLayout(new BoxLayout(newLabelPane, BoxLayout.X_AXIS));
        JLabel label2 = new JLabel(GDictionary.get("NewLabel"));
        newLabelPane.add(label2);
        newLabelPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        newLabelInput = GGraphicsFactory.getInstance().createLabelInput(null);
        newLabelPane.add(newLabelInput);
        inputPane.add(newLabelPane);
        inputPane.add(Box.createHorizontalGlue());
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String oldLabel) {
        logger.info(oldLabel);
        oldLabelInput.setText(oldLabel);
    }

    public void ok() {
        logger.info("");
        action.setInput(oldLabelInput.getText().trim(), newLabelInput.getText().trim());
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
