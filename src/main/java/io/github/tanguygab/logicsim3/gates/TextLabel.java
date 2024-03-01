package io.github.tanguygab.logicsim3.gates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import io.github.tanguygab.logicsim3.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.WidgetHelper;

/**
 * Text Label component for LogicSim
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */
public class TextLabel extends Gate {
	static final long serialVersionUID = 6576677427368074734L;

	static final String TEXTLABEL_DEFAULT = "Text";

	public TextLabel() {
		super("output");
		type = "label";
		width = 60;
		height = 20;
		loadProperties();
	}

	@Override
	protected void loadProperties() {
		text = getPropertyWithDefault(TEXT, TEXTLABEL_DEFAULT);
	}

	@Override
	public boolean insideFrame(int mx, int my) {
		return getBoundingBox().contains(mx, my);
	}

	@Override
	protected void drawActiveFrame(Graphics2D g2) {
		g2.setFont(bigFont);
		if (text != null) {
			Rectangle r = WidgetHelper.textDimensions(g2, text);
			width = r.width / 10 * 10 + 10;
			height = r.height / 10 * 10 + 10;
			super.drawActiveFrame(g2);
		}
	}

	@Override
	protected void drawFrame(Graphics2D g2) {
	}

	@Override
	public void drawRotated(Graphics2D g2) {
		g2.setFont(bigFont);
		g2.setColor(Color.black);
		if (text != null) {
			Rectangle r = WidgetHelper.textDimensions(g2, text);
			width = r.width;
			width = width / 10 * 10 + 10;
			height = r.height;
			height = height / 10 * 10 + 10;
			WidgetHelper.drawString(g2, text, xc, yc, WidgetHelper.ALIGN_CENTER);
		}
	}

	@Override
	public void loadLanguage() {
		I18N.addGate(I18N.ALL, type, I18N.TITLE, "Textfield");
		I18N.addGate(I18N.ALL, type, I18N.DESCRIPTION,
				"Textfield just for displaying text on the drawing surface - for documentation purposes");
		I18N.addGate(I18N.ALL, type, TEXT, "input Text");

		I18N.addGate("de", type, I18N.TITLE, "Textfeld");
		I18N.addGate("de", type, I18N.DESCRIPTION, "Textfeld für Dokumentationszwecke oder Hilfetexte");
		I18N.addGate("de", type, TEXT, "Text eingeben");

		I18N.addGate("es", type, I18N.TITLE, "Etiqueta de texto");

		I18N.addGate("fr", type, I18N.TITLE, "Étiquette");
		I18N.addGate("fr", type, TEXT, "Entrer le texte");

	}
}