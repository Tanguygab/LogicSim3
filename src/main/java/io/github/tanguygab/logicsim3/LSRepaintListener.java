package io.github.tanguygab.logicsim3;

import io.github.tanguygab.logicsim3.parts.CircuitPart;

public interface LSRepaintListener {

	void needsRepaint(CircuitPart source);

}