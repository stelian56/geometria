/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.prototype;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

public class GPMainPanel extends JPanel {

	private GPFrame parent;

	private static Logger logger = Logger.getLogger("net.geocentral.geometria");

	public GPMainPanel(GPFrame p) {
		parent = p;
		layoutComponents();
	}

	private void layoutComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		layoutMenu();
		layoutToolBar();
		JSplitPane contentPane = new JSplitPane();
		JPanel toolPane = new JPanel();
		toolPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		toolPane.setLayout(new BoxLayout(toolPane, BoxLayout.Y_AXIS));
		JTextPane textPane = new JTextPane();
		makeTextPane(textPane);
		JScrollPane sp = new JScrollPane(textPane);
		TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray, 1), "Problem",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
		sp.setBorder(tb);
		sp.setPreferredSize(new Dimension(300, 175));
		sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
		toolPane.add(sp);
		toolPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JPanel calcPane = new JPanel();
		calcPane.setLayout(new BorderLayout());
		TitledBorder cb = BorderFactory.createTitledBorder(BorderFactory
				.createEmptyBorder(), "Calculator",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
		calcPane.setBorder(cb);
		JTextField calcTextField = new JTextField();
		makeCalculator(calcTextField);
		calcPane.add(calcTextField, BorderLayout.CENTER);
		calcPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		toolPane.add(calcPane);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.add(Box.createHorizontalGlue());
		JButton evalButton = new JButton("Evaluate");
		buttonPane.add(evalButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton copyButton = new JButton("Copy");
		buttonPane.add(copyButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JButton clearButton = new JButton("Clear");
		buttonPane.add(clearButton);
		buttonPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		toolPane.add(buttonPane);
		toolPane.add(Box.createRigidArea(new Dimension(10, 10)));
		JList notationList = new JList();
		notationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		makeNotepad(notationList);
		sp = new JScrollPane(notationList);
		TitledBorder nb = BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.gray, 1), "Notepad",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP);
		sp.setBorder(nb);
		toolPane.add(sp);
		contentPane.setLeftComponent(toolPane);
		JPanel rightPane = new JPanel();
		rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
		JTabbedPane figsPane = new JTabbedPane();
		GPFigure figure = new GPFigure();
		figure.setPreferredSize(new Dimension(600, 450));
		sp = new JScrollPane(figure);
		figsPane.add("Solid 1", sp);
		rightPane.add(figsPane);
		JPanel solutionLogTitle = new JPanel();
		solutionLogTitle.setLayout(new BoxLayout(solutionLogTitle,
				BoxLayout.X_AXIS));
		solutionLogTitle.add(Box.createRigidArea(new Dimension(10, 10)));
		solutionLogTitle.add(new JLabel("Solutioin Log"));
		solutionLogTitle.add(Box.createHorizontalGlue());
		rightPane.add(solutionLogTitle);
		JList solutionLog = new JList();
		solutionLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		makeSolutionLog(solutionLog);
		sp = new JScrollPane(solutionLog);
		sp.setPreferredSize(new Dimension(600, 100));
		rightPane.add(sp);
		contentPane.setRightComponent(rightPane);
		contentPane.setDividerLocation(0.4);
		contentPane.setDividerSize(8);
		// If split pane added directly to main panel, a ghost left margin
		// appears
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.add(contentPane);
		add(pane);
	}

	public ImageIcon getEmptyIcon() {
		URL url = getClass().getResource("/images/24x24/Empty.png");
		return new ImageIcon(url);
	}

	private JMenuItem makeMenuItem(String name, String icon,
			KeyStroke accelerator) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setAccelerator(accelerator);
		logger.info("icon=" + icon);
		if (icon != null)
			menuItem.setIcon(new ImageIcon(getClass().getResource(
					"/images/24x24/" + icon + ".png")));
		else
			menuItem.setIcon(new ImageIcon(getClass().getResource(
					"/images/24x24/Empty.png")));
		return menuItem;
	}

	private void layoutMenu() {
		JMenuBar menuBar = new JMenuBar();
		parent.setJMenuBar(menuBar);
		// Document menu
		JMenu documentMenu = new JMenu("Document");
		// New menu
		JMenu newDocMenu = new JMenu("New Document");
		newDocMenu.setIcon(getEmptyIcon());
		JMenuItem item = makeMenuItem("Problem", "NewProblem", null);
		newDocMenu.add(item);
		item = makeMenuItem("Solution", "NewSolution", null);
		newDocMenu.add(item);
		documentMenu.add(newDocMenu);
		// Open menu
		JMenu openDocMenu = new JMenu("Open Document");
		openDocMenu.setIcon(getEmptyIcon());
		item = makeMenuItem("Problem", "OpenProblem", KeyStroke.getKeyStroke(
				KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openDocMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(parent);
			}
		});
		item = makeMenuItem("Solution", "OpenSolution", null);
		openDocMenu.add(item);
		documentMenu.add(openDocMenu);
		// Save menu
		item = makeMenuItem("Save Document", "SaveDocument", KeyStroke
				.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		documentMenu.add(item);
		item = makeMenuItem("Save Document As", null, null);
		documentMenu.add(item);
		item = makeMenuItem("Close Document", "CloseDocument", KeyStroke
				.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK));
		documentMenu.add(item);
		item = makeMenuItem("Document Info", "Info", null);
		documentMenu.add(item);
		documentMenu.addSeparator();
		item = makeMenuItem("Preferences", "Preferences", null);
		documentMenu.add(item);
		item = makeMenuItem("Exit", null, KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_MASK));
		documentMenu.add(item);
		menuBar.add(documentMenu);
		// Edit menu
		JMenu editMenu = new JMenu("Edit");
		item = makeMenuItem("Undo", "Undo", KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		editMenu.add(item);
		item = makeMenuItem("Undo History", "UndoHistory", null);
		editMenu.add(item);
		editMenu.addSeparator();
		item = makeMenuItem("Cut", "Cut", null);
		editMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPCutDialog(parent, "Cut", true);
			}
		});
		item = makeMenuItem("Join", "Join", null);
		editMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPJoinDialog(parent, "Join", true);
			}
		});
		item = makeMenuItem("Add Vertex", "AddVertex", null);
		editMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPAddVertexDialog(parent, "Add Vertex", true);
			}
		});
		menuBar.add(editMenu);
		// Solid menu
		JMenu solidMenu = new JMenu("Figure");
		item = makeMenuItem("New Figure", "NewFigure", null);
		solidMenu.add(item);
		JMenu predefinedSolidMenu = new JMenu("Predefined");
		predefinedSolidMenu.setIcon(getEmptyIcon());
		JMenu prismMenu = new JMenu("Prism");
		item = makeMenuItem("3-sided", "Prism3", null);
		prismMenu.add(item);
		item = makeMenuItem("4-sided", "Prism4", null);
		prismMenu.add(item);
		item = makeMenuItem("5-sided", "Prism5", null);
		prismMenu.add(item);
		item = makeMenuItem("6-sided", "Prism6", null);
		prismMenu.add(item);
		predefinedSolidMenu.add(prismMenu);
		JMenu pyramidMenu = new JMenu("Pyramid");
		item = makeMenuItem("3-sided", "Pyramid3", null);
		pyramidMenu.add(item);
		item = makeMenuItem("4-sided", "Pyramid4", null);
		pyramidMenu.add(item);
		item = makeMenuItem("5-sided", "Pyramid5", null);
		pyramidMenu.add(item);
		item = makeMenuItem("6-sided", "Pyramid6", null);
		pyramidMenu.add(item);
		predefinedSolidMenu.add(pyramidMenu);
		JMenu platonicMenu = new JMenu("Platonic");
		item = makeMenuItem("Tetrahedron", "Tetrahedron", null);
		platonicMenu.add(item);
		item = makeMenuItem("Cube", "Cube", null);
		platonicMenu.add(item);
		item = makeMenuItem("Octahedron", "Octahedron", null);
		platonicMenu.add(item);
		item = makeMenuItem("Dodecahedron", "Dodecahedron", null);
		platonicMenu.add(item);
		item = makeMenuItem("Icosahedron", "Icosahedron", null);
		platonicMenu.add(item);
		predefinedSolidMenu.add(platonicMenu);
		solidMenu.add(predefinedSolidMenu);
		solidMenu.addSeparator();
		item = makeMenuItem("Open Figure", "OpenFigure", null);
		solidMenu.add(item);
		item = makeMenuItem("Save Figure", "SaveFigure", null);
		solidMenu.add(item);
		item = makeMenuItem("Save Figure As", null, null);
		solidMenu.add(item);
		item = makeMenuItem("Rename Figure", null, null);
		solidMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showInputDialog(parent, "Rename Figure 1 to",
						"Geometria", JOptionPane.QUESTION_MESSAGE);
			}
		});
		item = makeMenuItem("Remove Figure", "RemoveFigure", null);
		solidMenu.add(item);
		menuBar.add(solidMenu);
		// Transform menu
		JMenu transformMenu = new JMenu("Transform");
		item = makeMenuItem("Scale", "Scale", null);
		transformMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPScaleDialog(parent, "Scale", true);
			}
		});
		item = makeMenuItem("Shear", "Shear", null);
		transformMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPShearDialog(parent, "Shear", true);
			}
		});
		menuBar.add(transformMenu);
		// Measure menu
		JMenu measureMenu = new JMenu("Measure");
		item = makeMenuItem("Distance", "MeasureDistance", null);
		measureMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPMeasureDistanceDialog(parent, "Measure Distance", true);
			}
		});
		item = makeMenuItem("Angle", "MeasureAngle", null);
		measureMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPMeasureAngleDialog(parent, "Measure Angle", true);
			}
		});
		item = makeMenuItem("Area", "Area", null);
		measureMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPMeasureAreaDialog(parent, "Measure Area", true);
			}
		});
		item = makeMenuItem("Volume", "Volume", null);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPMeasureVolumeDialog(parent, "Measure Volume", true);
			}
		});
		measureMenu.add(item);
		menuBar.add(measureMenu);
		// Draw menu
		JMenu drawMenu = new JMenu("Draw");
		item = makeMenuItem("Draw Line", "DrawLine", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPDrawLineDialog(parent, "Draw Line", true);
			}
		});
		item = makeMenuItem("Draw Perpendicular", "Perpendicular", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPDrawPerpendicularDialog(parent, "Draw Perpendicular",
						true);
			}
		});
		item = makeMenuItem("Divide Line", "DivideLine", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPDivideLineDialog(parent, "Divide Line", true);
			}
		});
		item = makeMenuItem("Divide Angle", "DivideAngle", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPDivideAngleDialog(parent, "Divide Angle", true);
			}
		});
		item = makeMenuItem("Intersect", "Intersect", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPIntersectDialog(parent, "Intersect Lines", true);
			}
		});
		item = makeMenuItem("Lay Distance", "LayDistance", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPLayDistanceDialog(parent, "Lay Distance", true);
			}
		});
		item = makeMenuItem("Lay Angle", "LayAngle", null);
		drawMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPLayAngleDialog(parent, "Lay Angle", true);
			}
		});
		drawMenu.addSeparator();
		item = makeMenuItem("Erase Line", null, null);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new GPEraseLineDialog(parent, "Erase Line", true);
			}
		});
		drawMenu.add(item);
		item = makeMenuItem("Erase Selection", "Erase", null);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showConfirmDialog(parent,
						"Erase all selected drawings in Figure 1?",
						"Geometria", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
			}
		});
		drawMenu.add(item);
		item = makeMenuItem("Erase All", null, null);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane
						.showConfirmDialog(parent,
								"Erase all drawings in Figure 1?", "Geometria",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
			}
		});
		drawMenu.add(item);
		menuBar.add(drawMenu);
		// View menu
		JMenu viewMenu = new JMenu("View");
		item = makeMenuItem("Zoom In", "ZoomIn", KeyStroke.getKeyStroke(
				KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
		viewMenu.add(item);
		item = makeMenuItem("Zoom Out", "ZoomOut", KeyStroke.getKeyStroke(
				KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
		viewMenu.add(item);
		item = makeMenuItem("Best Fit", "FitToView", KeyStroke.getKeyStroke(
				KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
		viewMenu.add(item);
		viewMenu.addSeparator();
		item = makeMenuItem("Initial Attitude", null, KeyStroke.getKeyStroke(
				KeyEvent.VK_HOME, InputEvent.CTRL_MASK));
		viewMenu.add(item);
		item = makeMenuItem("Default Attitude", null, null);
		viewMenu.add(item);
		viewMenu.addSeparator();
		item = new JCheckBoxMenuItem("Labels", true);
		viewMenu.add(item);
		item = new JCheckBoxMenuItem("Transparent", true);
		viewMenu.add(item);
		item = makeMenuItem("Color", "Color", null);
		viewMenu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JColorChooser.showDialog(parent, "Geometria", Color.red);
			}
		});
		item = new JCheckBoxMenuItem("XY Grid", true);
		viewMenu.add(item);
		menuBar.add(viewMenu);
		// Help menu
		JMenu helpMenu = new JMenu("Help");
		item = makeMenuItem("Contents", "Contents", KeyStroke.getKeyStroke(
				KeyEvent.VK_F1, 0));
		helpMenu.add(item);
		item = makeMenuItem("Index", null, null);
		helpMenu.add(item);
		item = makeMenuItem("Search", null, null);
		helpMenu.add(item);
		item = makeMenuItem("About", "About", null);
		helpMenu.add(item);
		menuBar.add(helpMenu);
	}

	private JButton makeButton(String icon) {
		JButton button = new JButton();
		if (icon != null) {
			logger.info("icon=" + icon);
			button.setIcon(new ImageIcon(getClass().getResource(
					"/images/24x24/" + icon + ".png")));
		} else
			button.setIcon(new ImageIcon(getClass().getResource(
					"/images/24x24/Empty.png")));
		return button;
	}

	private void layoutToolBar() {
		JPanel upperToolPane = new JPanel();
		upperToolPane.setLayout(new BoxLayout(upperToolPane, BoxLayout.X_AXIS));
		JToolBar fileToolBar = new JToolBar();
		fileToolBar.setLayout(new BoxLayout(fileToolBar, BoxLayout.X_AXIS));
		JButton newProblemButton = makeButton("NewProblem");
		fileToolBar.add(newProblemButton);
		JButton newSolutionButton = makeButton("NewSolution");
		fileToolBar.add(newSolutionButton);
		JButton openProblemButton = makeButton("OpenProblem");
		fileToolBar.add(openProblemButton);
		JButton openSolutionButton = makeButton("OpenSolution");
		fileToolBar.add(openSolutionButton);
		JButton saveButton = makeButton("SaveDocument");
		fileToolBar.add(saveButton);
		JButton closeButton = makeButton("CloseDocument");
		fileToolBar.add(closeButton);
		JButton answerButton = makeButton("Info");
		fileToolBar.add(answerButton);
		upperToolPane.add(fileToolBar);
		upperToolPane.add(Box.createRigidArea(new Dimension(2, 2)));
		JToolBar measureToolBar = new JToolBar();
		measureToolBar
				.setLayout(new BoxLayout(measureToolBar, BoxLayout.X_AXIS));
		JButton distanceButton = makeButton("MeasureDistance");
		measureToolBar.add(distanceButton);
		JButton angleButton = makeButton("MeasureAngle");
		measureToolBar.add(angleButton);
		JButton areaButton = makeButton("Area");
		measureToolBar.add(areaButton);
		JButton volumeButton = makeButton("Volume");
		measureToolBar.add(volumeButton);
		upperToolPane.add(measureToolBar);
		upperToolPane.add(Box.createRigidArea(new Dimension(2, 2)));
		JToolBar drawToolBar = new JToolBar();
		drawToolBar.setLayout(new BoxLayout(drawToolBar, BoxLayout.X_AXIS));
		JButton drawLineButton = makeButton("DrawLine");
		drawToolBar.add(drawLineButton);
		JButton perpendicularButton = makeButton("Perpendicular");
		drawToolBar.add(perpendicularButton);
		JButton divideLineButton = makeButton("DivideLine");
		drawToolBar.add(divideLineButton);
		JButton divideAngleButton = makeButton("DivideAngle");
		drawToolBar.add(divideAngleButton);
		JButton intersectButton = makeButton("Intersect");
		drawToolBar.add(intersectButton);
		JButton layDistanceButton = makeButton("LayDistance");
		drawToolBar.add(layDistanceButton);
		JButton layAngleButton = makeButton("LayAngle");
		drawToolBar.add(layAngleButton);
		JButton eraseButton = makeButton("Erase");
		drawToolBar.add(eraseButton);
		upperToolPane.add(drawToolBar);
		upperToolPane.add(Box.createRigidArea(new Dimension(2, 2)));
		JToolBar viewToolBar = new JToolBar();
		viewToolBar.setLayout(new BoxLayout(viewToolBar, BoxLayout.X_AXIS));
		JButton zoomInButton = makeButton("ZoomIn");
		viewToolBar.add(zoomInButton);
		JButton zoomOutButton = makeButton("ZoomOut");
		viewToolBar.add(zoomOutButton);
		JButton fitToViewButton = makeButton("FitToView");
		viewToolBar.add(fitToViewButton);
		JButton colorButton = makeButton("Color");
		viewToolBar.add(colorButton);
		upperToolPane.add(viewToolBar);
		upperToolPane.add(Box.createHorizontalGlue());
		add(upperToolPane);
		JPanel lowerToolPane = new JPanel();
		lowerToolPane.setLayout(new BoxLayout(lowerToolPane, BoxLayout.X_AXIS));
		JToolBar editToolBar = new JToolBar();
		editToolBar.setLayout(new BoxLayout(editToolBar, BoxLayout.X_AXIS));
		JButton undoButton = makeButton("Undo");
		editToolBar.add(undoButton);
		JButton undoHistoryButton = makeButton("UndoHistory");
		editToolBar.add(undoHistoryButton);
		JButton cutButton = makeButton("Cut");
		editToolBar.add(cutButton);
		JButton joinButton = makeButton("Join");
		editToolBar.add(joinButton);
		JButton addVertexButton = makeButton("AddVertex");
		editToolBar.add(addVertexButton);
		lowerToolPane.add(editToolBar);
		JToolBar transformToolBar = new JToolBar();
		transformToolBar.setLayout(new BoxLayout(transformToolBar,
				BoxLayout.X_AXIS));
		JButton scaleButton = makeButton("Scale");
		transformToolBar.add(scaleButton);
		JButton shearButton = makeButton("Shear");
		transformToolBar.add(shearButton);
		lowerToolPane.add(transformToolBar);
		JToolBar solidToolBar = new JToolBar();
		solidToolBar.setLayout(new BoxLayout(solidToolBar, BoxLayout.X_AXIS));
		JButton newSolidButton = makeButton("NewFigure");
		solidToolBar.add(newSolidButton);
		JButton openSolidButton = makeButton("OpenFigure");
		solidToolBar.add(openSolidButton);
		JButton saveSolidButton = makeButton("SaveFigure");
		solidToolBar.add(saveSolidButton);
		JButton closeSolidButton = makeButton("RemoveFigure");
		solidToolBar.add(closeSolidButton);
		lowerToolPane.add(solidToolBar);
		JToolBar helpToolBar = new JToolBar();
		helpToolBar.setLayout(new BoxLayout(helpToolBar, BoxLayout.X_AXIS));
		// JButton helpButton = makeButton("Help");
		// helpToolBar.add(helpButton);
		JButton aboutButton = makeButton("About");
		helpToolBar.add(aboutButton);
		lowerToolPane.add(helpToolBar);
		lowerToolPane.add(Box.createHorizontalGlue());
		add(lowerToolPane);
	}

	private void makeSolutionLog(JList solutionLog) {
		DefaultListModel model = new DefaultListModel();
		solutionLog.setModel(model);
		model.addElement("Solid 1: Measure |AB| = edge");
	}

	private void makeTextPane(JTextPane textPane) {
		StyleContext styleContext = new StyleContext();
		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);
		int size = 14;
		StyleConstants.setFontSize(defaultStyle, size);
		styleContext.addStyle("default", null);
		Style parameterStyle = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontSize(parameterStyle, size);
		StyleConstants.setForeground(parameterStyle, Color.blue);
		StyleConstants.setBold(parameterStyle, true);
		styleContext.addStyle("parameter", defaultStyle);
		StyledDocument text = new DefaultStyledDocument(styleContext);
		try {
			text.insertString(text.getLength(),
					"This parallelepiped is carved into a cylinder"
							+ " whose basis diameter is ", text
							.getStyle("default"));
			text
					.insertString(text.getLength(), "k", text
							.getStyle("parameter"));
			text.insertString(text.getLength(),
					" times the parallelepiped's shortest edge."
							+ " Find the maximum volume of the cylinder.", text
							.getStyle("default"));
		} catch (Exception exception) {
			System.err.println(exception.getStackTrace());
		}
		textPane.setStyledDocument(text);
	}

	private void makeCalculator(JTextField calcTextField) {
		String text = "radius = edge * k / 2";
		calcTextField.setText(text);
	}

	private void makeNotepad(JList notationList) {
		DefaultListModel model = new DefaultListModel();
		notationList.setModel(model);
		model.addElement("radius = edge * k / 2");
	}

	private static final long serialVersionUID = 1L;
}
