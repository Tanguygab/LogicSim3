package io.github.tanguygab.logicsim3.gates;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JOptionPane;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.Lang;

/**
 * ON-Delay component for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class OnDelay extends Gate implements Runnable {
	static final long serialVersionUID = -2350098633141393951L;

	static final String DELAY = "delay";
	static final String DELAY_DEFAULT = "500";

	private Thread thread;
	int delayTime = 1000;

	public OnDelay() {
		super("input");
		type = "ondelay";
		createInputs(1);
		createOutputs(1);
		loadProperties();
	}

	@Override
	public void loadProperties() {
		delayTime = Integer.parseInt(getPropertyWithDefault(DELAY, DELAY_DEFAULT));
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		if (e.source.equals(getPin(0))) {
			if (e.level == HIGH) {
				// rising edge detection of input
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			} else {
				// low let through
				LSLevelEvent evt = new LSLevelEvent(this, LOW);
				getPin(1).changedLevel(evt);
			}
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delayTime);
		} catch (InterruptedException e) {
		}
		LSLevelEvent evt = new LSLevelEvent(this, HIGH);
		getPin(1).changedLevel(evt);
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		super.showPropertiesUI(frame);
		String h = (String) JOptionPane.showInputDialog(frame, I18N.getString(type, DELAY), I18N.tr(Lang.SETTINGS),
				JOptionPane.QUESTION_MESSAGE, null, null, Integer.toString((int) delayTime));
		if (h != null && h.length() > 0) {
			delayTime = Integer.parseInt(h);
			setPropertyInt(DELAY, delayTime);
		}
		return true;
	}

	@Override
	protected void drawRotated(Graphics2D g2) {
		int cd = 15;
		g2.drawOval(xc - cd / 2, yc - cd / 2 + 5, cd, cd);
		g2.drawLine(getX() + cd, getY() + cd, getX() + width - cd, getY() + cd);
		g2.drawString("0", getX() + cd, getY() + cd + 12);
		g2.drawString("1", getX() + width - cd - 6, getY() + cd + 12);
		Path2D ptr = new Path2D.Double();
		ptr.moveTo(xc, yc);
		ptr.lineTo(xc, yc + 5);
		ptr.lineTo(xc + 3, yc + 5);
		g2.draw(ptr);
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "On Delay");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Delays the signal when turning to HIGH");
		I18N.addGate(I18N.ALL, type, DELAY, "Time of delay (ms)");

		I18N.addGate("de", type, I18N.TITLE, "Einschaltverzögerung");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Verzögert das High-Signal");
		I18N.addGate("de", type, DELAY, "Verzögerungszeit (ms)");

		I18N.addGate("es", type, I18N.TITLE, "Retardo de 0 a 1");
		I18N.addGate("es", type, DELAY, "Introduce tiempo de retardo (ms)");

		I18N.addGate("fr", type, I18N.TITLE, "Temporisation travail");
		I18N.addGate("fr", type, DELAY, "Décalage (ms)");
	}
}