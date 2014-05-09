/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.event.ChangeListener;

import net.geocentral.geometria.view.GFigurePane;
import net.geocentral.geometria.view.GFigurePopupMenu;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GFigure implements Cloneable {

	public static final Color DEFAULT_COLOR = Color.CYAN.brighter();

	public static final double ZOOM_FACTOR = Math.sqrt(2);

	public static final double MARGIN_FACTOR = 1.2;

	private String name;

	private boolean transparent = true;

	private boolean labelled = true;

	private GSolid solid;

	private GCamera camera;

	private Color baseColor = DEFAULT_COLOR;

	private double scalingFactor = -1; // Number of pixels in an absolute unit

	private double initZoomFactor = 1;
	
	private boolean selected;

	private boolean painting;

	private GFigurePane pane;

	private static Logger logger = Logger.getLogger("net.geocentral.geometria");

	public GFigure() {
		camera = new GCamera();
	}

	public GFigure(GSolid solid) {
	    this();
	    this.solid = solid;
	}

	public GFigure(GSolid solid, double initZoomFactor) {
	    this(solid);
	    this.initZoomFactor = initZoomFactor;
	}

	public GFigure clone() {
		logger.info(this);
		GFigure figure = new GFigure();
		figure.name = name;
		figure.transparent = transparent;
		figure.labelled = labelled;
		figure.solid = solid.clone();
		figure.camera = camera.clone();
		figure.baseColor = new Color(baseColor.getRGB());
		figure.scalingFactor = -1;
		return figure;
	}

	public void make(Element node) throws Exception {
		logger.info("");
		makeName(node);
		makeColor(node);
		makeTransparent(node);
		makeLabelled(node);
		makeSolid(node);
		makeCamera(node);
	}

	private void makeName(Element node) throws Exception {
		logger.info("");
		name = node.getElementsByTagName("name").item(0).getTextContent();
	}

	private void makeColor(Element node) throws Exception {
		logger.info("");
		NodeList ns = node.getElementsByTagName("color");
		if (ns.getLength() > 0)
			baseColor = Color.decode(ns.item(0).getTextContent());
	}

	private void makeTransparent(Element node) throws Exception {
		logger.info("");
		NodeList ns = node.getElementsByTagName("transparent");
		if (ns.getLength() > 0)
			transparent = Boolean.parseBoolean(ns.item(0).getTextContent());
	}

	private void makeLabelled(Element node) throws Exception {
		logger.info("");
		NodeList ns = node.getElementsByTagName("labelled");
		if (ns.getLength() > 0) {
			labelled = Boolean.parseBoolean(ns.item(0).getTextContent());
		}
	}

	private void makeSolid(Element node) throws Exception {
		logger.info("");
		Element n = (Element)node.getElementsByTagName("solid").item(0);
		solid = new GSolid();
		solid.make(n);
	}

	private void makeCamera(Element node) throws Exception {
		logger.info("");
		Element n = (Element)node.getElementsByTagName("camera").item(0);
		camera.make(n);
	}

	public void serialize(StringBuffer buf) {
		logger.info("");
		buf.append("\n<figure>");
		buf.append("\n<name>");
		buf.append(name);
		buf.append("</name>");
		buf.append("\n<color>");
		buf.append("#"
				+ getHexBaseColor());
		buf.append("</color>");
		buf.append("\n<transparent>");
		buf.append(String.valueOf(transparent));
		buf.append("</transparent>");
		buf.append("\n<labelled>");
		buf.append(String.valueOf(labelled));
		buf.append("</labelled>");
		solid.serialize(buf);
		camera.serialize(buf);
		buf.append("\n</figure>");
	}

	private void setPreferredSize() {
		logger.info("");
		int span = 2 * (int)(scalingFactor
				* solid.getBoundingSphere().getRadius() * MARGIN_FACTOR);
		pane.setPreferredSize(new Dimension(span, span));
		logger.info(span);
	}

	private void scalingFactorToView() {
		logger.info("");
		double radius = solid.getBoundingSphere().getRadius();
		// Recalculate scaling factor
		Dimension viewSize = pane.getViewSize();
		scalingFactor = 0.5 * Math.min(viewSize.width / radius, viewSize.height
				/ radius) / MARGIN_FACTOR;
		logger.info(scalingFactor);
	}

	public void cameraTurned() {
		if (!painting)
			repaint();
	}

	public void zoomIn() {
		logger.info("");
		zoom(ZOOM_FACTOR);
		logger.info(scalingFactor);
	}

	public void zoomOut() {
		logger.info("");
		zoom(1 / ZOOM_FACTOR);
		logger.info(scalingFactor);
	}

	private void zoom(double factor) {
		ChangeListener[] listeners = pane.removeChangeListeners();
		pane.repositionViewport(factor);
		scalingFactor *= factor;
		setPreferredSize();
		pane.addChangeListeners(listeners);
		pane.revalidate();
	}

	public void fitToView() {
		logger.info("");
		scalingFactorToView();
		setPreferredSize();
		pane.revalidate();
		repaint();
	}

	public void toggleTransparency() {
		transparent ^= true;
		logger.info(transparent);
		repaint();
	}

	public void toggleLabels() {
		labelled ^= true;
		logger.info(labelled);
		repaint();
	}

	public void setBaseColor(Color baseColor) {
		logger.info(baseColor);
		this.baseColor = baseColor;
		repaint();
	}

	public void initialAttitude() {
		logger.info("");
		camera.seize();
		camera.toInitialAttitude();
		repaint();
	}

	public void defaultAttitude() {
		logger.info("");
		camera.seize();
		camera.toDefaultAttitude();
		repaint();
	}

	public void print() throws Exception {
	    logger.info("");
	    PrinterJob job = PrinterJob.getPrinterJob();
	    Printable printer = new Printer();
	    job.setPrintable(printer);
	    if (job.printDialog()) {
	        try {
	            job.print();
	            logger.info(name);
	        }
	        catch (Exception exception) {
	            logger.error(exception.getMessage());
	            throw new Exception();
	        }
	    }
	}

	public void paint(Graphics2D g2d) {
		painting = true;
		if (scalingFactor < 0) {
			scalingFactorToView();
			zoom(initZoomFactor);
		}
		if (transparent)
			solid.paintTransparent(g2d, camera, scalingFactor, pane.getSize(),
					labelled);
		else
			solid.paintOpaque(g2d, camera, scalingFactor, pane.getSize(),
					baseColor, labelled);
		painting = false;
	}

	public void repaint() {
		if (pane != null)
			pane.repaint();
	}

	public void setSelected(boolean selected) {
		logger.info(this + ", " + selected);
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GSolid getSolid() {
		return solid;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public boolean isLabelled() {
		return labelled;
	}

	public void setLabelled(boolean labelled) {
		this.labelled = labelled;
	}

	public Color getBaseColor() {
		return baseColor;
	}

	private String getHexBaseColor() {
		return Integer.toHexString((baseColor.getRed() * 256 + baseColor
				.getGreen()) * 256 + baseColor.getBlue());
	}

	public void select(MouseEvent e) {
		double xp = (e.getX() - 0.5 * pane.getWidth()) / scalingFactor;
		double yp = (0.5 * pane.getHeight() - e.getY()) / scalingFactor;
		solid.select(xp, yp, scalingFactor, e.isControlDown(), transparent,
				camera);
		repaint();
	}

	public void popupMenu(int x, int y) {
		GFigurePopupMenu popup = new GFigurePopupMenu(this);
		popup.show(pane, x, y);
	}

    public BufferedImage exportImage() {
        int width = pane.getWidth();
        int height = pane.getHeight();
        BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        pane.paint(g2);
        return image;
    }

	public void setPane(GFigurePane pane) {
		this.pane = pane;

	}

	public GCamera getCamera() {
		return camera;
	}

	public void setCamera(GCamera camera) {
		this.camera = camera;
	}

	public String toString() {
		return name;
	}

	class Printer implements Printable {
	    public int print(Graphics g, PageFormat pageFormat,
	            int pageIndex) throws PrinterException {
	        if (pageIndex > 0)
	            /* We have only one page, and 'page' is zero-based */
	            return NO_SUCH_PAGE;
	       /* User (0,0) is typically outside the imageable area, so we must
	        * translate by the X and Y values in the PageFormat
	        *  to avoid clipping
	        */
	       ((Graphics2D)g).translate(pageFormat.getImageableX(),
	               pageFormat.getImageableY());
	       /* Now we perform our rendering */
	       pane.printAll(g);
	       return PAGE_EXISTS;
    }
}

}
