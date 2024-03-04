package io.github.tanguygab.logicsim3.gui;

import io.github.tanguygab.logicsim3.App;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class LSFrame_AboutBox extends JWindow {

	private static final long serialVersionUID = -3193728228853983319L;
	private final Image imgSplash;

    public LSFrame_AboutBox(Frame parent) {
		super(parent);

		imgSplash = new ImageIcon(App.getInstance().getResource("images/about.jpg")).getImage();
		int imgWidth = imgSplash.getWidth(this);
		int imgHeight = imgSplash.getHeight(this) + 155;
		Dimension dim = parent.getSize();
		Point loc = parent.getLocation();

		setLocation(loc.x + dim.width / 2 - imgWidth / 2, loc.y + dim.height / 2 - imgHeight / 2);
		setSize(imgWidth, imgHeight);
		getContentPane().setLayout(new BorderLayout(0, 0));
        SplashPanel splashPanel = new SplashPanel();
        getContentPane().add(splashPanel, "Center");
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);

		setVisible(true);
	}

	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			setVisible(false);
			dispose();
		}
	}

private class SplashPanel extends JPanel {

		private static final long serialVersionUID = 5564588819196489014L;

		public void paint(Graphics g) {
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(imgSplash, 0, 0, this);

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setColor(Color.white);

			FontMetrics fm = g.getFontMetrics();
			Font of = fm.getFont();
			g.setFont(new Font(of.getName(), of.getStyle(), 12));

			String version = App.class.getPackage().getImplementationVersion();
			g.drawString("Version " + version, 10, 240);
			g.drawString("Programmed 2020 by Peter Gabriel - http://sis.schule", 10, 260);
			g.drawString("Based on LogicSim 2.4 (2009) by Andreas Tetzl - http://tetzl.de", 10, 290);
			g.drawString("About Graphic by Jens Borsdorf, http://jens.borsdorf.name", 10, 310);
			g.drawString("Contributions by Matthew Lister (chocolatepatty@github)", 10, 330);
			g.drawString("Contributions by Nicolas Neveu (nneveu@gmail.com)", 10, 345);
			g.drawString("LogicSim is free software-Released under the GPL-Download on Github", 10, 360);
		}
	}

}