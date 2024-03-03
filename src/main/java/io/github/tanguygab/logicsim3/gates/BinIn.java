package io.github.tanguygab.logicsim3.gates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSLevelEvent;
import io.github.tanguygab.logicsim3.LSMouseEvent;
import io.github.tanguygab.logicsim3.Lang;
import io.github.tanguygab.logicsim3.parts.Pin;

/**
 * Binary Input Component for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class BinIn extends Gate {
	static final long serialVersionUID = 3971572931629721831L;

	private static final String DISPLAY_TYPE = "displaytype";
	private static final String DISPLAY_TYPE_HEX = "hex";
	private static final String DISPLAY_TYPE_DEC = "dec";
	private static final String DISPLAY_TYPE_SDEC = "Sdec";
	private static final String DISPLAY_TYPE_DEFAULT = "dec";

	private static final String TYPE = "type";

	String displayType;
	
	// up de gauche
	Rectangle rect1 = new Rectangle(27, 32, 15, 15);
	// up de droite
	Rectangle rect2 = new Rectangle(47, 32, 15, 15);
	// down de gauche
	Rectangle rect3 = new Rectangle(27, 67, 15, 15);
	// down de droite
	Rectangle rect4 = new Rectangle(47, 67, 15, 15);

	public BinIn() {
		super("input");
		type = "binin";
		height = 90;
		width=90;
		createOutputs(8);
		loadProperties();
	}

	@Override
	public void loadProperties() {
		displayType = getPropertyWithDefault(DISPLAY_TYPE, DISPLAY_TYPE_DEFAULT);
	}

	@Override
	public void interact() {
		int value = getValue();
		value++;
		if (value > 0xff)
			value = 0;
		setValue(value);
	}

	private int getValue() {
		int value = 0;
		boolean dSign = DISPLAY_TYPE_SDEC.equals(displayType);
		//System.out.print(dSign);
		for (int i = 0; i < 8; i++) {
			if (getPin(i).getLevel())
				value += (1 << i);
		}
		if (getPin(7).getLevel() && dSign)
	  		{
			System.out.println("there");
			System.out.println(value);
			value=value-256;
			System.out.println(value);
			}
		return value;
	}

	private void setValue(int value) {
		for (int i = 0; i < 8; i++) {
			boolean b = (value & (1 << i)) != 0;
			getPin(i).changedLevel(new LSLevelEvent(this, b));
		}
	}

	@Override
	public void mousePressedSim(LSMouseEvent e) {
		super.mousePressedSim(e);

		int dx = e.getX() - getX();
		int dy = e.getY() - getY();
		int value = getValue();
		boolean dHex = DISPLAY_TYPE_HEX.equals(displayType);
		boolean dSign = DISPLAY_TYPE_SDEC.equals(displayType);

		// Si on appuie sur up de gauche
		if (rect1.contains(dx, dy)) {
			if (dHex)
				value += 16;
			else
				value += 10;
		} else if (rect2.contains(dx, dy))  { // appui de up de droite
			value += 1;
		} else if (rect3.contains(dx, dy)) { // appui sur down gauche
			if (dHex)
				value -= 16;
			else
				value -= 10;
		} else if (rect4.contains(dx, dy)) {	// appui sur down drote
			System.out.println(value);	
			value -= 1;
			System.out.println(value);	
		}

		System.out.println(dSign);
		if (value < 0)
			if (!dSign)
				value = 0xff + value + 1;
		if (dSign && value<-128)
				value=value+255+1;
		if (dSign && value>127)
		{
			System.out.println("l�");
			value=value-256;
			//System.out.println(value);	
		}
		if (!dSign && value > 0xff)
			value = value - 0xff - 1;
		
		//System.out.println(value);	
		setValue(value);
		//System.out.println(value);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		int x = getX();
		int y = getY();
		boolean dHex = DISPLAY_TYPE_HEX.equals(displayType);
		boolean dSign = DISPLAY_TYPE_SDEC.equals(displayType);
		
		g.setPaint(Color.BLUE);

		int value = 0;
		for (int i = 0; i < 8; i++) {
			if (getOutputs().get(i).getLevel())
				value += (1 << i);
		}
		if (getPin(7).getLevel() && dSign)
  		{
		value=value-256;
		}
		// draw triangles
		Rectangle rect = rect1.getBounds();
		Polygon p = new Polygon();
		p.addPoint(x + rect.x + rect.width, y + rect.y + rect.height);
		p.addPoint(x + rect.x + rect.width / 2, y + rect.y);
		p.addPoint(x + rect.x, y + rect.y + rect.height);
		g.fill(p);
		rect = rect2;
		p = new Polygon();
		p.addPoint(x + rect.x + rect.width, y + rect.y + rect.height);
		p.addPoint(x + rect.x + rect.width / 2, y + rect.y);
		p.addPoint(x + rect.x, y + rect.y + rect.height);
		g.fill(p);
		rect = rect3;
		p = new Polygon();
		p.addPoint(x + rect.x + rect.width, y + rect.y);
		p.addPoint(x + rect.x + rect.width / 2, y + rect.y + rect.height);
		p.addPoint(x + rect.x, y + rect.y);
		g.fill(p);
		rect = rect4;
		p = new Polygon();
		p.addPoint(x + rect.x + rect.width, y + rect.y);
		p.addPoint(x + rect.x + rect.width / 2, y + rect.y + rect.height);
		p.addPoint(x + rect.x, y + rect.y);
		g.fill(p);
		g.setPaint(Color.BLACK);
		String sval = "";

		if (dHex)
			sval = Integer.toHexString(value);
		else
			sval = Integer.toString(value);
		if (sval.length() == 0)
			sval = "00";
		if (sval.length() == 1)
			sval = "0" + sval;

		sval = sval.toUpperCase();
		g.setFont(bigFont);
		int sw = g.getFontMetrics().stringWidth(sval);
		g.drawString(sval, x + getWidth() / 2 - sw / 2, y + height / 2 + 17);
		g.setFont(Pin.smallFont);
		String s = "INPUT";
		sw = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x + getWidth() / 2 - sw / 2, y + 17);
		s = displayType.toUpperCase();
		sw = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x + getWidth() / 2 - sw / 2, y + 29);
	}

	@Override
	public boolean showPropertiesUI(Component frame) {
		super.showPropertiesUI(frame);
		JRadioButton jRadioButton1 = new JRadioButton();
		JRadioButton jRadioButton2 = new JRadioButton();
		JRadioButton jRadioButton3 = new JRadioButton();

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(jRadioButton1);
		group.add(jRadioButton2);
		group.add(jRadioButton3);

		if (DISPLAY_TYPE_HEX.equals(displayType))
			jRadioButton1.setSelected(true);
		else if (DISPLAY_TYPE_DEC.equals(displayType))
				jRadioButton2.setSelected(true);
		else if (DISPLAY_TYPE_SDEC.equals(displayType)) 
				jRadioButton3.setSelected(true);

		JPanel jPanel1 = new JPanel();
		TitledBorder titledBorder1;
		BorderLayout borderLayout1 = new BorderLayout();

		titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)),
				I18N.getString(type, TYPE));
		jRadioButton1.setText(I18N.getString(type, DISPLAY_TYPE_HEX));
		jRadioButton2.setText(I18N.getString(type, DISPLAY_TYPE_DEC));
		jRadioButton3.setText(I18N.getString(type, DISPLAY_TYPE_SDEC));
		jPanel1.setBorder(titledBorder1);
		jPanel1.setBounds(new Rectangle(11, 11, 171, 150));
		jPanel1.setLayout(borderLayout1);
		jPanel1.add(jRadioButton1, BorderLayout.SOUTH);
		jPanel1.add(jRadioButton2, BorderLayout.CENTER);
		jPanel1.add(jRadioButton3, BorderLayout.NORTH);

		JOptionPane pane = new JOptionPane(jPanel1);
		pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		pane.setOptions(new String[] { I18N.tr(Lang.OK), I18N.tr(Lang.CANCEL) });
		JDialog dlg = pane.createDialog(frame, I18N.tr(Lang.SETTINGS));
		dlg.setResizable(true);
		dlg.setSize(320, 180);
		dlg.setVisible(true);
		if (I18N.tr(Lang.OK).equals((String) pane.getValue())) {
			if (jRadioButton1.isSelected()) {
				displayType = DISPLAY_TYPE_HEX;
			} else if (jRadioButton2.isSelected()) {
				displayType = DISPLAY_TYPE_DEC;
			} else if (jRadioButton3.isSelected()) {
				displayType = DISPLAY_TYPE_SDEC;
			} 
			return true;
		}
		return false;
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Binary Input 8 inputs");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Binary Input (input hex or binary)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_DEC, "Decimal (00..255)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_HEX, "Hexadecimal (00..FF)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_SDEC, "Signed Decimal (-128..+127)");
		I18N.addGate(I18N.ALL, type, TYPE, "Type");

		I18N.addGate("de", type, I18N.TITLE, "Binäreingabe");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Binäreingabe (Hex und Binär)");
		I18N.addGate("de", type, DISPLAY_TYPE_DEC, "Dezimal (00..255)");
		I18N.addGate("de", type, DISPLAY_TYPE_HEX, "Hexadezimal (00..FF)");

		I18N.addGate("es", type, I18N.TITLE, "Entrada binaria");
		I18N.addGate("es", type, TYPE, "Tipo de Visualizador");

		I18N.addGate("fr", type, I18N.TITLE, "roue codeuse");
		I18N.addGate("fr", type, TYPE, "Type d'affichage");
		I18N.addGate("fr", type, DISPLAY_TYPE_DEC, "Décimal (00..99)");
		I18N.addGate("fr", type, DISPLAY_TYPE_HEX, "Hexadécimal (00..FF)");

	}
}