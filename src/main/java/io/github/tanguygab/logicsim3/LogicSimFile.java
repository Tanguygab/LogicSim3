package io.github.tanguygab.logicsim3;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LogicSimFile {
	io.github.tanguygab.logicsim3.Circuit circuit = new Circuit();
	Map<String, String> info = new HashMap<String, String>();
	String fileName;
	public boolean changed = false;
	private Vector<String> errors = new Vector<String>();

	public LogicSimFile(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * extract the pure file name from an absolute path
	 * 
	 * @param fileName
	 * @return
	 */
	public String extractFileName() {
		File f = new File(fileName);
		String name = f.getName();
		// strip extension
		name = name.substring(0, name.lastIndexOf('.'));
		return name;
	}

	public Vector<io.github.tanguygab.logicsim3.Gate> getGates() {
		return circuit.getGates();
	}

	public void setGates(Vector<Gate> gates) {
		circuit.setGates(gates);
	}

	public void setWires(Vector<io.github.tanguygab.logicsim3.Wire> wires) {
		circuit.setWires(wires);
	}

	public Vector<Wire> getWires() {
		return circuit.getWires();
	}

	private String getKey(String key) {
		return info.containsKey(key) ? info.get(key) : null;
	}

	public void setLabel(String value) {
		info.put("label", value);
	}

	public void setDescription(String value) {
		info.put("description", value);
	}

	public String getLabel() {
		return getKey("label");
	}

	public String getDescription() {
		return getKey("description");
	}

	@Override
	public String toString() {
		String s = "File: ";
		if (fileName != null)
			s += fileName;
		if (circuit != null)
			s += " circuit: " + circuit.parts.size() + " parts";
		return s;
	}

	public void addError(String s) {
		errors.add(s);
	}

	public String getErrorString() {
		if (errors.size() == 0)
			return null;
		String s = "";
		for (String err : errors)
			s += " " + err;
		return s;
	}

}
