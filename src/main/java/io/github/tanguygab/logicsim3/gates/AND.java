package io.github.tanguygab.logicsim3.gates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.LSProperties;
import io.github.tanguygab.logicsim3.parts.Pin;

/**
 * AND Gate for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class AND extends Gate {
	static final long serialVersionUID = 4521959944440523564L;

	public AND() {
		super("basic");
		label = "&";
		type = "and";
		createOutputs(1);
		createInputs(2);
		variableInputCountSupported = true;
	}

	@Override
	public void simulate() {
		super.simulate();
		boolean newLevel = true;
		for (Pin c : getInputs()) {
			newLevel = newLevel && c.getLevel();
			if (!newLevel)
				break;
		}
		// call pin directly
		getPin(0).changedLevel(new LSLevelEvent(this, newLevel, force));
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		super.changedLevel(e);
		simulate();
	}

	@Override
	protected void drawRotated(Graphics2D g2) {
		String gateType = LSProperties.getInstance().getProperty(LSProperties.GATEDESIGN, LSProperties.GATEDESIGN_IEC);
		if (gateType.equals(LSProperties.GATEDESIGN_ANSI)) {
			Path2D p = new Path2D.Double();
			p.moveTo(getX() + CONN_SIZE, getY() + CONN_SIZE);
			p.lineTo(getX() + width - 4 * CONN_SIZE, getY() + CONN_SIZE);
			double x1 = getX() + width + 1.4f;
			p.curveTo(x1, getY() + CONN_SIZE, x1, getY() + height - CONN_SIZE, getX() + width - 4 * CONN_SIZE,
					getY() + height - CONN_SIZE);
			p.lineTo(getX() + CONN_SIZE, getY() + height - CONN_SIZE);
			p.closePath();
			g2.setPaint(Color.WHITE);
			g2.fill(p);
			g2.setPaint(Color.black);
			g2.draw(p);
		}
	}

	@Override
	protected void drawFrame(Graphics2D g2) {
		String gateType = LSProperties.getInstance().getProperty(LSProperties.GATEDESIGN, LSProperties.GATEDESIGN_IEC);
		if (gateType.equals(LSProperties.GATEDESIGN_IEC))
			super.drawFrame(g2);
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "AND");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "AND Gate (variable Inputcount)");
		I18N.addGate("de", type, I18N.DESCRIPTION, "AND Gatter mit einstellbarer Eingangsanzahl");
		I18N.addGate("es", type, I18N.TITLE, "AND (Y)");
		I18N.addGate("fr", type, I18N.TITLE, "Et (AND)");
	}

}