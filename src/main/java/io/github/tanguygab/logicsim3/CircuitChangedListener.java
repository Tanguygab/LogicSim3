package io.github.tanguygab.logicsim3;

import java.awt.Point;

public interface CircuitChangedListener {

	void changedCircuit();

	void changedStatusText(String text);

	void changedZoomPos(double zoom, Point pos);

	void setAction(int action);

	void needsRepaint(CircuitPart circuitPart);
}
