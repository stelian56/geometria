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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class GPMeasureAreaDialog extends JDialog {

	public final Dimension size = new Dimension(280, 370);

	public GPMeasureAreaDialog(Frame owner, String title, boolean modal)
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
				.createEtchedBorder(), "Reference face",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		JList faceList = new JList(new String[] { "ABC", "BCD", "CDE", "DEA",
				"EAB" });
		faceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		faceList.setSelectedIndex(2);
		JScrollPane sp = new JScrollPane(faceList);
		sp.setPreferredSize(new Dimension(200, 80));
		sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		faceList.setEnabled(false);
		inputPane.add(sp);
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
		Dimension textFieldSize = new Dimension(96, 24);
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
		textField4.setText("area");
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
