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

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GScaleAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GValueInputPane;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GScaleDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GScaleAction action;

    private JTextField p1TextField;

    private JTextField p2TextField;

    private GValueInputPane factorInputPane;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GScaleDialog(Frame ownerFrame, GScaleAction action) {
        super(ownerFrame, true);
        this.action = action;
        logger.info("");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("ScaleFigure"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(
                GDictionary.get("ReferencePoints"));
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        p1TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel leftInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustBottom(p1TextField);
        topPane.add(leftInputPane);
        JPanel centerInputPane = GGraphicsFactory.getInstance()
            .createImagePane("/images/Scale.png");
        topPane.add(centerInputPane);
        p2TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel rightInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustBottom(p2TextField);
        topPane.add(rightInputPane);
        getContentPane().add(topPane);
        factorInputPane = GGraphicsFactory.getInstance()
            .createVariableInputPane(null, GDictionary.get("ScalingFactor"));
        getContentPane().add(factorInputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String pLabel1, String pLabel2) {
        logger.info(pLabel1 + ", " + pLabel2);
        p1TextField.setText(pLabel1);
        p2TextField.setText(pLabel2);
    }

    public void ok() {
        logger.info("");
        action.setInput(p1TextField.getText().trim(),
                p2TextField.getText().trim(), factorInputPane.getInput());
        try {
            action.validateApply();
        }
        catch (Exception exception) {
            GGraphicsFactory.getInstance().showErrorDialog(
                    this, exception.getMessage());
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
