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

public class GPCutDialog extends JDialog {

	public final Dimension size = new Dimension(280, 248);

	public GPCutDialog(Frame owner, String title, boolean modal)
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
				.createEtchedBorder(), "Reference points",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
		inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));
		JPanel topInputPane = new JPanel();
		topInputPane.setLayout(new BoxLayout(topInputPane, BoxLayout.X_AXIS));
		topInputPane.add(Box.createHorizontalGlue());
		topInputPane.add(Box.createRigidArea(new Dimension(40, 10)));
		Dimension textFieldSize = new Dimension(48, 24);
		JTextField textField0 = new JTextField();
		textField0.setText("A1");
		textField0.setPreferredSize(textFieldSize);
		textField0.setMaximumSize(textFieldSize);
		topInputPane.add(textField0);
		topInputPane.add(Box.createHorizontalGlue());
		inputPane.add(topInputPane);
		JPanel bottomInputPane = new JPanel();
		bottomInputPane.setLayout(new BoxLayout(bottomInputPane,
				BoxLayout.X_AXIS));
		JPanel leftInputPane = new JPanel();
		leftInputPane.setLayout(new BoxLayout(leftInputPane, BoxLayout.Y_AXIS));
		leftInputPane.add(Box.createRigidArea(new Dimension(30, 10)));
		JTextField textField1 = new JTextField();
		textField1.setText("B1");
		textField1.setPreferredSize(textFieldSize);
		textField1.setMaximumSize(textFieldSize);
		leftInputPane.add(textField1);
		leftInputPane.add(Box.createVerticalGlue());
		bottomInputPane.add(leftInputPane);
		bottomInputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/images/Cut.png"));
		bottomInputPane.add(new JLabel(icon));
		bottomInputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel rightInputPane = new JPanel();
		rightInputPane
				.setLayout(new BoxLayout(rightInputPane, BoxLayout.Y_AXIS));
		rightInputPane.add(Box.createVerticalGlue());
		JTextField textField2 = new JTextField();
		textField2.setText("C1");
		textField2.setPreferredSize(textFieldSize);
		textField2.setMaximumSize(textFieldSize);
		rightInputPane.add(textField2);
		bottomInputPane.add(rightInputPane);
		inputPane.add(bottomInputPane);
		topPane.add(inputPane);
		getContentPane().add(topPane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(80, 24);
		JButton drawButton = new JButton("Cut");
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
