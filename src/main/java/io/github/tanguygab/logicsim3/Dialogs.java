package io.github.tanguygab.logicsim3;

import java.awt.Component;

import javax.swing.JOptionPane;

public class Dialogs {
	public static final int SAVE = 1;
	public static final int CANCEL = 2;
	public static final int DONT_SAVE = 3;

	public static int confirmSaveDialog(Component comp) {
		Object[] options1 = { io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.SAVE), io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.CANCEL), io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.DONTSAVE) };

		int result = JOptionPane.showOptionDialog(comp, io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.QUESTION_CONFIRMSAVE), "LogicSim",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);

		if (result == JOptionPane.YES_OPTION) {
			return SAVE;
		} else if (result == JOptionPane.NO_OPTION) {
			return CANCEL;
		} else {
			return DONT_SAVE;
		}
	}

	public static int confirmDiscardDialog(Component comp) {
		Object[] options1 = { io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.YES), io.github.tanguygab.logicsim3.I18N.tr(io.github.tanguygab.logicsim3.Lang.NO) };
		int result = JOptionPane.showOptionDialog(comp, I18N.tr(Lang.QUESTION_CONFIRMDISCARD), "LogicSim",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[1]);

		return result;
	}

	public static void messageDialog(Component comp, String msg) {
		JOptionPane.showMessageDialog(comp, msg);
	}

}
