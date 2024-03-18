package io.github.tanguygab.logicsim3;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.parts.Module;

import java.util.ArrayList;
import java.util.List;

public class GateLoaderHelper {

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @return The classes
     */
	static List<Class<?>> getClasses() throws ClassNotFoundException {
		List<String> files = App.getInstance().getFiles("io/github/tanguygab/logicsim3/gates/","io/github/tanguygab/logicsim3/gates/",".class");
		List<Class<?>> classes = new ArrayList<>();
		for (String file : files) {
			classes.add(Class.forName("io.github.tanguygab.logicsim3.gates."+file));
		}
		return classes;
	}

	public static Gate create(Gate g) {
		try {
			Gate gate;
			Class<? extends Gate> c = g.getClass();

			if (g instanceof Module) {
				Class<?>[] cArg = new Class[] { String.class, Boolean.TYPE };
				gate = c.getDeclaredConstructor(cArg).newInstance(g.type, true);
			} else gate = c.getDeclaredConstructor().newInstance();
			return gate;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
