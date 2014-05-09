/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.prototype;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class GPDivideAngleDialog extends JDialog {

	public final Dimension size = new Dimension(280, 324);

	public GPDivideAngleDialog(Frame owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
		setSize(size);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		layoutComponents();
		setVisible(true);
	}

	private void layoutComponents() {
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
		TitledBorder tb1 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Refrence points",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(0, 10,
				10, 10)));

		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		Dimension textFieldSize = new Dimension(48, 24);
		JTextField textField1 = new JTextField();
		textField1.setText("B1");
		textField1.setMaximumSize(textFieldSize);
		inputPane.add(textField1);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/DivideAngle.png"));
		inputPane.add(new JLabel(icon));
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel rightInputPane = new JPanel();
		rightInputPane
				.setLayout(new BoxLayout(rightInputPane, BoxLayout.Y_AXIS));
		rightInputPane.setMaximumSize(new Dimension(textFieldSize.width, 96));
		JTextField textField21 = new JTextField();
		textField21.setText("A1");
		textField21.setMaximumSize(textFieldSize);
		rightInputPane.add(textField21);
		rightInputPane.add(Box.createVerticalGlue());
		JTextField textField22 = new JTextField();
		textField22.setText("C1");
		textField22.setMaximumSize(textFieldSize);
		rightInputPane.add(textField22);
		inputPane.add(rightInputPane);
		topPane.add(inputPane);
		getContentPane().add(topPane);

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BoxLayout(middlePane, BoxLayout.X_AXIS));
		TitledBorder tb2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Ratio",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		middlePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb2), BorderFactory.createEmptyBorder(0, 10,
				10, 10)));
		middlePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		getContentPane().add(middlePane);
		middlePane.add(Box.createHorizontalGlue());
		JTextField textField4 = new JTextField();
		textField4.setText("3");
		textField4.setPreferredSize(textFieldSize);
		textField4.setMaximumSize(textFieldSize);
		middlePane.add(textField4);
		middlePane.add(Box.createRigidArea(new Dimension(10, 10)));
		JLabel label = new JLabel("to");
		middlePane.add(label);
		middlePane.add(Box.createRigidArea(new Dimension(10, 10)));
		JTextField textField5 = new JTextField();
		textField5.setText("2");
		textField5.setPreferredSize(textFieldSize);
		textField5.setMaximumSize(textFieldSize);
		middlePane.add(textField5);
		middlePane.add(Box.createHorizontalGlue());
		middlePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

		getContentPane().add(middlePane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(80, 24);
		JButton drawButton = new JButton("Divide");
		drawButton.setPreferredSize(buttonSize);
		bottomPane.add(drawButton);
		bottomPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(buttonSize);
		bottomPane.add(cancelButton);
		bottomPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton helpButton = new JButton("Help");
		helpButton.setPreferredSize(buttonSize);
		bottomPane.add(helpButton);
		bottomPane.add(Box.createHorizontalGlue());
		getContentPane().add(bottomPane);
	}

	private static final long serialVersionUID = 1L;
}
