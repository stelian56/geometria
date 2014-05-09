/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.model.GCalculator;
import net.geocentral.geometria.model.GDocument;
import net.geocentral.geometria.model.GFigure;
import net.geocentral.geometria.model.GProblem;
import net.geocentral.geometria.model.GSolution;
import net.geocentral.geometria.util.GDictionary;
import net.geocentral.geometria.util.GGraphicsFactory;

import org.apache.log4j.Logger;

public class GMainPanel extends JPanel implements DocumentListener {

    public static final Dimension FIGURES_PANE_SIZE = new Dimension(550, 550);
    
    private JSplitPane contentPane;

    private JTextArea textPane;

    private GNotepadPane notepadPane;

    private JSplitPane rightPane;

    private GFiguresPane figuresPane;

    private GLogPane logPane;
    
    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public GMainPanel() {
        logger.info("");
    }

    public void layoutComponents() {
        logger.info("");
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        contentPane = new JSplitPane();
        add(contentPane);
        JPanel toolPane = new JPanel();
        toolPane.setBorder(BorderFactory.createEmptyBorder(10, 3, 3, 3));
        toolPane.setLayout(new BoxLayout(toolPane, BoxLayout.Y_AXIS));
        textPane = new JTextArea();
        textPane.setEditable(false);
        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(textPane);
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory
                .createLineBorder(Color.gray, 1),
                GDictionary.get("Problem"),
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
        sp.setBorder(tb);
        sp.setPreferredSize(new Dimension(150, 150));
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 150));
        toolPane.add(sp);
        toolPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        JPanel calcPane = new JPanel();
        calcPane.setLayout(new BorderLayout());
        TitledBorder cb = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), GDictionary.get("Calculator"),
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
        calcPane.setBorder(cb);
        JTextField textField =
            GGraphicsFactory.getInstance().createLongInput("");
        GCalculator calculator = GDocumentHandler.getInstance().getCalculator();
        calculator.setTextField(textField);
        calcPane.add(textField, BorderLayout.CENTER);
        calcPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        toolPane.add(calcPane);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        JButton[] buttons = new JButton[2];
        buttonPane.add(Box.createHorizontalGlue());
        AbstractAction actionHandler = GDocumentHandler.getInstance().getActionHandler("calculator.evaluate");
        buttons[0] = GGraphicsFactory.getInstance().createButton(actionHandler);
        buttons[0].setIcon(null);
        buttons[0].setText(GDictionary.get("Evaluate"));
        buttonPane.add(buttons[0]);
        buttonPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        actionHandler = GDocumentHandler.getInstance().getActionHandler("calculator.clear");
        buttons[1] = GGraphicsFactory.getInstance().createButton(actionHandler);
        buttons[1].setIcon(null);
        buttons[1].setText(GDictionary.get("Clear"));
        buttonPane.add(buttons[1]);
        buttonPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        toolPane.add(buttonPane);
        GGraphicsFactory.getInstance().adjustSize(buttons);
        toolPane.add(GGraphicsFactory.getInstance().createSmallRigidArea());
        notepadPane = new GNotepadPane();
        TitledBorder nb = BorderFactory.createTitledBorder(BorderFactory
                .createLineBorder(Color.gray, 1), GDictionary.get("Notepad"),
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
        notepadPane.setBorder(nb);
        toolPane.add(notepadPane);
        contentPane.setLeftComponent(toolPane);
        contentPane.setDividerSize(8);
        rightPane = new JSplitPane();
        rightPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        figuresPane = new GFiguresPane();
        logPane = new GLogPane();
        rightPane.setTopComponent(figuresPane);
        figuresPane.setPreferredSize(FIGURES_PANE_SIZE);
        rightPane.setBottomComponent(logPane);
        rightPane.setDividerLocation((int)(rightPane.getPreferredSize().height * 0.75));
        rightPane.setDividerSize(8);
        contentPane.setRightComponent(figuresPane);
    }

    public void documentChanged(GDocument document) {
        logger.info("");
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        int rightPaneDividerLocation = rightPane.getDividerLocation();
        int contentPaneDividerLocation = contentPane.getDividerLocation();
        if (document instanceof GProblem) {
            contentPane.setRightComponent(figuresPane);
            Document text = ((GProblem)document).getText();
            textPane.setDocument(text);
            text.addDocumentListener(this);
            textPane.setEditable(true);
            figuresPane.documentChanged(document);
            notepadPane.documentChanged(document);
        }
        else if (document instanceof GSolution) {
            GSolution masterSolution = documentHandler.getMasterSolution();
            contentPane.setRightComponent(rightPane);
            if (rightPane.getTopComponent() == null) {
                rightPane.setTopComponent(figuresPane);
                rightPane.setDividerLocation(rightPaneDividerLocation);
            }
            Document text = ((GSolution)masterSolution).getProblem().getText();
            textPane.setDocument(text);
            textPane.setEditable(false);
            figuresPane.documentChanged(document);
            notepadPane.documentChanged(document);
            logPane.documentChanged(masterSolution);
        }
        else if (document == null) {
            figuresPane.removeAllFigures();
            contentPane.setRightComponent(figuresPane);
            textPane.setDocument(new PlainDocument());
            textPane.setEditable(false);
            notepadPane.documentChanged(null);
        }
        contentPane.setDividerLocation(contentPaneDividerLocation);
    }

    public void figureSelectionChanged() {
        logger.info("");
        figuresPane.selectionChanged();
    }
    
    public void notepadChanged(GDocument document) {
        logger.info("");
        notepadPane.repaintRecords();
    }

    public void addFigure(GFigure figure, int index) {
        logger.info(figure + ", " + index);
        figuresPane.addFigure(figure, index);
    }

    public void addFigure(GFigure figure) {
        logger.info(figure);
        figuresPane.addFigure(figure);
    }

    public void removeFigure(String figureName) {
        logger.info(figureName);
        figuresPane.removeFigure(figureName);
    }

    public void renameFigure(String oldName, String newName) {
        logger.info(oldName + ", " + newName);
        figuresPane.renameFigure(oldName, newName);
    }

    public void changedUpdate(DocumentEvent e) {
        textUpdated();
    }

    public void insertUpdate(DocumentEvent e) {
        textUpdated();
    }

    public void removeUpdate(DocumentEvent e) {
        textUpdated();
    }

    private void textUpdated() {
        GDocumentHandler documentHandler = GDocumentHandler.getInstance();
        documentHandler.setDocumentModified(true);
        documentHandler.updateSaveActionHandlerState(true);
    }
    
    private static final long serialVersionUID = 1L;
}
