package io.github.tanguygab.logicsim3.gates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import io.github.tanguygab.logicsim3.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.Lang;
import io.github.tanguygab.logicsim3.Pin;

/**
 * Counter Component (rising edge driven)
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class Counter extends Gate {
	static final long serialVersionUID = 3971572931629721831L;

	private static final String DISPLAY_TYPE = "displaytype";
	private static final String DISPLAY_TYPE_HEX = "hex";
	private static final String DISPLAY_TYPE_DEC = "dec";
	private static final String DISPLAY_TYPE_DEFAULT = "hex";

	String displayType;

	boolean lastInputState = true;

	int value = 0;

	public Counter() {
		super("output");
		type = "counter";
		height = 90;
		createInputs(1);
		createOutputs(8);
		loadProperties();
		reset();
	}

	@Override
	protected void loadProperties() {
		displayType = getPropertyWithDefault(DISPLAY_TYPE, DISPLAY_TYPE_DEFAULT);
	}

	@Override
	public void reset() {
		super.reset();
		value = 0;
		setOutputs();
	}

	/**
	 * check for rising edge
	 */
	@Override
	public void changedLevel(LSLevelEvent e) {
		if (e.source.equals(getPin(0)) && e.level == HIGH) {
			// rising edge detection
			value++;
			if (value > 0xff)
				value = 0;
			setOutputs();
		}
	}

	private void setOutputs() {
		for (int i = 0; i < 8; i++) {
			boolean b = ((value & (1 << i)) != 0);
			LSLevelEvent evt = new LSLevelEvent(this, b);
			getPin(i + 1).changedLevel(evt);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		int x = getX();
		int y = getY();

		g.setPaint(Color.BLACK);
		String sval = "";
		if (DISPLAY_TYPE_DEC.equals(displayType)) {
			sval = Integer.toString(value);
		} else
			sval = Integer.toHexString(value);
		if (sval.length() == 0)
			sval = "00";
		if (sval.length() == 1)
			sval = "0" + sval;

		sval = sval.toUpperCase();
		g.setFont(bigFont);
		int sw = g.getFontMetrics().stringWidth(sval);
		g.drawString(sval, x + getWidth() / 2 - sw / 2, y + height / 2 + 18);
		g.setFont(Pin.smallFont);
		String s = "CNT";
		sw = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x + getWidth() / 2 - sw / 2, y + 16);
		s = displayType.toUpperCase();
		sw = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x + getWidth() / 2 - sw / 2, y + 27);
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		super.showPropertiesUI(frame);
		JRadioButton jRadioButton1 = new JRadioButton();
		JRadioButton jRadioButton2 = new JRadioButton();

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(jRadioButton1);
		group.add(jRadioButton2);

		if (DISPLAY_TYPE_HEX.equals(displayType))
			jRadioButton1.setSelected(true);
		else
			jRadioButton2.setSelected(true);

		JPanel jPanel1 = new JPanel();
		TitledBorder titledBorder1;
		BorderLayout borderLayout1 = new BorderLayout();

		titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)),
				I18N.getString(type, DISPLAY_TYPE));
		jRadioButton1.setText(I18N.getString(type, DISPLAY_TYPE_HEX));
		jRadioButton2.setText(I18N.getString(type, DISPLAY_TYPE_DEC));
		jPanel1.setBorder(titledBorder1);
		jPanel1.setBounds(new Rectangle(11, 11, 171, 150));
		jPanel1.setLayout(borderLayout1);
		jPanel1.add(jRadioButton1, BorderLayout.NORTH);
		jPanel1.add(jRadioButton2, BorderLayout.CENTER);

		JOptionPane pane = new JOptionPane(jPanel1);
		pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		pane.setOptions(new String[] { I18N.tr(Lang.OK), I18N.tr(Lang.CANCEL) });
		JDialog dlg = pane.createDialog(frame, I18N.tr(Lang.SETTINGS));
		dlg.setResizable(true);
		dlg.setSize(290, 180);
		dlg.setVisible(true);
		if (I18N.tr(Lang.OK).equals((String) pane.getValue())) {
			if (jRadioButton1.isSelected()) {
				displayType = DISPLAY_TYPE_HEX;
			} else if (jRadioButton2.isSelected()) {
				displayType = DISPLAY_TYPE_DEC;
			}
			label = displayType.toUpperCase();
			return true;
		}
		return false;
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Counter");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Counter counts positive edges");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE, "Type");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_DEC, "Decimal (00..255)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_HEX, "Hexadecimal (00..FF)");
		I18N.addGate("de", type, I18N.TITLE, "Zähler");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Zählmodul (positive Flanke)");
	}

}