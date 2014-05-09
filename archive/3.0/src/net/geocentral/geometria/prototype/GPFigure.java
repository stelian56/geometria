/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.prototype;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JPanel;

public class GPFigure extends JPanel {

	private final Point[] points = { new Point(60, 160), new Point(60, 360),
			new Point(440, 360), new Point(440, 160), new Point(160, 60),
			new Point(160, 260), new Point(540, 260), new Point(540, 60) };

	public GPFigure() {
		setBackground(Color.white);
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		g2d.setColor(Color.black);
		float[] dash = { 12, 8 };
		Stroke solidStroke = new BasicStroke(1);
		Stroke dashStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_ROUND, 1, dash, 0);

		g2d.setStroke(solidStroke);
		drawLine(g2d, 0, 1);
		drawLine(g2d, 1, 2);
		drawLine(g2d, 2, 3);
		drawLine(g2d, 3, 0);

		g2d.setStroke(dashStroke);
		drawLine(g2d, 4, 5);
		drawLine(g2d, 5, 6);
		g2d.setStroke(solidStroke);
		drawLine(g2d, 6, 7);
		drawLine(g2d, 7, 4);

		drawLine(g2d, 0, 4);
		g2d.setStroke(dashStroke);
		drawLine(g2d, 1, 5);
		g2d.setStroke(solidStroke);
		drawLine(g2d, 2, 6);
		drawLine(g2d, 3, 7);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		char label = 'A';
		for (Point p : points) {
			int diameter = 6;
			g2d.setColor(Color.red);
			g2d.fillOval(p.x - diameter / 2, p.y - diameter / 2, diameter,
					diameter);
			g2d.setColor(Color.black);
			g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
			g2d.drawString(String.valueOf(label++), p.x - 14, p.y - 8);
		}
	}

	private void drawLine(Graphics2D g2d, int fromIndex, int toIndex) {
		g2d.drawLine(points[fromIndex].x, points[fromIndex].y,
				points[toIndex].x, points[toIndex].y);
	}

	private static final long serialVersionUID = 1L;
}
