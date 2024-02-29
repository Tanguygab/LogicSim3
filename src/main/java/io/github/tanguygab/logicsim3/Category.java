package io.github.tanguygab.logicsim3;

import java.util.ArrayList;

public class Category {
	String title;
	ArrayList<io.github.tanguygab.logicsim3.Gate> gates = new ArrayList<io.github.tanguygab.logicsim3.Gate>();

	public Category(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public ArrayList<io.github.tanguygab.logicsim3.Gate> getGates() {
		return gates;
	}

	public void addGate(Gate g) {
		gates.add(g);
	}

	@Override
	public String toString() {
		String s = "[Category: " + title + "/#gates: " + gates.size();
		s += "]";
		return s;
	}
}
