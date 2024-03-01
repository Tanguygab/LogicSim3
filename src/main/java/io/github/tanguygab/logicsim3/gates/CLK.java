package io.github.tanguygab.logicsim3.gates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Date;

import javax.swing.JOptionPane;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.LSMouseEvent;
import io.github.tanguygab.logicsim3.Lang;
import io.github.tanguygab.logicsim3.parts.Pin;
import io.github.tanguygab.logicsim3.Simulation;
import io.github.tanguygab.logicsim3.WidgetHelper;

/**
 * Clock Generator for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class CLK extends Gate implements Runnable {
	static final long serialVersionUID = 3971572931629721831L;

	private static final String ENTERLOW = "enterlow";
	private static final String ENTERHIGH = "enterhigh";

	static final int PAUSE = 0;
	static final int RUNNING = 1;
	static final int MANUAL = 2;

	static final String HT = "hightime";
	static final String LT = "lowtime";

	static final String HT_DEFAULT = "500";
	static final String LT_DEFAULT = "500";

	private Thread thread;
	private boolean running = false;

	Rectangle auto = new Rectangle(39, 53, 30, 15);
	Rectangle manual = new Rectangle(11, 53, 15, 15);
	Rectangle oszi = new Rectangle(11, 20, 59, 30);

	boolean[] osz = new boolean[oszi.width + 1];

	int currentMode = PAUSE;
	long lastTime;
	int highTime = 500;
	int lowTime = 500;
	int pos = 0;

	private static final int OUT = 0;
	private static final int NOUT = 1;
	private static final int HLT = 2;

	public CLK() {
		super("input");
		type = "clock";
		width = 80;
		height = 80;
		createOutputs(2);
		createInputs(1);
		loadProperties();
		getPin(1).setLevelType(Pin.INVERTED);
		getPin(HLT).setProperty(TEXT, "H");
	}

	@Override
	public void reset() {
		getPin(OUT).changedLevel(new LSLevelEvent(this, false, true));
		getPin(NOUT).changedLevel(new LSLevelEvent(this, true, true));
	}

	@Override
	public void loadProperties() {
		highTime = Integer.parseInt(getPropertyWithDefault(HT, HT_DEFAULT));
		lowTime = Integer.parseInt(getPropertyWithDefault(LT, LT_DEFAULT));
	}

	@Override
	public void interact() {
		if (currentMode == RUNNING) {
			if (running && !Simulation.getInstance().isRunning())
				running = false;
			currentMode = PAUSE;
		} else {
			currentMode = RUNNING;
			if (!running)
				startClock();
		}
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		if (e.source.equals(getPin(HLT))) {
			if (e.level == HIGH) {
				currentMode = PAUSE;
			} else {
				currentMode = RUNNING;
			}
		}
	}

	@Override
	public void mousePressedSim(LSMouseEvent e) {
		super.mousePressedSim(e);

		int dx = e.getX() - getX();
		int dy = e.getY() - getY();

		if (manual.contains(dx, dy) && currentMode != RUNNING) {
			getPin(OUT).changedLevel(new LSLevelEvent(this, !getPin(OUT).getLevel()));
			getPin(NOUT).changedLevel(new LSLevelEvent(this, getPin(OUT).getLevel()));
			lastTime = 0;
		} else if (auto.contains(dx, dy)) {
			interact();
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		int x = getX();
		int y = getY();

		if (Simulation.getInstance().isRunning() && !this.running)
			startClock();

		if (!Simulation.getInstance().isRunning())
			running = false;
		
		g2.setPaint(Color.black);
		g2.setFont(Pin.smallFont);
		String s = "CLK";
		int sw = g2.getFontMetrics().stringWidth(s);
		g2.drawString(s, x + getWidth() / 2 - sw / 2, y + 18);

		g2.setStroke(new BasicStroke(1));
		WidgetHelper.drawPushSwitch(g2, x + manual.x, y + manual.y, manual.width,
				getPin(0).getLevel() ? Color.red : Color.LIGHT_GRAY, null);
		Rectangle r = new Rectangle(auto.x + getX(), auto.y + getY(), auto.width, auto.height);
		WidgetHelper.drawSwitchHorizontal(g2, r, currentMode == RUNNING, Color.RED, Color.LIGHT_GRAY);

		// oszi
		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(x + oszi.x, y + oszi.y, oszi.width, oszi.height);
		// oszi line
		g2.setPaint(Color.green);
		g2.setStroke(new BasicStroke(1));
		boolean level1 = osz[0];
		boolean level2;
		int offset = 6;
		for (int i = 1; i < pos; i++) {
			level2 = osz[i];

			g2.drawLine(x + oszi.x + i, y + oszi.y + offset + (oszi.height - 2 * offset) * (level1 ? 0 : 1),
					x + oszi.x + i, y + oszi.y + offset + (oszi.height - 2 * offset) * (level2 ? 0 : 1));
			level1 = level2;
		}
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		super.showPropertiesUI(frame);
		String h = (String) JOptionPane.showInputDialog(frame, I18N.getString(type, ENTERHIGH), I18N.tr(Lang.SETTINGS),
				JOptionPane.QUESTION_MESSAGE, null, null, Integer.toString((int) highTime));
		if (h != null && h.length() > 0) {
			highTime = Integer.parseInt(h);
			setPropertyInt(HT, highTime);
		}
		h = (String) JOptionPane.showInputDialog(frame, I18N.getString(type, ENTERLOW), I18N.tr(Lang.SETTINGS),
				JOptionPane.QUESTION_MESSAGE, null, null, Integer.toString((int) lowTime));
		if (h != null && h.length() > 0) {
			lowTime = Integer.parseInt(h);
			setPropertyInt(LT, lowTime);
		}
		return true;
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Clock");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION,
				"Clock with configurable low and high time - press SPACE to start the Clock");
		I18N.addGate(I18N.ALL, type, ENTERHIGH, "Time High-Level (ms)");
		I18N.addGate(I18N.ALL, type, ENTERLOW, "Time Low-Level (ms)");

		I18N.addGate("de", type, I18N.TITLE, "Taktgeber");
		I18N.addGate("de", type, I18N.DESCRIPTION,
				"Taktgeber (Clock) mit einstellbaren LOW- und HIGH-Zeiten - Leertaste drücken zum Starten");
		I18N.addGate("de", type, ENTERHIGH, "Dauer High-Pegel (ms)");
		I18N.addGate("de", type, ENTERLOW, "Dauer Low-Pegel (ms)");

		I18N.addGate("es", type, I18N.TITLE, "Reloj");

		I18N.addGate("fr", type, I18N.TITLE, "Horloge");
		I18N.addGate("fr", type, ENTERHIGH, "Durée du palier haut (ms)");
		I18N.addGate("fr", type, ENTERLOW, "Durée du palier bas (ms)");
	}

	public void startClock() {
		thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public void run() {
		running = true;
		int ms = 10 * highTime / oszi.width;
		Date temp = new Date();
		long lastMS = new Date().getTime();
		lastTime = new Date().getTime();
		while (running) {
			// advance oszilloscope's position
			if ((temp = new Date()).getTime() - lastMS > ms) {
				pos++;
				lastMS = temp.getTime();
				fireRepaint();
			}

			// reset data array
			if (pos > 59) {
				pos = 0;
				osz = new boolean[oszi.width + 1];
			}

			Pin cout = getPin(0);
			boolean out = cout.getLevel();
			if (currentMode == RUNNING) {
				if (!out && new Date().getTime() - lastTime > lowTime) {
					getPin(1).changedLevel(new LSLevelEvent(this, HIGH));
					cout.changedLevel(new LSLevelEvent(this, HIGH));
					lastTime = new Date().getTime();
				} else if (out && new Date().getTime() - lastTime > highTime) {
					getPin(1).changedLevel(new LSLevelEvent(this, LOW));
					cout.changedLevel(new LSLevelEvent(this, LOW));
					lastTime = new Date().getTime();
				}
			} else if (currentMode == MANUAL) {
				// if (out && new Date().getTime() - lastTime > highTime) {
				// cout.changedLevel(new LSLevelEvent(this, LOW));
				// currentMode = PAUSE;
				// }
			}
			try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
			}
			osz[pos] = cout.getLevel();
		}
	}

}