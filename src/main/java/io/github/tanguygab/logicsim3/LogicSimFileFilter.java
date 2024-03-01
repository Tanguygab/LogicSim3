package io.github.tanguygab.logicsim3;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

public class LogicSimFileFilter extends FileFilter {

	private Hashtable<String, LogicSimFileFilter> filters = new Hashtable<>();
	private String description = null;
	private String fullDescription = null;

	public LogicSimFileFilter() {}

	public LogicSimFileFilter(String extension, String description) {
		if (extension != null) addExtension(extension);
		if (description != null) setDescription(description);
	}

	public boolean accept(File f) {
		if (f == null) return false;
		if (f.isDirectory()) return true;
		String extension = getExtension(f);
        return extension != null && filters.get(extension) != null;
    }

	public String getExtension(File f) {
		if (f == null) return null;
		String filename = f.getName();
		int i = filename.lastIndexOf('.');
		return i > 0 && i < filename.length() - 1
				? filename.substring(i + 1).toLowerCase()
				: null;
	}

	public void addExtension(String extension) {
		if (filters == null) filters = new Hashtable<>(5);
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	public String getDescription() {
		if (fullDescription != null) return fullDescription;
		if (description != null) return description;
		fullDescription = "(";
		// build the description from the extension list
		Enumeration<String> extensions = filters.keys();
		if (extensions != null) {
			fullDescription += "." + extensions.nextElement();
			while (extensions.hasMoreElements())
				fullDescription += ", ." + extensions.nextElement();
		}
		fullDescription += ")";
		return fullDescription;
	}

	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

}
