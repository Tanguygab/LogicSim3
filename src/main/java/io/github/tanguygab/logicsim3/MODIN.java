package io.github.tanguygab.logicsim3;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

/**
 * input gate for modules
 * 
 * will be created during module creation. connect inputs to define module
 * inputs.
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class MODIN extends io.github.tanguygab.logicsim3.Gate {
	static final long serialVersionUID = -2338870902247206767L;
	private int pincount;
	private static final String PINCOUNT = "pincount";
	private static final int PINCOUNT_DEFAULT = 16;

	public MODIN() {
		super();
		type = "modin";
		label = "INPUTS";
		height = 170;
		backgroundColor = Color.LIGHT_GRAY;
		createInputs(16);
		createOutputs(16);
	}

	@Override
	protected void drawLabel(Graphics2D g2, String lbl, Font font) {
		g2.setFont(bigFont);
		io.github.tanguygab.logicsim3.WidgetHelper.drawStringRotated(g2, label, xc, yc, WidgetHelper.ALIGN_CENTER,
				-90);
	}

	@Override
	public void changedLevel(io.github.tanguygab.logicsim3.LSLevelEvent e) {
		io.github.tanguygab.logicsim3.Pin p = (io.github.tanguygab.logicsim3.Pin) e.source;
		// forward event to the appropriate output
		int target = p.number + getNumInputs();
		io.github.tanguygab.logicsim3.LSLevelEvent evt = new LSLevelEvent(this, p.getLevel());
		getPin(target).changedLevel(evt);
	}

	@Override
	public void rotate() {
		// don't rotate
	}

	@Override
	public void mirror() {
		// don't mirror
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		int numberOfInputs = getInputs().size();
		g2.setColor(Color.GREEN);
		// draw click areas if pin is connected
		for (io.github.tanguygab.logicsim3.Pin p : getInputs()) {
			io.github.tanguygab.logicsim3.Pin pout = getPin(p.number + numberOfInputs);
			if (pout.isConnected() && p.getProperty(TEXT) == null)
				g2.fill(new Rectangle(getX() + CONN_SIZE + 1, p.getY() - 4, 8, 8));
		}
	}

	@Override
	protected void loadProperties() {
		pincount = getPropertyIntWithDefault(PINCOUNT, PINCOUNT_DEFAULT);
		int inputcount = getInputs().size();
		if (pincount != inputcount) {
			if (pincount < inputcount) {
				// disconnect wires
				for (int i = inputcount - 1; i >= pincount; i--) {
					// disconnect all parts from input and output pin
					getPin(i + inputcount).disconnect();
					getPin(i).disconnect();
				}
				for (int i = inputcount - 1; i >= pincount; i--) {
					pins.remove(i + inputcount);
				}
				for (int i = inputcount - 1; i >= pincount; i--) {
					pins.remove(i);
				}
				for (int i = 0; i < pincount; i++) {
					io.github.tanguygab.logicsim3.Pin p = pins.get(pincount + i);
					p.number = pincount + i;
				}
			} else {
				// pincount is greater than it is now
				for (int i = inputcount; i < pincount; i++) {
					io.github.tanguygab.logicsim3.Pin p = new io.github.tanguygab.logicsim3.Pin(getX(), 0, this, i);
					p.paintDirection = io.github.tanguygab.logicsim3.Pin.RIGHT;
					p.setIoType(io.github.tanguygab.logicsim3.Pin.INPUT);
					pins.insertElementAt(p, i);
				}
				for (int i = pincount; i < pins.size(); i++) {
					io.github.tanguygab.logicsim3.Pin p = pins.get(i);
					p.number = i;
				}
				for (int i = inputcount + pincount; i < pincount * 2; i++) {
					io.github.tanguygab.logicsim3.Pin p = new io.github.tanguygab.logicsim3.Pin(getX() + width, 0, this, i);
					p.paintDirection = io.github.tanguygab.logicsim3.Pin.LEFT;
					p.setIoType(io.github.tanguygab.logicsim3.Pin.OUTPUT);
					pins.insertElementAt(p, i);
				}
			}
			// adjust height
			if (pincount < 6)
				height = 60;
			else
				height = pincount * 10 + 10;
			// reposition all pins (only y)
			int numIn = getNumInputs();
			int numOut = getNumOutputs();
			for (io.github.tanguygab.logicsim3.Pin c : getInputs()) {
				c.setY(getY() + getConnectorPosition(c.number, numIn, io.github.tanguygab.logicsim3.Gate.VERTICAL));
			}
			for (Pin c : getOutputs()) {
				c.setY(getY() + getConnectorPosition(c.number - numIn, numOut, Gate.VERTICAL));
			}
		}
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		super.showPropertiesUI(frame);
		String h = (String) JOptionPane.showInputDialog(frame, io.github.tanguygab.logicsim3.I18N.getString(type, PINCOUNT), io.github.tanguygab.logicsim3.I18N.tr(Lang.SETTINGS),
				JOptionPane.QUESTION_MESSAGE, null, null, Integer.toString(pincount));
		if (h != null && h.length() > 0) {
			pincount = Integer.parseInt(h);
			setPropertyInt(PINCOUNT, pincount);
			loadProperties();
		}
		return true;
	}

	@Override
	public void loadLanguage() {
		io.github.tanguygab.logicsim3.I18N.addGate(io.github.tanguygab.logicsim3.I18N.ALL, type, io.github.tanguygab.logicsim3.I18N.TITLE, "Inputs");
		io.github.tanguygab.logicsim3.I18N.addGate(io.github.tanguygab.logicsim3.I18N.ALL, type, io.github.tanguygab.logicsim3.I18N.DESCRIPTION,
				"Input Gate for Modules - click label area to set an input pin's label");
		io.github.tanguygab.logicsim3.I18N.addGate(io.github.tanguygab.logicsim3.I18N.ALL, type, PINCOUNT, "Number of Input Pins");
		io.github.tanguygab.logicsim3.I18N.addGate("de", type, io.github.tanguygab.logicsim3.I18N.TITLE, "Moduleingänge");
		io.github.tanguygab.logicsim3.I18N.addGate("de", type, io.github.tanguygab.logicsim3.I18N.DESCRIPTION,
				"Eingangsgatter für Module - klicke den Labelbereich um einen Pin zu benennen.");
		I18N.addGate("de", type, PINCOUNT, "Anzahl Eingänge");
	}

}