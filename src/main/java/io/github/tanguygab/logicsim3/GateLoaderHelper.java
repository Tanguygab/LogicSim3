package io.github.tanguygab.logicsim3;

import io.github.tanguygab.logicsim3.parts.Gate;
import io.github.tanguygab.logicsim3.parts.Module;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class GateLoaderHelper {

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @return The classes
     */
	static List<Class<?>> getClasses() throws ClassNotFoundException, URISyntaxException {
		if (App.Running_From_Jar) return getClassesOutsideJar();

		File directory = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		directory = new File(directory, "io/github/tanguygab/logicsim3/gates");

		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) return classes;

		File[] files = directory.listFiles();
		assert files != null;
		for (File file : files) {
			if (!file.getName().endsWith(".class")) continue;
			classes.add(Class.forName("io.github.tanguygab.logicsim3.gates."+file.getName().substring(0,file.getName().length()-6)));
		}
		return classes;
	}

	public static List<Class<?>> getClassesOutsideJar() {
		List<String> classNames = new ArrayList<>();

		File directory = new File("gates");
		File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
			if (!file.getName().endsWith(".class"))
				continue;
			classNames.add(file.getName().substring(0, file.getName().length() - 6));
		}

		List<Class<?>> classes = new ArrayList<>();

		File file = new File(".");
		try {
			// Convert File to URL
			URL url = file.toURI().toURL();
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			URLClassLoader cl = new URLClassLoader(urls);
			for (String className : classNames) {
				Class<?> cls = cl.loadClass("gates." + className);
				classes.add(cls);
			}
			cl.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
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
