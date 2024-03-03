package io.github.tanguygab.logicsim3.parts;

import io.github.tanguygab.logicsim3.*;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JOptionPane;

/**
 * Module implementation
 * 
 * taken from Orginal Module by Andreas Tetzl
 * 
 * @author Peter Gabriel
 *
 */
public class Module extends Gate {
	static final long serialVersionUID = 3938879095465005332L;

	private MODIN moduleIn = null;
	private MODOUT moduleOut = null;
	public LogicSimFile lsFile = new LogicSimFile(null);

	private boolean embedded = true;

	public Module(String type) {
		super();
		this.type = type;
		loadModule();
	}

	public Module(String type, boolean embedded) {
		super();
		this.type = type;
		this.embedded = embedded;
		loadModule();
	}

	/**
	 * loads module from file
	 */
	public void loadModule() {
		String filename = App.getModulePath() + type + "." + App.MODULE_FILE_SUFFIX;
		File f = new File(filename);
		if (!f.exists()) {
			String s = I18N.tr(Lang.MODULENOTFOUND).replaceFirst("%s", type);
			JOptionPane.showMessageDialog(null, s);
			return;
		}
		try {
			lsFile = XMLLoader.loadXmlFile(filename);
		} catch (RuntimeException x) {
			JOptionPane.showMessageDialog(null, I18N.tr(Lang.READERROR) + ": " + x.getMessage());
			return;
		}

		if (lsFile == null)
			return;
		if (lsFile.circuit == null)
			return;
		if (lsFile.getErrorString() != null) {
			Dialogs.messageDialog(null, lsFile.getErrorString());
		}

		label = lsFile.getLabel();

		// postprocessing: search for MODIN and MODOUT
		for (CircuitPart g : lsFile.circuit.getParts()) {
			if (g instanceof MODIN) {
				moduleIn = (MODIN) g;
				int numberOfInputs = moduleIn.getInputs().size();
				for (Pin c : moduleIn.getOutputs()) {
					// add MODIN's input-connectors to module:
					// check if MODIN's outputs are connected
					if (c.isConnected()) {
						Pin newIn = new Pin(getX(), getY() + 10 + (c.number - numberOfInputs) * 10, this, c.number - numberOfInputs);
						newIn.setIoType(Pin.INPUT);
						newIn.levelType = Pin.NORMAL;
						Pin in = moduleIn.getPin(c.number - numberOfInputs);
						if (in.getProperty(TEXT) != null)
							newIn.setProperty(TEXT, in.getProperty(TEXT));
						pins.add(newIn);
					}
				}
			}
		}
		for (CircuitPart g : lsFile.circuit.getParts()) {
			if (g instanceof MODOUT) {
				moduleOut = (MODOUT) g;
				int numberOfInputs = moduleOut.getInputs().size();
				// add MODOUT's output-connectors to module:
				// check if MODOUT's inputs have a wire
				for (Pin c : moduleOut.getInputs()) {
					if (c.isConnected()) {
						Pin newOut = new Pin(getX() + getWidth(), getY() + 10 + c.number * 10, this, c.number + numberOfInputs);
						newOut.setIoType(Pin.OUTPUT);
						newOut.paintDirection = Pin.LEFT;
						newOut.levelType = Pin.NORMAL;
						Pin out = moduleOut.getPin(c.number + numberOfInputs);
						if (out.getProperty(TEXT) != null)
							newOut.setProperty(TEXT, out.getProperty(TEXT));
						pins.add(newOut);
					}
				}
			}
		}
		// initialize height and reposition connectors
		int numIn = getNumInputs();
		int numOut = getNumOutputs();
		int modoutNumOut = moduleOut.getNumInputs();
		int max = (numIn > numOut) ? numIn : numOut;
		if (max > 5)
			height = 10 * max + 10;
		for (Pin c : getInputs()) {
			c.setY(getY() + getConnectorPosition(c.number, numIn, Gate.VERTICAL));
		}
		for (Pin c : getOutputs()) {
			c.setY(getY() + getConnectorPosition(c.number - modoutNumOut, numOut, Gate.VERTICAL));
		}

		if (moduleIn == null || moduleOut == null) {
			JOptionPane.showMessageDialog(null, I18N.tr(Lang.NOMODULE));
			throw new RuntimeException("no module - does not contain both moduleio components: " + type);
		}

		if (embedded) {
			// remove all wires which are connected to MODIN-Inputs
			// and remove all wires which are connected to MODOUT-Outputs
			for (Pin p : moduleIn.getInputs()) {
				p.disconnect();
			}
			for (Pin p : moduleOut.getOutputs()) {
				p.disconnect();
				p.addLevelListener(this);
			}
		}
		//send initialization
		lsFile.circuit.reset();
	}

	@Override
	public void changedLevel(LSLevelEvent e) {
		Pin p = (Pin) e.source;
		int num = p.number;
		if (p.isInput()) {
			// source is one of the module's inputs
			// forward to MODIN-output
			moduleIn.getPin(num).changedLevel(new LSLevelEvent(new Wire(null, null), e.level));
		} else {
			// is output from MODOUT
			// forward to module's output
			int target = p.number;
			LSLevelEvent evt = new LSLevelEvent(this, p.getLevel());
			getPin(target).changedLevel(evt);
		}
	}

	@Override
	public void simulate() {
		super.simulate();

		for (Pin c : getInputs()) {
			moduleIn.getPin(c.number).setLevel(c.getLevel());
		}

		if (lsFile.circuit != null) {
			lsFile.circuit.simulate();
		}

		for (Pin c : getOutputs()) {
			c.setLevel(moduleOut.getPin(c.number).getLevel());
		}
	}

	@Override
	protected void drawLabel(Graphics2D g2, String lbl, Font font) {
		//super.drawLabel(g2, type, font);
		Font newFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
		g2.setFont(mediumFont);
		int ang=0;

		int sw = g2.getFontMetrics().stringWidth(type);
		if (sw>45) {
			if (height>60)
			{
				ang=-90;
			}
			else g2.setFont(newFont);
		}
		WidgetHelper.drawStringRotated(g2, type, getX() + width / 2, getY() + height / 2, WidgetHelper.ALIGN_CENTER, ang);
	}

	@Override
	public boolean hasPropertiesUI() {
		return true;
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		if (moduleIn != null) {
			String content = I18N.tr(Lang.DESCRIPTION) + ":\n";
			content += lsFile.getDescription();
			JOptionPane.showMessageDialog(frame, content);
		}
		return true;
	}

}