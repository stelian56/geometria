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
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GLayDistanceAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GValueInputPane;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GLayDistanceDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GLayDistanceAction action;

    private JTextField p0TextField;

    private JTextField p1TextField;

    private JTextField p2TextField;

    private GValueInputPane distanceInputPane;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLayDistanceDialog(Frame ownerFrame, GLayDistanceAction action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("LayDistance"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance()
            .createTitledBorderPane(GDictionary.get("ReferencePoints"));
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        p1TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel leftInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustBottom(p1TextField);
        topPane.add(leftInputPane);
        JPanel centerInputPane = GGraphicsFactory.getInstance()
            .createImagePane("/images/LayDistance.png");
        topPane.add(centerInputPane);
        p2TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        p0TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel rightInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustCenter(p0TextField, p2TextField, 25);
        topPane.add(rightInputPane);
        getContentPane().add(topPane);
        distanceInputPane = GGraphicsFactory.getInstance()
            .createVariableInputPane(null, GDictionary.get("Distance"));
        getContentPane().add(distanceInputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String[] pLabels, String distanceString) {
        logger.info(Arrays.asList(pLabels));
        p0TextField.setText(pLabels[0]);
        p1TextField.setText(pLabels[1]);
        p2TextField.setText(pLabels[2]);
        distanceInputPane.setInput(distanceString);
    }

    public void ok() {
        logger.info("");
        action.setInput(p0TextField.getText().trim(),
                p1TextField.getText().trim(), p2TextField.getText().trim(),
                distanceInputPane.getInput());
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
