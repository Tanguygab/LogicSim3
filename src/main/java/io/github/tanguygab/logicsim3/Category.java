package io.github.tanguygab.logicsim3;

import io.github.tanguygab.logicsim3.parts.Gate;

import java.util.ArrayList;
import java.util.List;

public class Category {

	private final String title;
	private final List<Gate> gates = new ArrayList<>();

	public Category(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public List<Gate> getGates() {
		return gates;
	}

	public void addGate(Gate g) {
		gates.add(g);
	}

	@Override
	public String toString() {
        return "[Category: " + title + "/#gates: " + gates.size() + "]";
	}
}
