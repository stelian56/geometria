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

public class GPMeasureDistanceDialog extends JDialog {

	public final Dimension size = new Dimension(280, 320);

	public GPMeasureDistanceDialog(Frame owner, String title, boolean modal)
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
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		Dimension textFieldSize = new Dimension(48, 24);
		JTextField textField1 = new JTextField();
		textField1.setText("A1");
		textField1.setEditable(false);
		textField1.setMaximumSize(textFieldSize);
		inputPane.add(textField1);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/MeasureDistance.png"));
		inputPane.add(new JLabel(icon));
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JTextField textField2 = new JTextField();
		textField2.setText("B1");
		textField2.setEditable(false);
		textField2.setMaximumSize(textFieldSize);
		inputPane.add(textField2);
		topPane.add(inputPane);
		topPane.add(Box.createRigidArea(new Dimension(20, 20)));

		JPanel resultPane = new JPanel();
		resultPane.setLayout(new BoxLayout(resultPane, BoxLayout.X_AXIS));
		JButton measureButton = new JButton("Measure");
		Dimension buttonSize = new Dimension(96, 24);
		measureButton.setEnabled(false);
		measureButton.setPreferredSize(buttonSize);
		resultPane.add(measureButton);
		resultPane.add(Box.createHorizontalGlue());
		JTextField textField3 = new JTextField();
		textFieldSize = new Dimension(96, textFieldSize.height);
		textField3.setPreferredSize(textFieldSize);
		textField3.setMaximumSize(textFieldSize);
		textField3.setText("57.333333");
		textField3.setEditable(false);
		resultPane.add(textField3);
		topPane.add(resultPane);
		getContentPane().add(topPane);

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BoxLayout(middlePane, BoxLayout.X_AXIS));
		TitledBorder tb2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Assign parameter",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		middlePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						10, 10), tb2), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));
		JTextField textField4 = new JTextField();
		textFieldSize = new Dimension(96, textFieldSize.height);
		textField4.setPreferredSize(textFieldSize);
		textField4.setMaximumSize(textFieldSize);
		textField4.setText("dist");
		middlePane.add(textField4);
		middlePane.add(Box.createHorizontalGlue());
		JButton okButton = new JButton("OK");
		okButton.setPreferredSize(buttonSize);
		middlePane.add(okButton);
		getContentPane().add(middlePane);

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		bottomPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bottomPane.add(Box.createHorizontalGlue());
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
