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
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GJoinAction;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GNoMoreJoinMatchesException;
import net.geocentral.geometria.util.GUndefinedJoinException;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GJoinDialog extends JDialog implements GHelpOkCancelDialog {

    private int option = CANCEL_OPTION;

    private GJoinAction action;

    private String[] figureNames;

    private JList figure1List;

    private JList figure2List;

    private boolean result = false;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GJoinDialog(Frame ownerFrame, GJoinAction action, String[] figureNames) {
        super(ownerFrame, true);
        logger.info(Arrays.asList(figureNames));
        this.action = action;
        this.figureNames = figureNames;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.BOTTOM_LEFT);
        setTitle(GDictionary.get("JoinFigures"));
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel topPane = GGraphicsFactory.getInstance().createTitledBorderPane(
                GDictionary.get("FiguresToJoin"));
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        Dimension size = new Dimension(80, 80);
        int padding = 10;
        figure1List = new JList(figureNames);
        figure1List.setSelectedIndex(0);
        JScrollPane sp1 = new JScrollPane(figure1List);
        sp1.setPreferredSize(size);
        sp1.setMaximumSize(size);
        topPane.add(sp1);
        topPane.add(Box.createRigidArea(new Dimension(padding, 10)));
        JPanel pane = GGraphicsFactory.getInstance()
            .createImagePane("/images/Join.png");
        topPane.add(pane);
        topPane.add(Box.createRigidArea(new Dimension(padding, 10)));
        figure2List = new JList(figureNames);
        figure1List.setSelectedIndex(0);
        JScrollPane sp2 = new JScrollPane(figure2List);
        sp2.setPreferredSize(size);
        sp2.setMaximumSize(size);
        topPane.add(sp2);
        getContentPane().add(topPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void prefill(String figureName) {
        logger.info(figureName);
        figure1List.setSelectedValue(figureName, true);
        figure2List.setSelectedValue(figureName, true);
    }

    public void ok() {
        logger.info("");
        Object figure1Name = figure1List.getSelectedValue();
        Object figure2Name = figure2List.getSelectedValue();
        if (figure1Name == null || figure2Name == null)
            return;
        action.setFigureNames(String.valueOf(figure1Name),
                String.valueOf(figure2Name));
        try {
            action.validateApply(this);
        }
        catch (Exception exception) {
            if (exception instanceof GUndefinedJoinException
                    || exception instanceof GNoMoreJoinMatchesException) {
                result = false;
                dispose();
                return;
            }
            else {
                GGraphicsFactory.getInstance().showErrorDialog(
                        this, exception.getMessage());
                return;
            }
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
