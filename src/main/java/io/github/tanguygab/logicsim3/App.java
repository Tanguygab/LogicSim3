package io.github.tanguygab.logicsim3;

import io.github.tanguygab.logicsim3.gui.LSFrame;
import io.github.tanguygab.logicsim3.parts.*;
import lombok.Getter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class App {

	public static final String APP_TITLE = "LogicSim";
	public static final String CIRCUIT_FILE_SUFFIX = "lsc";
	public static final String MODULE_FILE_SUFFIX = "lsm";
	//public static final String GRAPHICS_FORMAT = "png";

	public static boolean Running_From_Jar = false;
	public static List<Category> cats = new ArrayList<>();

	@Getter private static App instance;
	/**
	 * Main method
	 */
	public static void main(String[] args) {
		new App();
	}

	public App() {
		instance = this;
		URL url = getClass().getResource("");
		if (url != null && "jar".equals(url.getProtocol()))
			Running_From_Jar = true;
		new I18N();
		initializeGateCategories();

		// center the window and adjust dimensions
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = new Dimension(1024, 768);
		if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;

        LSFrame lsframe = new LSFrame();
		lsframe.setSize(frameSize);
		lsframe.setLocation((screenSize.width - frameSize.width) / 2,
							(screenSize.height - frameSize.height) / 2);
		lsframe.setVisible(true);
		
		Wire.setColorMode();
	}

	public URL getResource(String file) {
		return getClass().getClassLoader().getResource(file);
	}
	public InputStream getResourceAsStream(String file) {
		return getClass().getClassLoader().getResourceAsStream(file);
	}

	public List<String> getFiles(String jarPath, String idePath, String extension) {
		try {
			File directory = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			List<String> names = new ArrayList<>();

			if (App.Running_From_Jar) {
				final JarFile jar = new JarFile(directory.getPath());

                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				while (entries.hasMoreElements()) {
					final String name = entries.nextElement().getName();
					if (name.startsWith(jarPath) && !name.equals(jarPath) && name.endsWith(extension)) { //filter according to the path
						names.add(name.substring(jarPath.length(),name.length()-extension.length()));
					}
				}
				jar.close();
			}
			else {
				directory = new File(directory, idePath);
				String[] files = directory.list();
				assert files != null;
				for (String file : files) {
					if (file.endsWith(extension)) {
						String name = file.substring(0, file.length() - extension.length());
						names.add(name);
					}
				}
			}
			Collections.sort(names);
			return names;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void addToCategory(Gate g) {
		String cattitle = g.category;
		if (g.category == null) cattitle = "hidden";

		Category cat = null;
		for (Category c : cats) {
			if (c.getTitle().equals(cattitle)) {
				cat = c;
				break;
			}
		}
		if (cat == null) {
			cat = new Category(cattitle);
			cats.add(cat);
		}
		cat.addGate(g);
	}

	private static void initializeGateCategories() {
		Category cat = new Category("hidden");
		Gate g = new MODIN();
		g.loadLanguage();
		cat.addGate(g);
		g = new MODOUT();
		g.loadLanguage();
		cat.addGate(g);
		cats.add(cat);

		cats.add(new Category("basic"));
		cats.add(new Category("input"));
		cats.add(new Category("output"));
		cats.add(new Category("flipflops"));

		try {
			for (Class<?> c : GateLoaderHelper.getClasses()) {
				Gate gate = (Gate) c.getDeclaredConstructor().newInstance();
				gate.loadLanguage();
				addToCategory(gate);
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }

        loadModules();
	}

	private static void loadModules() {
		/*
		 * module part
		 */
		File mods = new File(getModulePath());
		// list of filenames in modules dir
		String[] list = mods.list();
        assert list != null;
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		// prepare list for all loaded modules
		List<String> loadedModules = new ArrayList<>();
		// prepare list of modules with sublist of needed modules
		Map<String, List<String>> modules = new HashMap<>();

		// now collect all modules with their needed modules
        for (String filename : list) {
            if (filename.endsWith(MODULE_FILE_SUFFIX)) {
                String type = new File(filename).getName();
                type = type.substring(0, type.lastIndexOf("."));
                //type = type.toLowerCase();
                modules.put(type, XMLLoader.getModuleListFromFile(getModulePath() + "/" + filename));
            }
        }
		int maxTries = modules.keySet().size();
		int tries = 0;
		while (tries < maxTries && maxTries != loadedModules.size()) {
			for (String modName : modules.keySet()) {
				boolean load = true;
				for (String neededModuleName : modules.get(modName)) {
					if (!loadedModules.contains(neededModuleName.toLowerCase())) {
						load = false;
						break;
					}
				}
				if (load && !loadedModules.contains(modName.toLowerCase())) {
					Module mod = new Module(modName);
					mod.category = "module";
					addToCategory(mod);
					loadedModules.add(modName.toLowerCase());
				}
			}
			tries++;
		}
	}

	private static String getPath(String path) {
		File file = new File("");
		String fileName = file.getAbsolutePath() + "/" + path;
		file = new File(fileName);
		if (!file.exists() || !file.isDirectory()) file.mkdir();
		return file.getAbsolutePath() + "/";
	}

	public static String getModulePath() {
		return getPath("modules");
	}

	public static String getCircuitPath() {
		return getPath("circuits");
	}

	public static Gate getGate(String type) {
		for (Category cat : cats) {
			for (Gate g : cat.getGates()) {
				if (g.type.toLowerCase().equals(type))
					return g;
			}
		}
		return null;
	}


}