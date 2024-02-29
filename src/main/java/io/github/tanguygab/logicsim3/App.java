package io.github.tanguygab.logicsim3;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class App {

	public static final String APP_TITLE = "LogicSim";
	public static final String CIRCUIT_FILE_SUFFIX = "lsc";
	public static final String MODULE_FILE_SUFFIX = "lsm";
	public static final String GRAPHICS_FORMAT = "png";
	public static boolean Running_From_Jar = false;

	LSFrame lsframe;

	static long timer = 0;
	
	public static void time() {
		long newtime = System.nanoTime();
		if (timer != 0) {
			System.out.println("measure " + (newtime - timer) + " ns");
		}
		timer = newtime;
	}
	static ArrayList<io.github.tanguygab.logicsim3.Category> cats = new ArrayList<io.github.tanguygab.logicsim3.Category>();

	public App() {
		String protocol = this.getClass().getResource("").getProtocol();
		if (Objects.equals(protocol, "jar"))
			Running_From_Jar = true;
		new I18N();
		initializeGateCategories();

		// center the window and adjust dimensions
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = new Dimension(1024, 768);
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		lsframe = new LSFrame(APP_TITLE);
		lsframe.setSize(frameSize);
		lsframe.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		lsframe.setVisible(true);
		
		Wire.setColorMode();
	}

	private static void addToCategory(io.github.tanguygab.logicsim3.Gate g) {
		String cattitle = g.category;
		if (g.category == null)
			cattitle = "hidden";

		io.github.tanguygab.logicsim3.Category cat = null;
		for (io.github.tanguygab.logicsim3.Category c : cats) {
			if (c.getTitle().equals(cattitle)) {
				cat = c;
				break;
			}
		}
		if (cat == null) {
			cat = new io.github.tanguygab.logicsim3.Category(cattitle);
			cats.add(cat);
		}
		cat.addGate(g);
	}

	private static void initializeGateCategories() {
		io.github.tanguygab.logicsim3.Category cat = new io.github.tanguygab.logicsim3.Category("hidden");
		io.github.tanguygab.logicsim3.Gate g = new MODIN();
		g.loadLanguage();
		cat.addGate(g);
		g = new MODOUT();
		g.loadLanguage();
		cat.addGate(g);
		cats.add(cat);

		cats.add(new io.github.tanguygab.logicsim3.Category("basic"));
		cats.add(new io.github.tanguygab.logicsim3.Category("input"));
		cats.add(new io.github.tanguygab.logicsim3.Category("output"));
		cats.add(new io.github.tanguygab.logicsim3.Category("flipflops"));

		List<Class<?>> classes;
		try {
			classes = GateLoaderHelper.getClasses();
			for (Class<?> c : classes) {
				io.github.tanguygab.logicsim3.Gate gate = (io.github.tanguygab.logicsim3.Gate) c.getDeclaredConstructor().newInstance();
				gate.loadLanguage();
				addToCategory(gate);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
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
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		// prepare list for all loaded modules
		ArrayList<String> loadedModules = new ArrayList<String>();
		// prepare list of modules with sublist of needed modules
		Map<String, ArrayList<String>> modules = new HashMap<String, ArrayList<String>>();

		// now collect all modules with their needed modules
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(MODULE_FILE_SUFFIX)) {
				String filename = list[i];
				String type = new File(filename).getName();
				type = type.substring(0, type.lastIndexOf("."));
				//type = type.toLowerCase();
				modules.put(type, XMLLoader.getModuleListFromFile(getModulePath() + "/" + filename));
			}
		}
		int maxtries = modules.keySet().size();
		int tries = 0;
		while (tries < maxtries && maxtries != loadedModules.size()) {
			for (String modname : modules.keySet()) {
				boolean load = true;
				for (String neededModuleName : modules.get(modname)) {
					if (!loadedModules.contains(neededModuleName.toLowerCase())) {
						load = false;
						break;
					}
				}
				if (load && !loadedModules.contains(modname.toLowerCase())) {
					io.github.tanguygab.logicsim3.Module mod = new Module(modname);
					mod.category = "module";
					addToCategory(mod);
					loadedModules.add(modname.toLowerCase());
				}
			}
			tries++;
		}
	}

	public static String getModulePath() {
		File f = new File("");
		String fname = f.getAbsolutePath() + "/modules/";
		f = new File(fname);
		if (f != null && f.exists() && f.isDirectory()) {
			return new String(f.getAbsolutePath() + "/");
		} else {
			io.github.tanguygab.logicsim3.Dialogs.messageDialog(null, "Directory 'modules' not found.\nPlease run the program from its directory");
			System.exit(0);
		}

		return "";
	}

	public static String getCircuitPath() {
		File f = new File("");
		String fname = f.getAbsolutePath() + "/circuits/";
		f = new File(fname);
		if (f != null && f.exists() && f.isDirectory()) {
			return new String(f.getAbsolutePath() + "/");
		} else {
			Dialogs.messageDialog(null, "Directory 'circuits' not found.\nPlease run the program from its directory");
			System.exit(0);
		}
		return "";
	}

	public static io.github.tanguygab.logicsim3.Gate getGate(String type) {
		for (Category cat : cats) {
			for (Gate g : cat.getGates()) {
				if (g.type.toLowerCase().equals(type)) {
					return g;
				}
			}
		}
		return null;
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		new io.github.tanguygab.logicsim3.App();
	}

}