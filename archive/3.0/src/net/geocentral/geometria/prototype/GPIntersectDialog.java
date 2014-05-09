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

public class GPIntersectDialog extends JDialog {

	public final Dimension size = new Dimension(300, 255);

	public GPIntersectDialog(Frame owner, String title, boolean modal)
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
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel inputPane = new JPanel();
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		Dimension textFieldSize = new Dimension(48, 24);
		JPanel leftInputPane = new JPanel();
		leftInputPane.setLayout(new BoxLayout(leftInputPane, BoxLayout.Y_AXIS));
		leftInputPane.setMaximumSize(new Dimension(textFieldSize.width, 96));
		JTextField textField11 = new JTextField();
		textField11.setText("A1");
		textField11.setEditable(true);
		textField11.setMaximumSize(textFieldSize);
		leftInputPane.add(textField11);
		leftInputPane.add(Box.createVerticalGlue());
		JTextField textField12 = new JTextField();
		textField12.setText("C1");
		textField12.setEditable(true);
		textField12.setMaximumSize(textFieldSize);
		leftInputPane.add(textField12);
		inputPane.add(leftInputPane);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/Intersect.png"));
		inputPane.add(new JLabel(icon));
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel rightInputPane = new JPanel();
		rightInputPane
				.setLayout(new BoxLayout(rightInputPane, BoxLayout.Y_AXIS));
		rightInputPane.setMaximumSize(new Dimension(textFieldSize.width, 96));
		JTextField textField21 = new JTextField();
		textField21.setText("B1");
		textField21.setEditable(true);
		textField21.setMaximumSize(textFieldSize);
		rightInputPane.add(textField21);
		rightInputPane.add(Box.createVerticalGlue());
		JTextField textField22 = new JTextField();
		textField22.setText("D1");
		textField22.setEditable(true);
		textField22.setMaximumSize(textFieldSize);
		rightInputPane.add(textField22);
		inputPane.add(rightInputPane);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(inputPane);
		getContentPane().add(topPane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(84, 24);
		JButton intersectButton = new JButton("OK");
		intersectButton.setPreferredSize(buttonSize);
		bottomPane.add(intersectButton);
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
