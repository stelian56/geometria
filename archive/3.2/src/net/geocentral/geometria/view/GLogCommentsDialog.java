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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GLoggable;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GLogCommentsDialog extends JDialog implements GOkCancelDialog {

    private GLoggable action;

    private int option = CANCEL_OPTION;
    
    private JTextArea commentsTextArea;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GLogCommentsDialog(Frame ownerFrame, GLoggable action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("Comments"));
        setResizable(true);
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
        inputPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel commentsPane = layoutCommentsPane();
        inputPane.add(commentsPane);
        getContentPane().add(inputPane);
        JPanel okCancelPane = GGraphicsFactory.getInstance().createOkCancelPane(this);
        getContentPane().add(okCancelPane);
    }

    private JPanel layoutCommentsPane() {
        logger.info("");
        JPanel commentsPane = new JPanel();
        commentsPane.setLayout(new BoxLayout(commentsPane, BoxLayout.Y_AXIS));
        commentsTextArea = new JTextArea(action.getComments());
        JScrollPane sp = GGraphicsFactory.getInstance().createCommentsArea(commentsTextArea);
        commentsPane.add(sp);
        return commentsPane;
    }

    public void ok() {
        logger.info("");
        String comments = commentsTextArea.getText().trim();
        if (comments.length() < 1) {
            comments = null;
        }
        action.setComments(comments);
        dispose();
        option = GOkCancelDialog.OK_OPTION;
    }

    public void cancel() {
        logger.info("");
        dispose();
    }

    public int getOption() {
        return option;
    }

    private static final long serialVersionUID = 1L;
}
