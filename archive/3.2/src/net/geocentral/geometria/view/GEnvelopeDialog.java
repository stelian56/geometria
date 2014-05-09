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
import javax.swing.WindowConstants;

import net.geocentral.geometria.action.GEnvelopeAction;
import net.geocentral.geometria.model.GAuthor;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GDocumentEnvelope;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;
import net.geocentral.geometria.util.GGraphicsFactory.LocationType;

import org.apache.log4j.Logger;

public class GEnvelopeDialog extends JDialog implements GHelpOkCancelDialog {

    private GDocument document;

    private GEnvelopeAction action;

    private GEnvelopeEditPane editPane;
    
    private int option = CANCEL_OPTION;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GEnvelopeDialog(Frame ownerFrame, GDocument document, GEnvelopeAction action) {
        super(ownerFrame, true);
        logger.info("");
        this.action = action;
        this.document = document;
        editPane = new GEnvelopeEditPane(document);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        layoutComponents();
        pack();
        GGraphicsFactory.getInstance().setLocation(this, ownerFrame, LocationType.TOP_LEFT);
        setTitle(GDictionary.get("Envelope"));
        setResizable(true);
    }

    private void layoutComponents() {
        logger.info("");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        String problemLabel = GDictionary.get("Problem");
        String solutionLabel = GDictionary.get("Solution");
        String editPaneTitle;
        if (document instanceof GSolution) {
            JPanel problemPane = GGraphicsFactory.getInstance().createTitledBorderPane(problemLabel, false);
            problemPane.setLayout(new BoxLayout(problemPane, BoxLayout.Y_AXIS));
            GDocument problem = ((GSolution)document).getProblem();
            GEnvelopeViewPane viewPane = new GEnvelopeViewPane(problem);
            viewPane.layoutComponents();
            problemPane.add(viewPane);
            getContentPane().add(problemPane);
            editPaneTitle = solutionLabel;
        }
        else {
            editPaneTitle = problemLabel;
        }
        JPanel documentPane = GGraphicsFactory.getInstance().createTitledBorderPane(editPaneTitle, false);
        documentPane.setLayout(new BoxLayout(documentPane, BoxLayout.Y_AXIS));
        editPane.layoutComponents();
        documentPane.add(editPane);
        documentPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        getContentPane().add(documentPane);
        JPanel helpOkCancelPane = GGraphicsFactory.getInstance().createHelpOkCancelPane(this, action.getHelpId());
        getContentPane().add(helpOkCancelPane);
    }

    public void ok() {
        logger.info("");
        String authorName = editPane.getAuthorName();
        String authorEmail = editPane.getAuthorEmail();
        String authorWeb = editPane.getAuthorWeb();
        GAuthor author = new GAuthor(authorName, authorEmail, authorWeb);
        String comments = editPane.getComments();
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
