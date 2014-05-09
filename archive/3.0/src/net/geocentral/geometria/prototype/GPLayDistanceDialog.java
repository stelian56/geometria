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

public class GPLayDistanceDialog extends JDialog {

	public final Dimension size = new Dimension(280, 340);

	public GPLayDistanceDialog(Frame owner, String title, boolean modal)
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
		TitledBorder tb1 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Lay distance",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		JTextField textField1 = new JTextField();
		textField1.setText("dist");
		textField1.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
		topPane.add(textField1);
		getContentPane().add(topPane);

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BoxLayout(middlePane, BoxLayout.Y_AXIS));
		TitledBorder tb2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Reference points",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		middlePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb2), BorderFactory.createEmptyBorder(10, 10,
				0, 10)));

		Dimension textFieldSize = new Dimension(48, 24);
		JPanel middleInputPane = new JPanel();
		middleInputPane.setLayout(new BoxLayout(middleInputPane,
				BoxLayout.X_AXIS));
		middleInputPane.add(Box.createRigidArea(new Dimension(12, 12)));
		JTextField textField2 = new JTextField();
		textField2.setText("A1");
		textField2.setMaximumSize(textFieldSize);
		middleInputPane.add(textField2);
		middlePane.add(middleInputPane);
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));
		JPanel leftInputPane = new JPanel();
		leftInputPane.setLayout(new BoxLayout(leftInputPane, BoxLayout.Y_AXIS));
		leftInputPane.add(Box.createVerticalGlue());
		JTextField textField3 = new JTextField();
		textField3.setText("B1");
		textField3.setMaximumSize(textFieldSize);
		leftInputPane.add(textField3);
		inputPane.add(leftInputPane);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/LayDistance.png"));
		inputPane.add(new JLabel(icon));
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel rightInputPane = new JPanel();
		rightInputPane
				.setLayout(new BoxLayout(rightInputPane, BoxLayout.Y_AXIS));
		rightInputPane.add(Box.createVerticalGlue());
		JTextField textField4 = new JTextField();
		textField4.setText("C1");
		textField4.setMaximumSize(textFieldSize);
		rightInputPane.add(textField4);
		inputPane.add(rightInputPane);
		middlePane.add(inputPane);
		middlePane.add(Box.createRigidArea(new Dimension(20, 20)));
		getContentPane().add(middlePane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(80, 24);
		JButton drawButton = new JButton("Draw");
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
