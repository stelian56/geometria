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

public class GPAddVertexDialog extends JDialog {

	public final Dimension size = new Dimension(280, 230);

	public GPAddVertexDialog(Frame owner, String title, boolean modal)
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
				.createEtchedBorder(), "Reference point",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 75));
		inputPane.add(Box.createHorizontalGlue());
		JPanel leftInputPane = new JPanel();
		leftInputPane.setLayout(new BoxLayout(leftInputPane, BoxLayout.Y_AXIS));
		Dimension textFieldSize = new Dimension(48, 24);
		JTextField textField1 = new JTextField();
		textField1.setText("A1");
		textField1.setPreferredSize(textFieldSize);
		textField1.setMaximumSize(textFieldSize);
		leftInputPane.add(textField1);
		leftInputPane.add(Box.createVerticalGlue());
		inputPane.add(leftInputPane);
		JPanel rightInputPane = new JPanel();
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/AddVertex.png"));
		rightInputPane.add(new JLabel(icon));
		inputPane.add(rightInputPane);
		inputPane.add(Box.createHorizontalGlue());
		topPane.add(inputPane);
		getContentPane().add(topPane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(80, 24);
		JButton drawButton = new JButton("Add");
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
