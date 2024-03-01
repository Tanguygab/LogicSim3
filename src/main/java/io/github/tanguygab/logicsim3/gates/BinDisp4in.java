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
import io.github.tanguygab.logicsim3.Lang;
import io.github.tanguygab.logicsim3.WidgetHelper;

/**
 * Binary Display for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class BinDisp4in extends Gate {
	static final long serialVersionUID = -6532037559895208921L;

	private static final String DISPLAY_TYPE = "displaytype";
	private static final String DISPLAY_TYPE_HEX = "hex";
	private static final String DISPLAY_TYPE_DEC = "dec";
	private static final String DISPLAY_TYPE_SDEC = "Sdec";
	private static final String DISPLAY_TYPE_DEFAULT = "dec";
	private static final String UI_TYPE = "ui.type";

	String displayType;

	public BinDisp4in() {
		super("output");
		type = "bindisp4out";
		height = 90;
		backgroundColor = Color.LIGHT_GRAY;
		createInputs(4);
		loadProperties();
	}

	@Override
	protected void loadProperties() {
		displayType = getPropertyWithDefault(DISPLAY_TYPE, DISPLAY_TYPE_DEFAULT);
	}

	@Override
	public void draw(Graphics2D g2) {
		super.draw(g2);

		Rectangle r = new Rectangle(xc - 20, yc - 15, 40, 30);
		g2.setColor(Color.white);
		g2.fill(r);
		g2.setColor(Color.black);
		g2.draw(r);
		
		int value = 0;
		int valueS=0;
		int sign=0;
		for (int i = 0; i < 4; i++) {
			if (getPin(i).getLevel())
				value += (1 << i);
		}
		
		if (getPin(3).getLevel())
			sign=1;
		if (sign==1)
			{
			for (int i = 0; i < 3; i++) {
				if (getPin(i).getLevel())
					valueS += (1 << i);
			}
			valueS-=8;	
			}
		

		String sval = displayType.toUpperCase();
		g2.setFont(smallFont);
		WidgetHelper.drawString(g2, sval, xc, getY() + 10, WidgetHelper.ALIGN_CENTER);

		g2.setFont(hugeFont);
		if (DISPLAY_TYPE_DEC.equals(displayType))  // d�cimal non sign�
			sval = Integer.toString(value);
		else 
			if (DISPLAY_TYPE_SDEC.equals(displayType)) // d�cimal sign�
				if (sign==1) // si n�gatif
					sval = Integer.toString(valueS);
				else sval = Integer.toString(value);
			else 			// hexad�cimal
				sval = Integer.toHexString(value);
		// draw display's value
		if (sval.length() == 0)
			sval = "0";
		//if (sval.length() == 1)
		//	sval = sval;
		sval = sval.toUpperCase();
		WidgetHelper.drawString(g2, sval, xc, yc, WidgetHelper.ALIGN_CENTER);
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
		else
			if (DISPLAY_TYPE_DEC.equals(displayType))
				jRadioButton2.setSelected(true);
			else
				jRadioButton3.setSelected(true);

		JPanel jPanel1 = new JPanel();
		TitledBorder titledBorder1;
		BorderLayout borderLayout1 = new BorderLayout();

		titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(142, 142, 142)),
				I18N.getString(type, UI_TYPE));
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
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Binary Display 4 outputs");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION, "Binary Display (output in signed decimal, decimal, hex)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_DEC, "Decimal (0..15)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_SDEC, "Signed Decimal (-8..7)");
		I18N.addGate(I18N.ALL, type, DISPLAY_TYPE_HEX, "Hexadecimal (0..F)");
		I18N.addGate(I18N.ALL, type, UI_TYPE, "Type");

		I18N.addGate("de", type, I18N.TITLE, "Binärdisplay");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Binärdisplay (Hex und Binär)");
	}
}