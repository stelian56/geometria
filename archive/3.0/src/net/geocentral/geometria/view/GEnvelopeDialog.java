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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.geocentral.geometria.action.GEnvelopeAction;
import net.geocentral.geometria.model.GAuthor;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GDocumentEnvelope;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GEnvelopeDialog extends JDialog implements GHelpOkCancelDialog {

    private GDocument document;

    private GEnvelopeAction action;

    private JTextField authorNameTextField;

    private JTextField authorEmailTextField;

    private JTextField authorWebTextField;

    private JTextArea commentsTextArea;

    private int option = CANCEL_OPTION;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GEnvelopeDialog(Frame ownerFrame, GDocument document, GEnvelopeAction action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        this.document = document;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.CENTER);
        setTitle(GDictionary.get("Envelope"));
        setResizable(true);
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
        inputPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel authorPane = layoutAuthorPane();
        inputPane.add(authorPane);
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel commentsPane = layoutCommentsPane();
        inputPane.add(commentsPane);
        getContentPane().add(inputPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    private JPanel layoutAuthorPane() {
        logger.info("");
        JPanel authorPane = GGraphicsFactory.getInstance().createTitledBorderPane( GDictionary.get("Author"));
        authorPane.setLayout(new BoxLayout(authorPane, BoxLayout.X_AXIS));
        JPanel labelPane = new JPanel();
        labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.Y_AXIS));
        labelPane.add(new JLabel(GDictionary.get("Name")));
        labelPane.add(Box.createVerticalGlue());
        labelPane.add(new JLabel(GDictionary.get("Email")));
        labelPane.add(Box.createVerticalGlue());
        labelPane.add(new JLabel(GDictionary.get("Web")));
        authorPane.add(labelPane);
        authorPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
        authorNameTextField = GGraphicsFactory.getInstance().createAuthorInput(
                document.getEnvelope().getAuthor().getName());
        inputPane.add(authorNameTextField);
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        authorEmailTextField = GGraphicsFactory.getInstance().createAuthorInput(
                document.getEnvelope().getAuthor().getEmail());
        inputPane.add(authorEmailTextField);
        inputPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        authorWebTextField = GGraphicsFactory.getInstance().createAuthorInput(
                document.getEnvelope().getAuthor().getWeb());
        inputPane.add(authorWebTextField);
        authorPane.add(inputPane);
        return authorPane;
    }

    private JPanel layoutCommentsPane() {
        logger.info("");
        JPanel commentsPane = GGraphicsFactory.getInstance()
            .createTitledBorderPane(GDictionary.get("Comments"));
        commentsPane.setLayout(new BoxLayout(commentsPane, BoxLayout.X_AXIS));
        commentsTextArea = GGraphicsFactory.getInstance().createCommentsArea(document.getEnvelope().getComments());
        commentsTextArea.setLineWrap(true);
        commentsTextArea.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(commentsTextArea);
        commentsPane.add(sp);
        return commentsPane;
    }

    public void ok() {
        logger.info("");
        String authorName = authorNameTextField.getText().trim();
        String authorEmail = authorEmailTextField.getText().trim();
        String authorWeb = authorWebTextField.getText().trim();
        GAuthor author = new GAuthor(authorName, authorEmail, authorWeb);
        String comments = commentsTextArea.getText().trim();
        GDocumentEnvelope envelope = new GDocumentEnvelope(author, comments);
        document.setEnvelope(envelope);
        dispose();
        option = OK_OPTION;
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
