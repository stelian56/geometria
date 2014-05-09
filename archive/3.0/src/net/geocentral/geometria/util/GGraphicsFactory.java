/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

import net.geocentral.geometria.action.GDocumentHandler;
import net.geocentral.geometria.view.GHelpOkCancelDialog;
import net.geocentral.geometria.view.GOkCancelDialog;
import net.geocentral.geometria.view.GToolBarHandler;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GGraphicsFactory {

    public static final Font DEFAULT_FONT =
        new Font("Sans-Serif", Font.BOLD, 12);

    public enum LocationType {CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT};

    private static Point DIALOG_OFFSET = new Point(30, 30);
    
    private static GGraphicsFactory instance;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    private GGraphicsFactory() {}
    
    public static GGraphicsFactory getInstance() {
        if (instance == null)
            instance = new GGraphicsFactory();
        return instance;
    }
    
    public void initFont(Element node) {
        logger.info("");
        String fontName;
        int fontStyle;
        int fontSize;
        NodeList ns = node.getElementsByTagName("name");
        if (ns.getLength() > 0)
            fontName = ns.item(0).getTextContent();
        else
            fontName = DEFAULT_FONT.getName();
        ns = node.getElementsByTagName("bold");
        if (ns.getLength() > 0) {
            boolean bold = Boolean.valueOf(ns.item(0).getTextContent());
            fontStyle = bold ? Font.BOLD : Font.PLAIN;
        }
        else
            fontStyle = DEFAULT_FONT.getStyle();
        ns = node.getElementsByTagName("size");
        if (ns.getLength() > 0)
            fontSize = Integer.valueOf(ns.item(0).getTextContent());
        else
            fontSize = DEFAULT_FONT.getSize();
        Font font = new Font(fontName, fontStyle, fontSize);
        setFont(font);
    }
    
    private void setFont(Font font) {
        logger.info("");
        Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource)
                UIManager.put(key, font);
        }
    }

    public JPanel createOkCancelPane(final GOkCancelDialog target) {
        JPanel pane = new JPanel();
        JButton[] buttons = new JButton[2];
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.setBorder(new EmptyBorder(15, 13, 17, 13));
        buttons[0] = createButton(GDictionary.get("OK"));
        buttons[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                target.ok();
            }
        });
        pane.add(buttons[0]);
        pane.add(createSmallRigidArea());
        buttons[1] = createButton(GDictionary.get("Cancel"));
        buttons[1].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                target.cancel();
            }
        });
        pane.add(buttons[1]);
        adjustSize(buttons);
        return pane;
    }

    public JPanel createHelpOkCancelPane(
            final GHelpOkCancelDialog target, final String helpId) {
        JPanel pane = new JPanel();
        JButton[] buttons = new JButton[3];
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.setBorder(new EmptyBorder(15, 13, 17, 13));
        buttons[0] = createButton(GDictionary.get("Help"));
        buttons[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                GHelpManager.getInstance().displayPage(helpId, (JDialog) target);
            }
        });
        pane.add(buttons[0]);
        pane.add(createSmallRigidArea());
        pane.add(Box.createHorizontalGlue());
        buttons[1] = createButton(GDictionary.get("OK"));
        buttons[1].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                target.ok();
            }
        });
        pane.add(buttons[1]);
        pane.add(createSmallRigidArea());
        buttons[2] = createButton(GDictionary.get("Cancel"));
        buttons[2].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                target.cancel();
            }
        });
        pane.add(buttons[2]);
        adjustSize(buttons);
        return pane;
    }

    public JPanel createTitledBorderPane(String title) {
        JPanel pane = new JPanel();
        createTitledBorder(title, pane);
        return pane;
    }

    public void createTitledBorder(String title, JComponent comp) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " "
                + title + " ", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_JUSTIFICATION);
        comp.setBorder(BorderFactory.createCompoundBorder(BorderFactory
            .createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
            0, 10), tb), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    public JTextField createLongInput(String text) {
        Dimension size = new Dimension(250, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JTextField createAuthorInput(String text) {
        Dimension size = new Dimension(500, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JTextArea createCommentsArea(String text) {
        Dimension size = new Dimension(500, 250);
        JTextArea textArea = new JTextArea();
        textArea.setPreferredSize(size);
        textArea.setMaximumSize(size);
        textArea.setText(text);
        return textArea;
    }

    public JTextField createVariableInput(String text) {
        Dimension size = new Dimension(160, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JTextField createLabelInput(String text) {
        Dimension size = new Dimension(48, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JTextField createNumberInput(String text) {
        Dimension size = new Dimension(48, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JTextField createAnswerInput(String text) {
        Dimension size = new Dimension(Short.MAX_VALUE, 24);
        JTextField textField = new JTextField();
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setText(text);
        return textField;
    }

    public JComboBox createComboBox(String[] labels) {
        JComboBox comboBox = new JComboBox(labels);
        int maxLengthIndex = 0;
        int maxLength = labels[maxLengthIndex].length();
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].length() > maxLength) {
                maxLengthIndex = i;
                maxLength = labels[maxLengthIndex].length();
            }
        }
        comboBox.setPrototypeDisplayValue(labels[maxLengthIndex]);
        return comboBox;
    }

    public JButton createButton(String text) {
        JButton button = new JButton(text);
        return button;
    }

    public JButton createButton(AbstractAction action) {
        JButton button = new JButton(action);
        button.setText(
                action.getValue(AbstractAction.SHORT_DESCRIPTION).toString());
        return button;
    }

    public JPanel createContainerAdjustBottom(JComponent comp) {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(Box.createVerticalGlue());
        pane.add(comp);
        pane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return pane;
    }

    public JPanel createContainerAdjustCenter(JComponent comp) {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(Box.createVerticalGlue());
        pane.add(comp);
        pane.add(Box.createVerticalGlue());
        pane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return pane;
    }

    public JPanel createContainerAdjustCenter(JComponent comp1,
            JComponent comp2, int verticalPadding) {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(comp1);
        pane.add(Box.createRigidArea(new Dimension(10, verticalPadding)));
        pane.add(comp2);
        pane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return pane;
    }

    public JPanel createImagePane(String imageUrl) {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        ImageIcon icon =
            new ImageIcon(GGraphicsFactory.class.getResource(imageUrl));
        pane.add(new JLabel(icon));
        return pane;
    }

    public GValueInputPane createVariableInputPane(String variable, String title) {
        return new GValueInputPane(variable, title);
    }

    public Component createSmallRigidArea() {
        return Box.createRigidArea(new Dimension(10, 10));
    }

    public String showInputDialog(String message) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        String title = GDictionary.get(GApplicationManager.getInstance().getApplicationName());
        return JOptionPane.showInputDialog(owner, message, title,
            JOptionPane.QUESTION_MESSAGE);
    }

    public int showYesNoCancelDialog(String message) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        String title = GDictionary.get(GApplicationManager.getInstance().getApplicationName());
        return JOptionPane.showConfirmDialog(owner, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public int showQuestionDialog(String message, int optionType, LocationType locationType) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        String title = GDictionary.get(GApplicationManager.getInstance().getApplicationName());
        JOptionPane pane = new JOptionPane(message);
        pane.setOptionType(optionType);
        pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        JDialog dialog = pane.createDialog(owner, title);
        Point location = getLocation(owner, dialog, locationType);
        dialog.setLocation(location.x, owner.getY() + location.y);
        dialog.setVisible(true);
        Object value = pane.getValue();
        if (value == null)
            return JOptionPane.CLOSED_OPTION;
        return ((Integer)value);
    }
    
    Point getLocation(Component owner, Component component, LocationType locationType) {
        switch (locationType) {
        case TOP_LEFT:
            return new Point(owner.getX() + DIALOG_OFFSET.x, owner.getY() + DIALOG_OFFSET.y);
        case BOTTOM_LEFT:
            return new Point(owner.getX() + DIALOG_OFFSET.x,
                    owner.getX() + owner.getHeight() - component.getHeight() - DIALOG_OFFSET.y);
        case TOP_RIGHT:
            return new Point(owner.getX() + owner.getWidth() - component.getWidth() - DIALOG_OFFSET.x,
                    owner.getY() + DIALOG_OFFSET.y);
        case BOTTOM_RIGHT:
            return new Point(owner.getX() + owner.getWidth() -component.getWidth() - DIALOG_OFFSET.x,
                    owner.getX() + owner.getHeight() - component.getHeight() - DIALOG_OFFSET.y);
        default:
            return new Point((owner.getX() + owner.getWidth() - component.getWidth()) / 2,
                (owner.getLocation().y + owner.getHeight() - component.getHeight()) / 2);
        }
    }
    
    public int showYesNoDialog(String message) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        String title = GDictionary.get(
                GApplicationManager.getInstance().getApplicationName());
        return JOptionPane.showConfirmDialog(owner, message, title,
                JOptionPane.YES_NO_OPTION);
    }

    public void showMessageDialog(String message) {
        showMessageDialog(message, null);
    }

    public void showMessageDialog(String message, String iconFile) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        showMessageDialog(owner, message, iconFile);
    }

    public void showMessageDialog(Container owner, String message, String iconFile) {
        String title = GDictionary.get(GApplicationManager.getInstance().getApplicationName());
        ImageIcon icon = iconFile == null ? null : GIconManager.getInstance().getIcon(iconFile);
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.PLAIN_MESSAGE, icon);
    }

    public void showErrorDialog(String message) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        showErrorDialog(owner, message);
    }

    public void showErrorDialog(Container owner, String message) {
        String title = GDictionary.get(
                GApplicationManager.getInstance().getApplicationName());
        JOptionPane.showMessageDialog(owner, message, title,
                JOptionPane.ERROR_MESSAGE);
    }

    public Color showColorChooser(Color color) {
        Container owner = GDocumentHandler.getInstance().getOwnerFrame();
        String title = GDictionary.get("SetColor");
        return JColorChooser.showDialog(owner, title, color);
    }
    
    public void adjustSize(JComponent[] components) {
        int maxWidth = 0;
        for (JComponent component : components) {
            int width = component.getPreferredSize().width;
            if (width > maxWidth)
                maxWidth = width;
        }
        for (JComponent component : components) {
            int height = component.getPreferredSize().height;
            component.setPreferredSize(new Dimension(maxWidth, height));
        }
    }

    public void setLocation(JDialog dialog, Component owner, LocationType locationType) {
        Point location = getLocation(owner, dialog, locationType);
        dialog.setLocation(location);
    }

    public void layoutToolBars(Container contentPane) {
        GToolBarHandler toolBarHandler = GToolBarHandler.getInstance();
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(toolBarHandler.getToolBar("Document"));
        pane1.add(toolBarHandler.getToolBar("Measure"));
        pane1.add(toolBarHandler.getToolBar("Draw"));
        pane1.add(toolBarHandler.getToolBar("Help"));
        pane1.add(Box.createHorizontalGlue());
        contentPane.add(pane1);
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(toolBarHandler.getToolBar("Edit"));
        pane2.add(toolBarHandler.getToolBar("Figure"));
        pane2.add(toolBarHandler.getToolBar("Transform"));
        pane2.add(toolBarHandler.getToolBar("View"));
        pane2.add(Box.createHorizontalGlue());
        contentPane.add(pane2);
    }

    public void showAnswerEvaluation(boolean correct) {
        logger.info(correct);
        String message = correct ? GDictionary.get("AnswerIsCorrect") : GDictionary.get("AnswerIsIncorrect");
        String iconFile = correct ? "Correct.png" : "Incorrect.png";
        showMessageDialog(message, iconFile);
    }
}
