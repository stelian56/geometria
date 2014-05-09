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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class GPJoinDialog extends JDialog {

	public final Dimension size = new Dimension(280, 450);

	public GPJoinDialog(Frame owner, String title, boolean modal)
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
				.createEtchedBorder(), "Reference solids",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		topPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb1), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel topInputPane = new JPanel();
		topInputPane.setLayout(new BoxLayout(topInputPane, BoxLayout.X_AXIS));

		JPanel topLeftPane = new JPanel();
		topLeftPane.setLayout(new BoxLayout(topLeftPane, BoxLayout.Y_AXIS));
		JScrollPane sp;
		JList leftSolidList = new JList(new String[] { "ABCDE", "ZXHJK" });
		leftSolidList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftSolidList.setSelectedIndex(0);
		sp = new JScrollPane(leftSolidList);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		topLeftPane.add(sp);
		topInputPane.add(topLeftPane);

		topInputPane.add(Box.createRigidArea(new Dimension(20, 20)));

		JPanel topRightPane = new JPanel();
		topRightPane.setLayout(new BoxLayout(topRightPane, BoxLayout.Y_AXIS));
		JList rightSolidList = new JList(new String[] { "ABCDE", "ZXHJK" });
		rightSolidList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rightSolidList.setSelectedIndex(1);
		sp = new JScrollPane(rightSolidList);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		topRightPane.add(sp);
		topInputPane.add(topRightPane);
		topPane.add(topInputPane);
		topInputPane.setPreferredSize(new Dimension(260, 70));
		topInputPane.setMaximumSize(new Dimension(260, 70));
		getContentPane().add(topPane);
		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BoxLayout(middlePane, BoxLayout.X_AXIS));
		middlePane.add(Box.createHorizontalGlue());
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/images/Join.png"));
		middlePane.add(new JLabel(icon));
		middlePane.add(Box.createHorizontalGlue());
		getContentPane().add(middlePane);
		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));

		TitledBorder tb2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Reference faces",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_JUSTIFICATION);
		bottomPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10,
						0, 10), tb2), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel bottomInputPane = new JPanel();
		bottomInputPane.setLayout(new BoxLayout(bottomInputPane,
				BoxLayout.X_AXIS));

		JPanel bottomLeftPane = new JPanel();
		bottomLeftPane
				.setLayout(new BoxLayout(bottomLeftPane, BoxLayout.Y_AXIS));
		JList leftFaceList = new JList(new String[] { "ABC", "BCD", "CDE",
				"DEA", "EAB" });
		leftFaceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftFaceList.setSelectedIndex(3);
		sp = new JScrollPane(leftFaceList);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		bottomLeftPane.add(sp);
		bottomInputPane.add(bottomLeftPane);

		bottomInputPane.add(Box.createRigidArea(new Dimension(20, 20)));

		JPanel bottomRightPane = new JPanel();
		bottomRightPane.setLayout(new BoxLayout(bottomRightPane,
				BoxLayout.Y_AXIS));
		JList rightFaceList = new JList(new String[] { "ZXH", "XHJ", "HJK",
				"ZXK", "ZJK" });
		rightFaceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rightFaceList.setSelectedIndex(2);
		sp = new JScrollPane(rightFaceList);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		bottomRightPane.add(sp);
		bottomInputPane.add(bottomRightPane);
		bottomInputPane.setMaximumSize(new Dimension(260, 90));
		bottomInputPane.setPreferredSize(new Dimension(260, 90));

		bottomPane.add(bottomInputPane);
		getContentPane().add(bottomPane);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		Dimension buttonSize = new Dimension(80, 24);
		JButton drawButton = new JButton("Join");
		drawButton.setPreferredSize(buttonSize);
		buttonPane.add(drawButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(buttonSize);
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton helpButton = new JButton("Help");
		helpButton.setPreferredSize(buttonSize);
		buttonPane.add(helpButton);
		buttonPane.add(Box.createHorizontalGlue());
		getContentPane().add(buttonPane);
	}

	private static final long serialVersionUID = 1L;
}
