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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

public class GPFrame extends JFrame {

	public static final Point G_FRAMELOCATION = new Point(50, 0);

	public GPFrame() {
		setDefaultFont();
		setLocation(G_FRAMELOCATION);
		setTitle("Geometria");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		GPMainPanel mainPane = new GPMainPanel(this);
		getContentPane().add(mainPane, BorderLayout.CENTER);
		pack();
	}

	private static void setDefaultFont() {
		Enumeration<?> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, new Font("SansSerif", Font.PLAIN, 14));
		}
	}

	public static void main(String[] args) {
		final GPFrame frame = new GPFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}

	private static final long serialVersionUID = 1L;
}
