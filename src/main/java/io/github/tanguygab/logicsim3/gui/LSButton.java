package io.github.tanguygab.logicsim3.gui;

import io.github.tanguygab.logicsim3.App;
import io.github.tanguygab.logicsim3.I18N;
import io.github.tanguygab.logicsim3.LSProperties;
import io.github.tanguygab.logicsim3.Lang;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class LSButton extends JButton {

	private static final long serialVersionUID = 4465562539140913810L;

	public LSButton(String iconName, Lang toolTip) {
		setDoubleBuffered(true);
		setIcon(getIcon(iconName));
		setToolTipText(I18N.tr(toolTip));
		setName(toolTip.toString());
		// this.setBorderPainted(true);
		// this.setBorder(BorderFactory.createLineBorder(Color.black));
		// this.addMouseListener(this);
	}

	private ImageIcon getIcon(String imgname) {
		String filename = "images/" + imgname + ".png";
		int is = LSProperties.getInstance().getPropertyInteger("iconsize", 36);
		// return new ImageIcon(LSFrame.class.getResource(filename));
		return new ImageIcon(new ImageIcon(App.getInstance().getResource(filename))
								.getImage()
								.getScaledInstance(is, is, Image.SCALE_AREA_AVERAGING));
	}
}
