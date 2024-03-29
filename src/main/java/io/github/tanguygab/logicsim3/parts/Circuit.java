package io.github.tanguygab.logicsim3.parts;

import io.github.tanguygab.logicsim3.LSRepaintListener;
import lombok.Getter;
import lombok.Setter;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Vector;

/**
 * all parts that belong to the circuit
 *
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */

public class Circuit implements LSRepaintListener, Serializable {

	private static final long serialVersionUID = 3458986578856078326L;

	@Getter private final Vector<CircuitPart> parts = new Vector<>();
	@Setter private LSRepaintListener repaintListener;

	public void clear() {
		parts.clear();
	}

	public void addGate(Gate gate) {
		parts.add(gate);
		gate.setRepaintListener(this);
	}

	public boolean addWire(Wire newWire) {
		// only add a wire if there is not a wire from<->to
		for (CircuitPart part : parts) {
			if (part instanceof Wire) {
				Wire w = (Wire) part;
				if (w.getTo() == newWire.getTo() && w.getFrom() == newWire.getFrom()) {
					// don't add
					return false;
				}
			}
		}
		parts.add(newWire);
		newWire.setRepaintListener(this);
		return true;
	}

//	private CircuitPart[] findPartsAt(Class<?> clazz, int x, int y) {
//		Vector<CircuitPart> findParts = new Vector<CircuitPart>();
//		for (CircuitPart part : parts) {
//			if (part instanceof Gate) {
//				Gate g = (Gate) part;
//				CircuitPart cp = g.findPartAt(x, y);
//				if (cp != null && (clazz == null || (clazz != null && cp.getClass().equals(clazz))))
//					findParts.add(cp);
//			}
//			if (part instanceof Wire) {
//				Wire w = (Wire) part;
//				CircuitPart cp = w.findPartAt(x, y);
//				if (cp != null && (clazz == null || (clazz != null && cp.getClass().equals(clazz))))
//					findParts.add(cp);
//			}
//		}
//		return findParts.toArray(new CircuitPart[findParts.size()]);
//	}

	public Vector<Gate> getGates() {
		Vector<Gate> gates = new Vector<>();
		for (CircuitPart part : parts)
			if (part instanceof Gate)
				gates.add((Gate) part);
		return gates;
	}

	public Vector<Wire> getWires() {
		Vector<Wire> wires = new Vector<>();
		for (CircuitPart part : parts)
			if (part instanceof Wire)
				wires.add((Wire) part);
		return wires;
	}

	// what is this even used for?
	public void simulate() {
	}

	public void selectAll() {
		deselectAll();
		parts.forEach(CircuitPart::select);
	}

	public void deselectAll() {
		parts.forEach(CircuitPart::deselect);
	}

	public boolean isModule() {
		return parts.stream().anyMatch(part->part instanceof MODIN);
	}

	public boolean isPartAtCoordinates(int x, int y) {
		return parts.stream().anyMatch(part->part.getX() == x && part.getY() == y);
	}

	public CircuitPart findPartAt(int x, int y) {
		for (Gate g : getGates()) {
			CircuitPart cp = g.findPartAt(x, y);
			if (cp != null) return cp;
		}
		for (Wire w : getWires()) {
			CircuitPart cp = w.findPartAt(x, y);
			if (cp != null) return cp;
		}
		return null;
	}

	public CircuitPart[] getSelected() {
		Vector<CircuitPart> parts = new Vector<>();
		for (Gate g : getGates()) addPart(parts,g);

		for (Wire w : getWires()) {
			addPart(parts,w);
			if (w.getFrom() instanceof WirePoint) addPart(parts,w.getFrom());
			if (w.getTo() instanceof WirePoint) addPart(parts,w.getTo());
			for (WirePoint wp : w.getPoints()) addPart(parts,wp);
		}
		return parts.toArray(new CircuitPart[0]);
	}
	private void addPart(Vector<CircuitPart> parts, CircuitPart part) {
		if (part.isSelected() && !parts.contains(part))
			parts.add(part);
	}

	public boolean remove(CircuitPart[] parts) {
		if (parts.length == 0) return false;

		for (CircuitPart part : parts) {
			if (part instanceof Gate) {
				Gate g = (Gate) part;
				removeGate(g);
			} else if (part instanceof Wire) {
				Wire w = (Wire) part;
				w.disconnect();
				this.parts.remove(part);
			}
		}
		return true;
	}

	public void removeGate(Gate g) {
		if (g == null) throw new RuntimeException("cannot remove a non-gate gate is null");
		if (g instanceof MODIN || g instanceof MODOUT) return;
		// 1. check all wires if they are connected to that gate
//		for (Pin p : g.pins) {
//			if (p.isConnected()) {
//				for (LSLevelListener l : p.getListeners()) {
//					// must be a wire
//					if (l instanceof Wire) {
//						// 2. delete wire OR put a wirepoint at its end
//						Wire w = (Wire) l;
//						WirePoint wp = new WirePoint(p.getX(), p.getY());
//						p.removeLevelListener(w);
//						w.removeLevelListener(p);
//						if (p.equals(w.getTo())) {
//							w.setTo(wp);
//						} else {
//							w.setFrom(wp);
//						}
//						w.connect(wp);
//						// parts.remove(w);
//
//					}
//				}
//			}
//		}
		for (CircuitPart part : parts) {
			if (!(part instanceof Wire)) continue;
			Wire wire = (Wire) part;
			removeGate(g,wire.getTo(),wire);
			removeGate(g,wire.getFrom(),wire);
		}
		// checkWires();
		parts.remove(g);
    }
	private void removeGate(Gate gate, CircuitPart part, Wire wire) {
		if (!(part instanceof Pin)) return;
		Pin pin = (Pin) part;
		if (pin.parent != gate) return;
		wire.removeLevelListener(pin);
		pin.removeLevelListener(wire);
		WirePoint wp = new WirePoint(pin.getX(), pin.getY());
		wire.setFrom(wp);
		wire.connect(wp);
	}

	public void removeGateIdx(int idx) {
		Gate g = (Gate) parts.get(idx);
        removeGate(g);
    }

//	public Gate findGateById(String fromGateId) {
//		for (CircuitPart p : parts)
//			if (p.getId().equals(fromGateId))
//				return (Gate) p;
//		return null;
//	}

	@Override
	public void needsRepaint(CircuitPart circuitPart) {
		// forward
		if (repaintListener != null)
			repaintListener.needsRepaint(circuitPart);
	}

	public void setGates(Vector<Gate> gates) {
		for (Gate gate : gates) {
			addGate(gate);
			gate.setRepaintListener(this);
		}
		fireRepaint();
	}

	public void setWires(Vector<Wire> wires) {
		for (Wire wire : wires) addWire(wire);
		// checkWires();
		fireRepaint();
	}

	/**
	 * don't know for what that was...
	 */
//	private void checkWires() {
//		// check if there is at least one wire at any
//		// WirePoint-position
//		for (Wire w : getWires()) {
//			for (WirePoint wp : w.getPoints()) {
//				wp.show = false;
//			}
//		}
//
//		for (Wire w : getWires()) {
//			for (WirePoint wp : w.getPoints()) {
//				CircuitPart[] parts = findPartsAt(WirePoint.class, wp.getX(), wp.getY());
//				if (parts.length > 1) {
//					for (CircuitPart part : parts) {
//						WirePoint wirepoint = (WirePoint) part;
//						wirepoint.show = true;
//					}
//				}
//				parts = findPartsAt(Wire.class, wp.getX(), wp.getY());
//				if (parts.length > 0) {
//					wp.show = true;
//					// add a wirepoint at that position in every part of parts
//					for (CircuitPart part : parts) {
//						Wire wire = (Wire) part;
//						wire.addPointFitting(wp.getX(), wp.getY());
//					}
//				}
//			}
//		}
//		this.needsRepaint(null);
//	}

	private void fireRepaint() {
		if (repaintListener != null)
			repaintListener.needsRepaint(null);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Gate g : getGates()) s.append("\n").append(g);
		for (Wire w : getWires()) s.append("\n").append(w);

		return "Circuit:" + CircuitPart.indent(s.toString(), 3);
	}

	public CircuitPart[] findParts(Rectangle2D selectRect) {
		Vector<CircuitPart> findParts = new Vector<>();
		for (CircuitPart p : parts) {
			Rectangle2D r = p.getBoundingBox();
			double height = r.getHeight();
			if (selectRect.contains(r.getX(), r.getY(), r.getWidth(), height == 0 ? height+0.0001 : height)) {
				p.select();
				findParts.add(p);
			}
		}
		return findParts.toArray(new CircuitPart[0]);
	}

	public void reset() {
		for (CircuitPart p : getGates())
			p.reset();
	}

	public void remove(CircuitPart part) {
		parts.remove(part);
	}

	public Wire getUnfinishedWire() {
		for (CircuitPart part : parts) {
			if (part instanceof Wire && ((Wire) part).isNotFinished())
				return (Wire) part;
		}
		return null;
	}

	/**
	 * replace a wirepoint by a pin
	 *
     */
	public void checkWirePoint(WirePoint wp) {
		// check for Pin
		int px = wp.getX();
		int py = wp.getY();
		CircuitPart cp = findPartAt(px, py);
        if (!(cp instanceof Pin)) return;

		Pin p = (Pin) cp;
		// wirepoint will be replaced by pin
		// get corresponding wire
		for (Wire w : getWires()) {
			if (wp == w.getFrom()) {
				// found wire
				wp.removeLevelListener(w);
				w.removeLevelListener(wp);
				w.setFrom(p);
				w.connect(p);
				return;
			}
			if (wp == w.getTo()) {
				// found wire
				wp.removeLevelListener(w);
				w.removeLevelListener(wp);
				w.setTo(p);
				w.connect(p);
				return;
			}
		}
	}

	public boolean isEmpty() {
		return parts.isEmpty();
	}

	public Rectangle getBoundingBox() {
		Rectangle2D r2d = null;
		for (CircuitPart part : parts) {
			Rectangle r2 = part.getBoundingBox();
			Rectangle2D r22d = new Rectangle2D.Double(r2.x, r2.y, r2.width, r2.height);
			if (r2d == null) r2d = (Rectangle2D) r22d.clone();
			r2d.add(r22d);
		}
        assert r2d != null;
        return new Rectangle((int) r2d.getX(), (int) r2d.getY(), (int) r2d.getWidth(), (int) r2d.getHeight());
	}

}