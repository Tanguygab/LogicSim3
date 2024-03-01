package io.github.tanguygab.logicsim3.gates;

import java.awt.Graphics2D;

import io.github.tanguygab.logicsim3.Pin;
import io.github.tanguygab.logicsim3.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.Wire;

/**
 * SR-Latch for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class SRLatch extends Gate {
	static final long serialVersionUID = 1049162074522360589L;

	Gate nor1 = new NOR();
	Gate nor2 = new NOR();
	// Circuit c = new Circuit();

	public SRLatch() {
		super("flipflops");
		type = "srl";
		createInputs(2);
		createOutputs(2);

		getPin(0).setProperty(TEXT, "S");
		getPin(1).setProperty(TEXT, "R");

		getPin(2).setProperty(TEXT, "Q");
		getPin(3).setProperty(TEXT, "/Q");

		getPin(0).moveBy(0, 10);
		getPin(1).moveBy(0, -10);
		getPin(2).moveBy(0, 10);
		getPin(3).moveBy(0, -10);

		// build gate
		// https://www.elektronik-kompendium.de/sites/dig/0209302.htm

		Wire nor1_nor2a = new Wire(nor1.getPin(0), nor2.getPin(1));
		nor1.getPin(0).connect(nor1_nor2a);
		nor2.getPin(1).connect(nor1_nor2a);

		Wire nor2_nor1b = new Wire(nor2.getPin(0), nor1.getPin(2));
		nor2.getPin(0).connect(nor2_nor1b);
		nor1.getPin(2).connect(nor2_nor1b);

		nor1.getPin(0).addLevelListener(this);
		nor2.getPin(0).addLevelListener(this);

		// initial change level
		//nor1.getPin(0).changedLevel(new LSLevelEvent(nor1, HIGH));
		//nor2.getPin(0).changedLevel(new LSLevelEvent(nor2, HIGH));
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);
		drawLabel(g2, "SRL", Pin.smallFont);
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		Pin p = (Pin) e.source;
		if (p.isInput()) {
			// forward to nor-pin
			if (p.number == 0)
				nor1.getPin(1).changedLevel(new LSLevelEvent(new Wire(null, null), getPin(0).getLevel()));
			else
				nor2.getPin(2).changedLevel(new LSLevelEvent(new Wire(null, null), getPin(1).getLevel()));
		} else {
			// output
			if (p.parent == nor1) {
				LSLevelEvent evt2 = new LSLevelEvent(this, !e.level);
				getPin(2).changedLevel(evt2);
			} else {
				LSLevelEvent evt3 = new LSLevelEvent(this, !e.level);
				getPin(3).changedLevel(evt3);
			}
		}
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "SR Latch");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "SR Latch");
	}
}