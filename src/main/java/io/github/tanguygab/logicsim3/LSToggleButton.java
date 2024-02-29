package io.github.tanguygab.logicsim3;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

public class LSToggleButton extends JToggleButton {

	private static final long serialVersionUID = 4992541122998327288L;
	public final String id;
	public LSToggleButton(String iconName, Lang toolTip) {
		this.setDoubleBuffered(true);
		this.setIcon(getIcon(iconName));
		this.setToolTipText(I18N.tr(toolTip));
		this.id = I18N.langToStr(toolTip);
		// this.setBorderPainted(true);
		// this.setBorder(BorderFactory.createLineBorder(Color.black));
		// this.addMouseListener(this);
	}

	private ImageIcon getIcon(String imgname) {
		String filename = "images/" + imgname + ".png";
		int is = LSProperties.getInstance().getPropertyInteger("iconsize", 36);
		// return new ImageIcon(LSFrame.class.getResource(filename));
		return new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource(filename)).getImage().getScaledInstance(is, is,
				Image.SCALE_AREA_AVERAGING));
	}

}
