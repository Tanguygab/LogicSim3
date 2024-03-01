package io.github.tanguygab.logicsim3;

import java.awt.Component;

import javax.swing.JOptionPane;

public class Dialogs {

	public static int confirmDiscardDialog(Component comp) {
		Object[] options1 = { I18N.tr(Lang.YES), I18N.tr(Lang.NO) };

        return JOptionPane.showOptionDialog(comp, I18N.tr(Lang.QUESTION_CONFIRMDISCARD), "LogicSim",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[1]);
	}

	public static void messageDialog(Component comp, String msg) {
		JOptionPane.showMessageDialog(comp, msg);
	}

}
