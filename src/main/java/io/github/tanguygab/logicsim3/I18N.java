/*
 * I18N.java
 *
 * Created on 29. Dezember 2005, 15:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package io.github.tanguygab.logicsim3;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author atetzl
 */
public class I18N {

	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String ALL = "ALL";

	public static String lang = "en";
	public static Properties prop = null;

	/** Creates a new instance of I18N */
	public I18N() {
		if (prop != null) return;

		lang = LSProperties.getInstance().getProperty(LSProperties.LANGUAGE, "en");
		prop = load(lang);
		if (prop == null && !"en".equals(lang)) {
			prop = load("en");
			if (prop == null) {
				Dialogs.messageDialog(null,
						"Language file languages/en.txt not found.\nPlease run the program from its directory.");
				System.exit(5);
			}
		}
	}

	public static Properties load(String lang) {
		Properties properties = new Properties();
		try {
			properties.load(Files.newInputStream(Paths.get("languages/" + lang + ".txt")));
			return properties;
		} catch (Exception e) {
			return null;
		}
	}

	public static String langToStr(Lang l) {
        return l.toString().toLowerCase().replace("_", ".");
	}

	public static String tr(Lang langkey) {
		if (prop == null)
			return "- I18N not initialized -";
		return tr(langToStr(langkey));
	}

	public static String tr(String key) {
		if (prop == null)
			return "- I18N not initialized -";
		if (prop.containsKey(key)) {
			String item = prop.getProperty(key);
			if (item != null)
				return item;
		}
		System.err.println("I18N: translation of '" + key + "' is missing");
		return "-" + key + "-";
	}

	public static String getString(String id, String key) {
		return tr("gate." + id + "." + key);
	}

	public static boolean hasString(String key) {
		String item = prop.getProperty(key);
		return (item != null);
	}

	public static boolean hasString(String id, String key) {
		return hasString("gate." + id + "." + key);
	}

	public static String tr(Lang key, String value) {
		return String.format(tr(key), value);
	}

	public static List<String> getLanguages() {
		File dir = new File("languages/");
		String[] files = dir.list();
        assert files != null;
        Arrays.sort(files);
		List<String> langs = new ArrayList<String>();
        for (String file : files) {
            if (file.endsWith(".txt")) {
                String name = file.substring(0, file.length() - 4);
                langs.add(name);
            }
        }
		return langs;
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		for (Lang l : Lang.values()) {
			String key = langToStr(l);
			list.add(key);
		}
		// get all languages from folder
		List<String> langs = getLanguages();
		// langs = new ArrayList<String>();
		// langs.add("fr");
		for (String lang : langs) {
			System.out.println(lang);
			System.out.println("-------------------------");
			Properties ps = load(lang);
			for (String key : list) {
				if (!ps.containsKey(key)) {
					System.err.println(key + " is missing in langfile");
				}
			}
			for (Object obj : ps.keySet()) {
				String key = (String) obj;
				if (key.startsWith("gate.")) continue;
				// check if the langfile key is in the list
				if (!list.contains(key)) {
					System.err.println("key '" + key + "' is not specified");
				}
			}
		}
	}

	public static void addGate(String langGate, String type, String key, String value) {
		if (!langGate.equals(lang) && !langGate.equals(ALL)) return;
		prop.setProperty("gate." + type + "." + key, value);
	}

	public static void add(String slang, String key, String value) {
		if (!slang.equals(lang) && !slang.equals(ALL)) return;
		prop.setProperty(key, value);
	}

}
