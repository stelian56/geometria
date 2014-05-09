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
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GVersionManager;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GSelectStringDialog extends JDialog implements GOkCancelDialog {

    private int option = CANCEL_OPTION;

    private JList choices;

    private String input;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GSelectStringDialog(JDialog owner, String[] fLabelStrings, String subtitle) {
        super(owner, true);
        logger.info(Arrays.asList(fLabelStrings));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents(fLabelStrings, subtitle);
        pack();
        GGraphicsFactory.getInstance().setLocation(this, owner, LocationType.CENTER);
        setTitle(GVersionManager.getInstance().getApplicationName());
    }

    private void layoutComponents(String[] fLabelStrings, String subtitle) {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(subtitle);
        choices = new JList(fLabelStrings);
        choices.setSelectedIndex(0);
        JScrollPane sp = new JScrollPane(choices);
        sp.setPreferredSize(new Dimension(80, 50));
        topPane.add(sp);
        getContentPane().add(topPane);
        JPanel okCancelPane = GGraphicsFactory.getInstance().createOkCancelPane(this);
        getContentPane().add(okCancelPane);
    }

    public void cancel() {
        logger.info("");
        dispose();
    }

    public int getOption() {
        return option;
    }

    public void ok() {
        logger.info("");
        input = (String)choices.getSelectedValue();
        option = OK_OPTION;
        dispose();
    }

    public String getInput() {
        return input;
    }

    private static final long serialVersionUID = 1L;
}
