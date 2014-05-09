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

import net.geocentral.geometria.action.GPerpendicularAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GUndefinedItemException;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GPerpendicularDialog extends JDialog implements
        GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GPerpendicularAction action;

    private JTextField p0TextField;

    private JTextField p1TextField;

    private JTextField p2TextField;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GPerpendicularDialog(Frame ownerFrame, GPerpendicularAction action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("DrawPerpendicular"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(
                GDictionary.get("ReferencePoints"));
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        p0TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        p1TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        p2TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel leftInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustBottom(p1TextField);
        topPane.add(leftInputPane);
        JPanel centerInputPane = new JPanel();
        centerInputPane.setLayout(new BoxLayout(centerInputPane,
                BoxLayout.Y_AXIS));
        centerInputPane.add(p0TextField);
        centerInputPane.add(GGraphicsFactory.getInstance()
            .createImagePane("/images/Perpendicular.png"));
        topPane.add(centerInputPane);
        JPanel rightInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustBottom(p2TextField);
        topPane.add(rightInputPane);
        getContentPane().add(topPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String p0Label, String p1Label, String p2Label) {
        logger.info(p0Label + ", " + p1Label + ", " + p2Label);
        p0TextField.setText(p0Label);
        p1TextField.setText(p1Label);
        p2TextField.setText(p2Label);
    }

    public void ok() {
        logger.info("");
        action.setInput(p0TextField.getText().trim(),
            p1TextField.getText().trim(), p2TextField.getText().trim(), null);
        try {
            action.validateApply();
        }
        catch (GUndefinedItemException exception) {
            String[] faceLabelStrings =
                ((GUndefinedItemException) exception).itemNames;
            GSelectStringDialog dialog = new GSelectStringDialog(this,
                    faceLabelStrings, GDictionary.get("ChooseFace"));
            dialog.setVisible(true);
            if (dialog.getOption() == OK_OPTION) {
                String faceLabelsString = dialog.getInput();
                action.setInput(p0TextField.getText().trim(),
                    p1TextField.getText().trim(), p2TextField.getText().trim(),
                    faceLabelsString);
                try {
                    action.validateApply();
                }
                catch (Exception e) {
                    GGraphicsFactory.getInstance().showErrorDialog(
                            this, e.getMessage());
                    dispose();
                    return;
                }
            }
            else
                return;
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
