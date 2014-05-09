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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class GPLayAngleDialog extends JDialog {

	public final Dimension size = new Dimension(280, 416);

	public GPLayAngleDialog(Frame owner, String title, boolean modal)
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
				.createEtchedBorder(), "Lay angle",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		JTextField textField1 = new JTextField();
		textField1.setText("angle");
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
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.X_AXIS));
		inputPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));
		JPanel leftInputPane = new JPanel();
		leftInputPane.setLayout(new BoxLayout(leftInputPane, BoxLayout.Y_AXIS));
		leftInputPane.add(Box.createVerticalGlue());
		JTextField textField3 = new JTextField();
		textField3.setText("A1");
		textField3.setMaximumSize(textFieldSize);
		leftInputPane.add(textField3);
		inputPane.add(leftInputPane);
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/LayAngle.png"));
		inputPane.add(new JLabel(icon));
		inputPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel rightInputPane = new JPanel();
		rightInputPane
				.setLayout(new BoxLayout(rightInputPane, BoxLayout.Y_AXIS));
		rightInputPane.add(Box.createVerticalGlue());
		JTextField textField4 = new JTextField();
		textField4.setText("B1");
		textField4.setMaximumSize(textFieldSize);
		rightInputPane.add(textField4);
		inputPane.add(rightInputPane);
		middlePane.add(inputPane);
		middlePane.add(Box.createRigidArea(new Dimension(20, 20)));
		getContentPane().add(middlePane);

		JPanel lowerMiddlePane = new JPanel();
		lowerMiddlePane.setLayout(new BoxLayout(lowerMiddlePane,
				BoxLayout.Y_AXIS));
		TitledBorder tb3 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Reference face",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		lowerMiddlePane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(BorderFactory
						.createEmptyBorder(10, 10, 0, 10), tb3), BorderFactory
						.createEmptyBorder(0, 10, 10, 10)));
		JList faceList = new JList(new String[] { "ABC", "BCD" });
		faceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		faceList.setSelectedIndex(0);
		JScrollPane sp = new JScrollPane(faceList);
		sp.setPreferredSize(new Dimension(200, 40));
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		lowerMiddlePane.add(sp);
		lowerMiddlePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		getContentPane().add(lowerMiddlePane);

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
