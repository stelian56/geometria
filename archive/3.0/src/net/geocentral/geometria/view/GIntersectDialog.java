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

import net.geocentral.geometria.action.GIntersectAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GIntersectDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GIntersectAction action;

    private JTextField p11TextField;

    private JTextField p12TextField;

    private JTextField p21TextField;

    private JTextField p22TextField;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GIntersectDialog(Frame ownerFrame, GIntersectAction action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("IntersectLines"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(
                GDictionary.get("ReferencePoints"));
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        p11TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        p21TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel leftInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustCenter( p11TextField, p21TextField, 45);
        topPane.add(leftInputPane);
        JPanel centerInputPane = GGraphicsFactory.getInstance()
            .createImagePane("/images/Intersect.png");
        topPane.add(centerInputPane);
        p22TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        p12TextField = GGraphicsFactory.getInstance().createLabelInput(null);
        JPanel rightInputPane = GGraphicsFactory.getInstance()
            .createContainerAdjustCenter( p22TextField, p12TextField, 45);
        topPane.add(rightInputPane);
        getContentPane().add(topPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String[] pLabels) {
        logger.info(Arrays.asList(pLabels));
        p11TextField.setText(pLabels[0]);
        p12TextField.setText(pLabels[1]);
        p21TextField.setText(pLabels[2]);
        p22TextField.setText(pLabels[3]);
    }

    public void ok() {
        logger.info("");
        action.setInput(p11TextField.getText().trim(),
                p12TextField.getText().trim(), p21TextField.getText().trim(),
                p22TextField.getText().trim());
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
