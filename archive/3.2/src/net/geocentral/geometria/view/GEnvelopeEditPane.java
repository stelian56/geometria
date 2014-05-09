/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GEnvelopeEditPane extends JPanel {

    private GDocument document;

    private JTextField authorNameTextField;

    private JTextField authorEmailTextField;

    private JTextField authorWebTextField;

    private JTextArea commentsTextArea;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GEnvelopeEditPane(GDocument document) {
        logger.info("");
        this.document = document;
    }

    public void layoutComponents() {
        logger.info("");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel authorPane = layoutAuthorPane();
        add(authorPane);
        JPanel commentsPane = layoutCommentsPane();
        add(commentsPane);
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
        JPanel commentsPane = GGraphicsFactory.getInstance().createTitledBorderPane(GDictionary.get("Comments"));
        commentsPane.setLayout(new BoxLayout(commentsPane, BoxLayout.Y_AXIS));
        commentsTextArea = new JTextArea(document.getEnvelope().getComments()); 
        JScrollPane sp = GGraphicsFactory.getInstance().createCommentsArea(commentsTextArea);
        commentsPane.add(sp);
        return commentsPane;
    }

    public String getAuthorName() {
        return authorNameTextField.getText().trim();
    }

    public String getAuthorEmail() {
        return authorEmailTextField.getText().trim();
    }

    public String getAuthorWeb() {
        return authorWebTextField.getText().trim();
    }
    
    public String getComments() {
        return commentsTextArea.getText().trim();
    }

    private static final long serialVersionUID = 1L;
}
