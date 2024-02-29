package io.github.tanguygab.logicsim3;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Vector;

/**
 * all parts that belong to the circuit
 * 
 * @author Andreas Tetzl
 * @author Peter Gabriel
 * @version 2.0
 */

public class Circuit implements io.github.tanguygab.logicsim3.LSRepaintListener {
	static final long serialVersionUID = 3458986578856078326L;

	Vector<io.github.tanguygab.logicsim3.CircuitPart> parts;

	private io.github.tanguygab.logicsim3.LSRepaintListener repaintListener;

	public Circuit() {
		parts = new Vector<io.github.tanguygab.logicsim3.CircuitPart>();
	}

	public void clear() {
		parts.clear();
	}

	public void addGate(io.github.tanguygab.logicsim3.Gate gate) {
		parts.add(gate);
		gate.setRepaintListener(this);
	}

	public boolean addWire(io.github.tanguygab.logicsim3.Wire newWire) {
		// only add a wire if there is not a wire from<->to
		for (io.github.tanguygab.logicsim3.CircuitPart part : parts) {
			if (part instanceof io.github.tanguygab.logicsim3.Wire) {
				io.github.tanguygab.logicsim3.Wire w = (io.github.tanguygab.logicsim3.Wire) part;
				if (w.getTo().equals(newWire.getTo()) && w.getFrom().equals(newWire.getFrom())) {
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

	public Vector<io.github.tanguygab.logicsim3.CircuitPart> getParts() {
		return parts;
	}

	public Vector<io.github.tanguygab.logicsim3.Gate> getGates() {
		Vector<io.github.tanguygab.logicsim3.Gate> gates = new Vector<io.github.tanguygab.logicsim3.Gate>();
		for (io.github.tanguygab.logicsim3.CircuitPart part : parts) {
			if (part instanceof io.github.tanguygab.logicsim3.Gate) {
				gates.add((io.github.tanguygab.logicsim3.Gate) part);
			}
		}
		return gates;
	}

	public Vector<io.github.tanguygab.logicsim3.Wire> getWires() {
		Vector<io.github.tanguygab.logicsim3.Wire> wires = new Vector<io.github.tanguygab.logicsim3.Wire>();
		for (io.github.tanguygab.logicsim3.CircuitPart part : parts) {
			if (part instanceof io.github.tanguygab.logicsim3.Wire) {
				wires.add((io.github.tanguygab.logicsim3.Wire) part);
			}
		}
		return wires;
	}

	public void simulate() {
	}

	public void setRepaintListener(LSRepaintListener listener) {
		this.repaintListener = listener;
	}

	public void selectAll() {
		deselectAll();
		for (io.github.tanguygab.logicsim3.CircuitPart p : parts) {
			p.select();
		}
	}

	// Alle Gatter und zugehörige Wires deaktivieren
	public void deselectAll() {
		for (io.github.tanguygab.logicsim3.CircuitPart p : parts) {
			p.deselect();
		}
	}

	public boolean isModule() {
		for (io.github.tanguygab.logicsim3.CircuitPart g : parts) {
			if (g instanceof MODIN) {
				return true;
			}
		}
		return false;
	}

	public boolean isPartAtCoordinates(int x, int y) {
		for (io.github.tanguygab.logicsim3.CircuitPart p : parts) {
			if (p.getX() == x && p.getY() == y)
				return true;
		}
		return false;
	}

	public io.github.tanguygab.logicsim3.CircuitPart findPartAt(int x, int y) {
		for (io.github.tanguygab.logicsim3.Gate g : getGates()) {
			io.github.tanguygab.logicsim3.CircuitPart cp = g.findPartAt(x, y);
			if (cp != null)
				return cp;
		}
		for (io.github.tanguygab.logicsim3.Wire w : getWires()) {
			io.github.tanguygab.logicsim3.CircuitPart cp = w.findPartAt(x, y);
			if (cp != null)
				return cp;
		}
		return null;
	}

	public io.github.tanguygab.logicsim3.CircuitPart[] getSelected() {
		Vector<io.github.tanguygab.logicsim3.CircuitPart> selParts = new Vector<io.github.tanguygab.logicsim3.CircuitPart>();
		for (io.github.tanguygab.logicsim3.Gate g : getGates()) {
			if (g.selected && !selParts.contains(g))
				selParts.add(g);
		}
		for (io.github.tanguygab.logicsim3.Wire w : getWires()) {
			if (w.selected && !selParts.contains(w))
				selParts.add(w);
			if (w.getFrom() instanceof io.github.tanguygab.logicsim3.WirePoint)
				if (w.getFrom().isSelected() && !selParts.contains(w.getFrom()))
					selParts.add(w.getFrom());
			if (w.getTo() instanceof io.github.tanguygab.logicsim3.WirePoint)
				if (w.getTo().isSelected() && !selParts.contains(w.getTo()))
					selParts.add(w.getTo());
			for (io.github.tanguygab.logicsim3.WirePoint wp : w.getPoints())
				if (wp.isSelected() && !selParts.contains(wp))
					selParts.add(wp);
		}
		return selParts.toArray(new io.github.tanguygab.logicsim3.CircuitPart[selParts.size()]);
	}

	public boolean remove(io.github.tanguygab.logicsim3.CircuitPart[] parts) {
		if (parts.length == 0)
			return false;

		for (io.github.tanguygab.logicsim3.CircuitPart part : parts) {
			if (part instanceof io.github.tanguygab.logicsim3.Gate) {
				io.github.tanguygab.logicsim3.Gate g = (io.github.tanguygab.logicsim3.Gate) part;
				removeGate(g);
			} else if (part instanceof io.github.tanguygab.logicsim3.Wire) {
				io.github.tanguygab.logicsim3.Wire w = (io.github.tanguygab.logicsim3.Wire) part;
				w.disconnect(null);
				this.parts.remove(part);
			}
		}
		return true;
	}

	public boolean removeGate(io.github.tanguygab.logicsim3.Gate g) {
		if (g == null)
			throw new RuntimeException("cannot remove a non-gate gate is null");
		if (g.type.equals("modin") || g.type.equals("modout"))
			return false;
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
		for (Iterator<io.github.tanguygab.logicsim3.CircuitPart> iter = parts.iterator(); iter.hasNext();) {
			io.github.tanguygab.logicsim3.CircuitPart part = iter.next();
			if (!(part instanceof io.github.tanguygab.logicsim3.Wire))
				continue;
			io.github.tanguygab.logicsim3.Wire w = (io.github.tanguygab.logicsim3.Wire) part;
			if (w.getTo() != null && w.getTo() instanceof io.github.tanguygab.logicsim3.Pin) {
				io.github.tanguygab.logicsim3.Pin p = (io.github.tanguygab.logicsim3.Pin) w.getTo();
				if (p.parent == g) {
					w.removeLevelListener(p);
					p.removeLevelListener(w);
					// w.disconnect(null);
					// iter.remove();
					io.github.tanguygab.logicsim3.WirePoint wp = new io.github.tanguygab.logicsim3.WirePoint(p.getX(), p.getY());
					w.setTo(wp);
					w.connect(wp);
				}
			}
			if (w.getFrom() != null && w.getFrom() instanceof io.github.tanguygab.logicsim3.Pin) {
				io.github.tanguygab.logicsim3.Pin p = (io.github.tanguygab.logicsim3.Pin) w.getFrom();
				if (p.parent == g) {
					w.removeLevelListener(p);
					p.removeLevelListener(w);
					// w.disconnect(null);
					// iter.remove();
					io.github.tanguygab.logicsim3.WirePoint wp = new io.github.tanguygab.logicsim3.WirePoint(p.getX(), p.getY());
					w.setFrom(wp);
					w.connect(wp);
				}
			}
		}
		// checkWires();
		parts.remove(g);
		return true;
	}

	public boolean removeGateIdx(int idx) {
		io.github.tanguygab.logicsim3.Gate g = (io.github.tanguygab.logicsim3.Gate) parts.get(idx);
		return removeGate(g);
	}

	public io.github.tanguygab.logicsim3.Gate findGateById(String fromGateId) {
		for (io.github.tanguygab.logicsim3.CircuitPart p : parts) {
			if (p.getId().equals(fromGateId))
				return (io.github.tanguygab.logicsim3.Gate) p;
		}
		return null;
	}

	@Override
	public void needsRepaint(io.github.tanguygab.logicsim3.CircuitPart circuitPart) {
		// forward
		if (repaintListener != null)
			repaintListener.needsRepaint(circuitPart);
	}

	public void setGates(Vector<io.github.tanguygab.logicsim3.Gate> gates) {
		for (io.github.tanguygab.logicsim3.Gate gate : gates) {
			addGate(gate);
			gate.setRepaintListener(this);
		}
		fireRepaint(null);
	}

	public void setWires(Vector<io.github.tanguygab.logicsim3.Wire> wires) {
		for (io.github.tanguygab.logicsim3.Wire wire : wires) {
			addWire(wire);
		}
		// checkWires();

		fireRepaint(null);
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

	private void fireRepaint(io.github.tanguygab.logicsim3.CircuitPart source) {
		if (repaintListener != null)
			repaintListener.needsRepaint(source);
	}

	@Override
	public String toString() {
		String s = "";
		for (Gate g : getGates()) {
			s += "\n" + g;
		}
		for (io.github.tanguygab.logicsim3.Wire w : getWires()) {
			s += "\n" + w;
		}
		return s = "Circuit:" + io.github.tanguygab.logicsim3.CircuitPart.indent(s, 3);
	}

	public io.github.tanguygab.logicsim3.CircuitPart[] findParts(Rectangle2D selectRect) {
		Vector<io.github.tanguygab.logicsim3.CircuitPart> findParts = new Vector<io.github.tanguygab.logicsim3.CircuitPart>();
		for (io.github.tanguygab.logicsim3.CircuitPart p : parts) {
			if (selectRect.contains(p.getBoundingBox())) {
				p.select();
				findParts.add(p);
			}
		}
		return findParts.toArray(new io.github.tanguygab.logicsim3.CircuitPart[findParts.size()]);
	}

	public void reset() {
		for (io.github.tanguygab.logicsim3.CircuitPart p : getGates())
			p.reset();
	}

	public void remove(io.github.tanguygab.logicsim3.CircuitPart part) {
		parts.remove(part);
	}

	public io.github.tanguygab.logicsim3.Wire getUnfinishedWire() {
		for (io.github.tanguygab.logicsim3.CircuitPart part : parts) {
			if (part instanceof io.github.tanguygab.logicsim3.Wire && ((io.github.tanguygab.logicsim3.Wire) part).isNotFinished())
				return (io.github.tanguygab.logicsim3.Wire) part;
		}
		return null;
	}

	/**
	 * replace a wirepoint by a pin
	 * 
	 * @param wp
	 */
	public void checkWirePoint(WirePoint wp) {
		// check for Pin
		int px = wp.getX();
		int py = wp.getY();
		io.github.tanguygab.logicsim3.CircuitPart cp = findPartAt(px, py);
		if (cp instanceof io.github.tanguygab.logicsim3.Pin) {
			io.github.tanguygab.logicsim3.Pin p = (Pin) cp;
			// wirepoint will be replaced by pin
			// get corresponding wire
			for (Wire w : getWires()) {
				if (wp.equals(w.getFrom())) {
					// found wire
					wp.removeLevelListener(w);
					w.removeLevelListener(wp);
					w.setFrom(p);
					w.connect(p);
					return;
				}
				if (wp.equals(w.getTo())) {
					// found wire
					wp.removeLevelListener(w);
					w.removeLevelListener(wp);
					w.setTo(p);
					w.connect(p);
					return;
				}
			}
		}
	}

	public boolean isEmpty() {
		return parts.size() == 0;
	}

	public Rectangle getBoundingBox() {
		Rectangle2D r2d = null;
		for (CircuitPart part : parts) {
			Rectangle r2 = part.getBoundingBox();
			Rectangle2D r22d = new Rectangle2D.Double(r2.x, r2.y, r2.width, r2.height);
			if (r2d == null)
				r2d = (Rectangle2D) r22d.clone();
			r2d.add(r22d);
		}
		return new Rectangle((int) r2d.getX(), (int) r2d.getY(), (int) r2d.getWidth(), (int) r2d.getHeight());
	}

}