package io.github.tanguygab.logicsim3.gates;

import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.parts.Pin;

/**
 * Equivalence Gate for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class EQU extends XOR {
	static final long serialVersionUID = 521585027776705481L;
	public EQU() {
		super();
		//label = "=1";
		type = "nxor";
		getPin(0).setLevelType(Pin.INVERTED);
		getPin(0).setLevel(true);
	}

/*	public EQU() {
		super("basic");
		label = "=1";
		type = "nxor";
		createOutputs(1);
		createInputs(2);
		getPin(0).setLevel(true);
		variableInputCountSupported = true;
		getPin(0).setLevelType(Pin.INVERTED); // petit rond en sortie de porte=inverseur
	}

	public void simulate() {
		int n = 0;
		for (Pin p : getInputs()) {
			if (p.getLevel())
				n++;
		}
		// if n is even, set true
		LSLevelEvent evt = new LSLevelEvent(this, n % 2 != 0, force);
		getPin(0).changedLevel(evt);
		
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		super.changedLevel(e);
		if (busted)
			return;
		simulate();
	}
*/
	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "NXOR");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Not XOR Gate");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Äquivalenz Gatter (einstellbare Eingangsanzahl)");
		I18N.addGate("es", type, I18N.TITLE, "XNOR (=)");
		I18N.addGate("fr", type, I18N.TITLE, "<-> (NXOR)");
	}
}