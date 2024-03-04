package io.github.tanguygab.logicsim3.gui;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.parts.Module;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class GateListRenderer extends JLabel implements ListCellRenderer<Object> {
	private static final long serialVersionUID = -361281475843085219L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

		setFont(list.getFont());
		setOpaque(true);
		if (value instanceof Gate) {
			Gate gate = (Gate) value;
			if (isSelected) {
				setForeground(Color.white);
				setBackground(new Color(0xaa, 0xaa, 0xFF));
			} else {
				setForeground(list.getForeground());
				setBackground(list.getBackground());
			}
			if (value instanceof Module) {
				setText(gate.type);
			} else {
				String s = gate.type;
				if (I18N.hasString("gate." + s + ".title"))
					s = I18N.getString(s, "title");
				setText(s);
			}
			setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
		if (value instanceof String) {
			setText(I18N.tr((String)value));
			setBackground(Color.LIGHT_GRAY);
			setForeground(Color.WHITE);
			setHorizontalAlignment(SwingConstants.CENTER);
			return this;
		}
		throw new RuntimeException("unknown format of object in getcelllistrenderer");
	}

}
