/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.geocentral.geometria.model.GAuthor;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GDocumentEnvelope;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GEnvelopeViewPane extends JPanel {

    private GDocument document;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GEnvelopeViewPane(GDocument document) {
        logger.info("");
        this.document = document;
    }

    public void layoutComponents() {
        logger.info("");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        GDocumentEnvelope envelope = document.getEnvelope();
        JPanel authorWrapPane = new JPanel();
        authorWrapPane.setLayout(new BoxLayout(authorWrapPane, BoxLayout.X_AXIS));
        authorWrapPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel authorPane = new JPanel();
        authorPane.setLayout(new BoxLayout(authorPane, BoxLayout.Y_AXIS));
        GAuthor author = envelope.getAuthor();
        String authorName = author.getName();
        if (!authorName.isEmpty()) {
            authorPane.add(new JLabel(String.format("%s: %s", GDictionary.get("Author"), authorName)));
        }
        String authorEmail = author.getEmail();
        if (!authorEmail.isEmpty()) {
            authorPane.add(new JLabel(String.format("%s: %s", GDictionary.get("Email"), authorEmail)));
        }
        String authorWeb = author.getWeb();
        if (!authorWeb.isEmpty()) {
            authorPane.add(new JLabel(String.format("%s: %s", GDictionary.get("Web"), authorWeb)));
        }
        authorWrapPane.add(authorPane);
        authorWrapPane.add(Box.createHorizontalGlue());
        add(authorWrapPane);
        String comments = envelope.getComments();
        if (!comments.isEmpty()) {
            JPanel commentsPane = GGraphicsFactory.getInstance().createTitledBorderPane(GDictionary.get("Comments"));
            commentsPane.setLayout(new BoxLayout(commentsPane, BoxLayout.Y_AXIS));
            JTextArea commentsTextArea = new JTextArea(comments);
            commentsTextArea.setBackground(new Color(0xee, 0Xee, 0xee));
            commentsTextArea.setEditable(false);
            JScrollPane sp = GGraphicsFactory.getInstance().createCommentsArea(commentsTextArea);
            commentsPane.add(sp);
            add(commentsPane);
        }
        add(GGraphicsFactory.getInstance().createSmallRigidArea());
    }

    private static final long serialVersionUID = 1L;
}
