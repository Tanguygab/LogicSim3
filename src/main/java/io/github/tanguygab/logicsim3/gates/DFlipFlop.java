package io.github.tanguygab.logicsim3.gates;

import java.awt.Color;
import java.awt.Graphics2D;

import io.github.tanguygab.logicsim3.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.Pin;

/**
 * D-Flipflop for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class DFlipFlop extends Gate {

	public DFlipFlop() {
		super("flipflops");
		type = "dff";
		createInputs(2);
		createOutputs(2);

		getPin(0).setProperty(TEXT, "D");
		getPin(1).setProperty(TEXT, Pin.POS_EDGE_TRIG);

		getPin(2).setProperty(TEXT, "Q");
		getPin(3).setProperty(TEXT, "/Q");
		getPin(3).setLevelType(Pin.INVERTED);

		getPin(0).moveBy(0, 10);
		getPin(1).moveBy(0, -10);
		getPin(2).moveBy(0, 10);
		getPin(3).moveBy(0, -10);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		g2.setColor(Color.black);
		drawLabel(g2, "D-FF", Pin.smallFont);
	}

	/**
	 * https://www.electronicsforu.com/resources/learn-electronics/flip-flop-rs-jk-t-d
	 */
	@Override
	public void changedLevel(LSLevelEvent e) {
		if (e.source.equals(getPin(1)) && e.level == HIGH) {
			// rising edge detection
			boolean d = getPin(0).getLevel();
			LSLevelEvent evt = new LSLevelEvent(this, d, true);
			getPin(2).changedLevel(evt);
			getPin(3).changedLevel(evt);
		}
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "D Flip-flop");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "D Flip-flop");
		I18N.addGate("de", type, I18N.TITLE, "D Flipflop");
		I18N.addGate("de", type, I18N.DESCRIPTION, "D Flipflop");
		I18N.addGate("es", type, I18N.TITLE, "FlipFlop D");
		I18N.addGate("fr", type, I18N.TITLE, "Bascule D");
	}
}