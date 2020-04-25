package gates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import logicsim.Gate;
import logicsim.I18N;
import logicsim.LSLevelEvent;
import logicsim.LSProperties;
import logicsim.Pin;

/**
 * Tri-State-Output
 * 
 * @author Peter Gabriel
 * @version 1.0
 */
public class TriStateOutput extends Gate {
	static final long serialVersionUID = 4521959944440523564L;

	public TriStateOutput() {
		super("output");
		type = "triout";
		createInputs(2);
		createOutputs(1);
		width = 40;
		height = 40;

		getPin(0).moveTo(getX() + 20, getY());
		getPin(0).setDirection(Pin.DOWN);
		getPin(1).moveTo(getX(), getY() + 20);
		getPin(2).moveTo(getX() + 40, getY() + 20);
		getPin(1).setProperty(TEXT, "I");
		getPin(2).setProperty(TEXT, "O");

		reset();
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		// pin 0 - switching
		// pin 1 - input
		// pin 2 - output

		boolean b0 = getPin(0).getLevel();
		if (b0 == LOW) {
			getPin(2).changedLevel(new LSLevelEvent(this, LOW));
		} else {
			getPin(2).changedLevel(new LSLevelEvent(this, getPin(1).getLevel()));
		}
	}

	@Override
	protected void drawFrame(Graphics2D g2) {
		String gateType = LSProperties.getInstance().getProperty(LSProperties.GATEDESIGN, LSProperties.GATEDESIGN_IEC);
		if (gateType.equals(LSProperties.GATEDESIGN_IEC))
			super.drawFrame(g2);
		else
			drawANSI(g2);
	}

	private void drawANSI(Graphics2D g2) {
		Path2D p = new Path2D.Double();
		double yu = getY() + 3;
		double xr = getX() + width - CONN_SIZE + 1;
		double yb = getY() + height - 3;
		double xl = getX() + CONN_SIZE;

		p.moveTo(xl, yc);
		p.lineTo(xl, yb);
		p.lineTo(xr, yc);
		p.lineTo(xl, yu);
		p.closePath();

		g2.setPaint(Color.WHITE);
		g2.fill(p);
		g2.setPaint(Color.black);
		g2.draw(p);

		g2.drawLine(getPin(0).getX(), getPin(0).getY() + 5, getPin(0).getX(), getPin(0).getY() + 10);
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Tri-State Output");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Tri-State Output");
		I18N.addGate("de", type, I18N.TITLE, "Tri-State Output");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Schaltbarer Ausgang - hochohmig, wenn nicht geschaltet");
	}

}