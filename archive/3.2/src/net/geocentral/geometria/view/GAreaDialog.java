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
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GAreaAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GValueInputPane;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GAreaDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GAreaAction action;

    private JList faceList;

    private GValueInputPane variableInputPane;

    private String[] fLabelStrings;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GAreaDialog(Frame ownerFrame, GAreaAction action,
            String[] faceLabelStrings) {
        super(ownerFrame, true);
        logger.info(Arrays.asList(faceLabelStrings));
        this.action = action;
        this.fLabelStrings = faceLabelStrings;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("MeasureArea"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(
                GDictionary.get("ChooseFace"));
        faceList = new JList(fLabelStrings);
        faceList.setSelectedIndex(0);
        JScrollPane sp = new JScrollPane(faceList);
        sp.setPreferredSize(new Dimension(80, 80));
        topPane.add(sp);
        getContentPane().add(topPane);
        variableInputPane = GGraphicsFactory.getInstance()
            .createVariableInputPane(null, GDictionary.get("AssignVariable"));
        getContentPane().add(variableInputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance()
            .createHelpOkCancelPane(this,action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String fLabelsString) {
        logger.info(fLabelsString);
        faceList.setSelectedValue(fLabelsString, true);
    }

    public void ok() {
        logger.info("");
        String faceLabelsString = String.valueOf(faceList.getSelectedValue());
        action.setInput(faceLabelsString, variableInputPane.getInput());
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
