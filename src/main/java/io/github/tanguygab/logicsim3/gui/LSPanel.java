package io.github.tanguygab.logicsim3.gui;

import io.github.tanguygab.logicsim3.*;
import io.github.tanguygab.logicsim3.parts.*;
import lombok.Setter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.*;

import javax.swing.event.MouseInputAdapter;

public class LSPanel extends Viewer implements Printable, CircuitChangedListener, LSRepaintListener {
	public class LogicSimPainterGraphics implements Painter {
		@Override
		public void paint(Graphics2D g2, AffineTransform at, int w, int h) {
			// set anti-aliasing
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2.transform(at);
			if (LSProperties.getInstance().getPropertyBoolean(LSProperties.PAINTGRID, true) && scaleX > 0.7f) {
				int startX = CircuitPart.round((int) Math.round(getTransformer().screenToWorldX(0)));
				int startY = CircuitPart.round((int) Math.round(getTransformer().screenToWorldY(0)));
				int endX = (int) getTransformer().screenToWorldX(w + 9);
				int endY = (int) getTransformer().screenToWorldY(h + 9);
				g2.setColor(gridColor);
				Path2D grid = new Path2D.Double();
				g2.setStroke(new BasicStroke(1));
				for (int x = startX; x < endX; x += 10) {
					for (int y = startY; y < endY; y += 10) {
						grid.moveTo(x, y);
						grid.lineTo(x, y);
					}
				}
				g2.draw(grid);
			}

			draw(g2);

			// redraw selected parts so that there are in the foreground
			for (CircuitPart part : circuit.getSelected()) {
				part.draw(g2);
			}

			if (currentAction == ACTION_SELECT) {
				g2.setStroke(dashed);
				g2.setColor(Color.blue);
				if (selectRect != null)
					g2.draw(selectRect);
			}
		}
	}

	/**
	 * A class summarizing the mouse interaction for this viewer.
	 */
	private class MouseControl extends MouseInputAdapter
			implements MouseListener, MouseMotionListener, MouseWheelListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			e = convertToWorld(e);
			if (currentAction == ACTION_SELECT) {
				notifyZoomPos(scaleX, new Point(e.getX(), e.getY()));
				// previousPoint is the start point of the selection box
				Point currentMouse = new Point(e.getX(), e.getY());
				if (currentMouse.x < previousPoint.x || currentMouse.y < previousPoint.y)
					selectRect.setFrameFromDiagonal(currentMouse, previousPoint);
				else selectRect.setFrameFromDiagonal(previousPoint, currentMouse);
				repaint();
				return;
			}

			CircuitPart[] parts = circuit.getSelected();
			if (parts.length == 0) {
				// drag world
				int dx = e.getX() - previousPoint.x;
				int dy = e.getY() - previousPoint.y;
				translate(dx, dy);
				return;
			}
			// don't drag in simulation mode
			if (Simulation.getInstance().isRunning()) return;

			// drag parts
			notifyZoomPos(scaleX, new Point(e.getX(), e.getY()));

			for (CircuitPart part : parts) {
				part.mouseDragged(e);

				if (!LSProperties.getInstance().getPropertyBoolean(LSProperties.AUTOWIRE, true)) continue;
				// check if currentpart is a gate and if any output touches another part's input
				// pin
				if (!(part instanceof Gate)) continue;
				Gate gate = (Gate) part;
				for (Pin pin : gate.getPins()) {
					// autowire unconnected pins only
					if (pin.isConnected()) continue;
					int x = pin.getX();
					int y = pin.getY();
					for (Gate g : circuit.getGates()) {
						CircuitPart cp = g.findPartAt(x, y);
						if (!(cp instanceof Pin)) continue;
						Pin p = (Pin) cp;
						if (pin.isInput() != p.isOutput()) continue;
						// put new wire between pin and p
						Wire w = pin.isOutput() ? new Wire(pin, p) : new Wire(p, pin);
						w.deselect();
						if (circuit.addWire(w)) {
							p.connect(w);
							pin.connect(w);
						}
					}
				}
			}
			fireCircuitChanged();
		}

		private void setPoint(int x, int y) {
			previousPoint.setLocation(x, y);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			e = convertToWorld(e);

			int rx = CircuitPart.round(e.getX());
			int ry = CircuitPart.round(e.getY());
			setPoint(rx, ry);
			notifyZoomPos(scaleX, new Point(rx, ry));

			CircuitPart[] parts = circuit.getSelected();
			if (parts.length != 1 || !(parts[0] instanceof Wire)) return;
			Wire wire = (Wire) parts[0];
			if (!wire.isNotFinished()) return;
			if (e.isShiftDown()) {
				// pressed SHIFT while moving and drawing wire
				WirePoint wp = wire.getLastPoint();
				int lastx = wp.getX();
				int lasty = wp.getY();
				if (Math.abs(rx - lastx) < Math.abs(ry - lasty))
					rx = lastx;
				else ry = lasty;
			}
			// the selected wire is unfinished - force draw
			wire.setTempPoint(new Point(rx, ry));
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			e = convertToWorld(e);
			setPoint(e.getX(), e.getY());
			int rx = CircuitPart.round(e.getX());
			int ry = CircuitPart.round(e.getY());

			boolean simRunning = Simulation.getInstance().isRunning();
			boolean expertMode = LSProperties.MODE_EXPERT
					.equals(LSProperties.getInstance().getProperty(LSProperties.MODE, LSProperties.MODE_NORMAL));

			if (simRunning) {
				currentAction = ACTION_NONE;
				//fireStatusText(NOTHING);
			}

			if (currentAction == ACTION_SELECT) {
				selectRect = new Rectangle2D.Double(e.getX(), e.getY(), 0, 0);
			}

			CircuitPart[] parts = circuit.getSelected();
			CircuitPart part = circuit.findPartAt(e.getX(), e.getY());

			if (currentAction == ACTION_DELPOINT && part instanceof WirePoint && part.parent instanceof Wire) {
				part.parent.mousePressed(new LSMouseEvent(e, ACTION_DELPOINT, null));
			}
			// if (part != null)
			// System.out.println(cp.toStringAll());
			if (!simRunning && part instanceof Pin && !e.isAltDown() && currentAction == ACTION_NONE) {
				// we start a new wire if the pin we clicked is an output OR
				// if we are in expert mode
				if (((Pin) part).isOutput() || expertMode)
					currentAction = ACTION_ADDWIRE;
			}

			switch (currentAction) {
				case ACTION_ADDWIRE: {
					CircuitPart p = part == null ? new WirePoint(rx, ry) : part;
					Wire newWire;

					if (part instanceof Wire) {
						// put a WirePoint at this position
						Wire wire = (Wire) part;
						int pt = wire.isAt(e.getX(), e.getY());
						wire.insertPointAfter(pt, rx, ry);
						part = wire.findPartAt(rx, ry);
						p = part;
					}

					newWire = new Wire(p, null);
					if (circuit.addWire(newWire)) p.connect(newWire);

					circuit.deselectAll();
					newWire.select();

					fireStatusText(I18N.tr(Lang.WIREEDIT));
					fireCircuitChanged();
					currentAction = ACTION_EDITWIRE;
					return;
				}
				case ACTION_EDITWIRE: {
					editWire(e,circuit.getUnfinishedWire(),part,rx,ry,expertMode);
					repaint();
					return;
				}
			}


			if (part == null) {
				// empty space has been clicked
				circuit.deselectAll();
				repaint();
				fireStatusText("");
				return;
			}

			// check if the part is a connector
			if (part instanceof Pin && !e.isAltDown() && !simRunning) {
				Pin pin = ((Pin) part);
				fireStatusText(I18N.tr(Lang.PIN) + " (" + part.getId() + ")");
				// modify input (inverted or high or low or revert to normal type)
				if (pin.isInput() && currentAction >= 10 && currentAction <= 13) {
					// 1. if we clicked on an input modificator
					pin.setLevelType(currentAction);
					pin.changedLevel(new LSLevelEvent(new Wire(null, null), pin.level, true));
					currentAction = ACTION_NONE;
					fireStatusText(NOTHING);
					fireCircuitChanged();
					return;
				}
			}

			parts = mousePressed(e,part,parts,simRunning);

			part.mousePressed(new LSMouseEvent(e, currentAction, parts));
			currentAction = ACTION_NONE;
			fireStatusText(NOTHING);
			repaint();
		}

		private void editWire(MouseEvent e, Wire wire, CircuitPart part, int rx, int ry, boolean expert) {
			if (part == null) {
				// empty space clicked
				wire.addPoint(rx, ry);
				return;
			}
			if (part instanceof Pin) {
				Pin pin = ((Pin) part);
				if (!expert && pin.isOutput()) return;

				wire.setTo(pin);
				wire.getTo().connect(wire);
				wire.finish();
				currentAction = ACTION_NONE;
				fireStatusText(NOTHING);
				fireCircuitChanged();
				return;
			}
			if (part instanceof Wire) {
				Wire clickedWire = (Wire) part;
				if (clickedWire.equals(wire)) return;
				if (!expert) return;
				int pt = clickedWire.isAt(e.getX(), e.getY());
				clickedWire.insertPointAfter(pt, rx, ry);
				part = clickedWire.findPartAt(rx, ry);

				wire.setTo(part);
				wire.getTo().connect(wire);
				wire.finish();
				currentAction = ACTION_NONE;
				fireStatusText(NOTHING);
				fireCircuitChanged();
				return;
			}
			if (part instanceof WirePoint) {
				WirePoint clickedWP = (WirePoint) part;
				// check if the clicked point belongs to another wire
				if (clickedWP.parent.equals(wire)) {
					// the clicked wirepoint belongs to the editing wire...
					// so check if we clicked the last point of the wire to finish it
					WirePoint lp = wire.getLastPoint();
					if (lp.getX() == rx && lp.getY() == ry) {
						// it is the same point as the last one
						wire.removeLastPoint();
						wire.setTo(new WirePoint(rx, ry));
						wire.getTo().connect(wire);
						wire.finish();
						currentAction = ACTION_NONE;
						fireStatusText(NOTHING);
						fireCircuitChanged();
					}
					// shorten the wire and delete circles
					else wire.addPoint(rx, ry);
					return;
				}
				// wirepoint belongs to another wire
				if (!expert) return;
				wire.setTo(clickedWP);
				wire.getTo().connect(wire);
				wire.finish();
				currentAction = ACTION_NONE;
				fireStatusText(NOTHING);
				fireCircuitChanged();
			}
		}

		private CircuitPart[] mousePressed(MouseEvent e, CircuitPart part, CircuitPart[] parts, boolean simRunning) {
			if (part instanceof Gate) {
				String type = ((Gate) part).type;
				if (part instanceof Module)
					fireStatusText(I18N.tr(Lang.MODULE) + " (" + part.getId() + ")");
				else fireStatusText(I18N.getString(type, I18N.DESCRIPTION) + " (" + part.getId() + ")");

				if (parts.length > 0 && !simRunning && !part.isSelected()) {
					// check if we clicked a new gate
					part.select();
					if (!e.isShiftDown()) circuit.deselectAll();
					parts = circuit.getSelected();
				}
				return parts;
			}
			if (part instanceof Wire && !simRunning) {
				String desc = I18N.tr(Lang.WIRE);
				String s = part.getProperty(CircuitPart.TEXT);
				if (s != null) desc += ": " + s;
				desc += " (" + part.getId() + ")";
				fireStatusText(desc);
				circuit.deselectAll();
				part.select();
				return parts;
			}
			if (part instanceof WirePoint) {
				fireStatusText(I18N.tr(Lang.WIREPOINT) + " (" + part.getId() + ")");
				circuit.deselectAll();
				part.select();
			}
			return parts;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			e = convertToWorld(e);
			int x = e.getPoint().x;
			int y = e.getPoint().y;
			requestFocusInWindow();

			if (currentAction == ACTION_SELECT) {
				CircuitPart[] parts = circuit.findParts(selectRect);
				for (CircuitPart part : parts) {
					if (!(part instanceof Wire)) continue;
					Wire w = (Wire) part;
					if (w.getTo() instanceof WirePoint) w.getTo().select();
					if (w.getFrom() instanceof WirePoint) w.getFrom().select();
				}
				fireStatusText(String.format(I18N.tr(Lang.PARTSSELECTED), parts.length));
				currentAction = ACTION_NONE;
				fireStatusText(NOTHING);
				selectRect = null;
				repaint();
				return;
			}

			for (CircuitPart part : circuit.getSelected()) {
				part.mouseReleased(x, y);
				if (part instanceof WirePoint && part.parent == null) {
					WirePoint wp = (WirePoint) part;
					circuit.checkWirePoint(wp);
				}
			}
			CircuitPart cp = circuit.findPartAt(e.getX(), e.getY());
			if (cp != null) cp.mouseReleased(x, y);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoomBy(e.getX(), e.getY(), -e.getWheelRotation() * zoomingSpeed);
			notifyZoomPos(scaleX, new Point(e.getX(), e.getY()));
		}
	}

	private static final short ACTION_NONE = 0;

	protected static final short ACTION_ADDWIRE = 0x50;
	private static final short ACTION_EDITWIRE = 0x51;

	public static final short ACTION_ADDPOINT = 0x52;
	public static final short ACTION_DELPOINT = 0x53;
	public static final short ACTION_SELECT = 1;

	private final static Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 10 }, 0);
	private static final Color gridColor = Color.black;
	private static final long serialVersionUID = -6414072156700139318L;

	public static final String MSG_ABORTED = "MSG_DESELECT_BUTTONS";

	public static final String NOTHING = "NOTHING";

	@Setter
	private CircuitChangedListener changeListener;
	public Circuit circuit = new Circuit();

	// current mode
	private int currentAction;

    /**
	 * used for track selection, is one endpoint of a rectangle
	 */
	private Rectangle2D selectRect;

	private final List<CircuitPart> copiedParts = new ArrayList<>();
	private final List<CircuitPart> lastActions = new ArrayList<>();

	public LSPanel() {
        Dimension panelSize = new Dimension(1280, 1024);
        setSize(panelSize);
		setPreferredSize(panelSize);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		circuit.setRepaintListener(this);

		// setZoomingSpeed(0.02);
		setPainter(new LogicSimPainterGraphics());

		MouseControl mouseControl = new MouseControl();
		addMouseListener(mouseControl);
		addMouseMotionListener(mouseControl);
		addMouseWheelListener(mouseControl);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				onKeyPressed(e);
			}
		});
	}

	@Override
	public void changedCircuit() {
		if (changeListener != null)
			changeListener.changedCircuit();
		repaint();
	}

	@Override
	public void changedStatusText(String text) {
		// just transfer to parent
		changeListener.changedStatusText(text);
	}

	@Override
	public void changedZoomPos(double zoom, Point pos) {}

	public void clear() {
		circuit.deselectAll();
		currentAction = 0;
		circuit.clear();
		repaint();
	}

	private MouseEvent convertToWorld(MouseEvent e) {
		int x = (int) (getTransformer().screenToWorldX(e.getX()));
		int y = (int) (getTransformer().screenToWorldY(e.getY()));
		// int x = (int) Math.round(getTransformer().screenToWorldX(e.getX()));
		// int y = (int) Math.round(getTransformer().screenToWorldY(e.getY()));

        return new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y,
				e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	public void doPrint() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		if (printJob.printDialog())
			try {printJob.print();}
			catch (Exception e) {e.printStackTrace();}
	}

	public void draw(Graphics2D g2) {
		// draw panels first
		for (CircuitPart gate : circuit.getGates()) {
			gate.draw(g2);
		}
		// then wires
		for (CircuitPart wire : circuit.getWires()) {
			wire.draw(g2);
		}
	}

	/**
	 * mirror a part if selected
	 */
	public void mirrorSelected() {
		for (CircuitPart part : circuit.getSelected()) {
			if (part instanceof Gate) {
				((Gate) part).mirror();
			}
		}
		repaint();
	}

	/**
	 * check for escape, delete and space key
	 *
     */
	private void onKeyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (e.isControlDown() && onCtrlKeyPressed(keyCode)) return;

		CircuitPart[] parts = circuit.getSelected();
		if (parts.length == 0) return;

		switch (keyCode) {
			case KeyEvent.VK_ESCAPE: {
				if (currentAction == ACTION_EDITWIRE) {
					Wire wire = (Wire) parts[0];
					int pointsOfWire = wire.removeLastPoint();
					if (pointsOfWire == 0) {
						currentAction = ACTION_NONE;
						// delete wire
						wire.disconnect();
						circuit.remove(wire);
						circuit.deselectAll();
						fireStatusText(MSG_ABORTED);
					}
				} else if (currentAction == ACTION_ADDPOINT
						|| currentAction == ACTION_DELPOINT
						|| currentAction == ACTION_SELECT) {
					currentAction = ACTION_NONE;
					fireStatusText(MSG_ABORTED);
				} else circuit.deselectAll();
				repaint();
				return;
			}
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT: {
				int dx = 0;
				int dy = 0;
				if (keyCode == KeyEvent.VK_UP) dy -= 10;
				else if (keyCode == KeyEvent.VK_DOWN) dy += 10;
				else if (keyCode == KeyEvent.VK_LEFT) dx -= 10;
				else dx += 10;
				for (CircuitPart part : parts) part.moveBy(dx, dy);
				fireCircuitChanged();
				return;
			}
			case KeyEvent.VK_DELETE:
			case KeyEvent.VK_BACK_SPACE: {
				if (!circuit.remove(parts)) return;
				currentAction = ACTION_NONE;
				fireStatusText(NOTHING);
				fireStatusText(I18N.tr(Lang.PARTSDELETED, String.valueOf(parts.length)));
				fireCircuitChanged();
				repaint();
				return;
			}
			case KeyEvent.VK_SPACE: {
				CircuitPart[] selected = circuit.getSelected();
				for (CircuitPart part : selected)
					if (part instanceof Gate)
						((Gate)part).interact();
				repaint();
				return;
			}
			case KeyEvent.VK_C:
				if (!e.isControlDown()) return;
				copiedParts.clear();
				copiedParts.addAll(Arrays.asList(parts));
		}
	}

	private boolean onCtrlKeyPressed(int keyCode) {
		switch (keyCode) {
			case KeyEvent.VK_V:
				if (copiedParts.isEmpty()) break;
				circuit.deselectAll();
				Map<Gate,Gate> gates = new HashMap<>();
				Map<WirePoint,WirePoint> wirePoints = new HashMap<>();

				copiedParts.forEach(part -> {
					if (part instanceof WirePoint) {
						WirePoint wp = (WirePoint) part;
						WirePoint newWp = new WirePoint(part.getX(),part.getY(),wp.show);
						newWp.select();
						wirePoints.put(wp,newWp);
						return;
					}
					if (!(part instanceof Gate)) return;

					Gate gate = GateLoaderHelper.create((Gate) part);
					circuit.addGate(gate);
					gate.setProperties(part.getProperties());
					gate.select();

					gate.moveTo(part.getX(),part.getY());
					gate.moveBy(20,20);
					gates.put((Gate) part,gate);
				});
				copiedParts.forEach(part -> {
					if (!(part instanceof Wire)) return;

					Wire wire = (Wire) part;
					Wire newWire = new Wire(wire.getX()+20,wire.getY()+20);
					circuit.addWire(newWire);

					Pin from = wire.getFrom() instanceof Pin ? (Pin) wire.getFrom() : null;
					Pin to = wire.getTo() instanceof Pin ? (Pin) wire.getTo() : null;

					wirePoints.forEach((wp,newWp)->{
						if (wire.getFrom() == wp) newWire.setFrom(newWp);
						if (wire.getTo() == wp) newWire.setTo(newWp);
					});

					gates.forEach((gate,newGate)->{
						if (gate.getPins().contains(from)) {
							Pin pin = newGate.getPin(gate.getPins().indexOf(from));
							newWire.setFrom(pin);
							pin.connect(newWire);
						}
						if (gate.getPins().contains(to)) {
							Pin pin = newGate.getPin(gate.getPins().indexOf(to));
							newWire.setTo(pin);
							pin.connect(newWire);
						}

					});
				});
				currentAction = ACTION_NONE;
				fireStatusText("MSG_PASTE_SELECTION");
				fireCircuitChanged();
				break;
			case KeyEvent.VK_Z:
				System.out.println("Undo");
				break;
			case KeyEvent.VK_Y:
				System.out.println("Redo");
				break;
			default: return false;
		}
		return true;
	}


	@Override
	public void needsRepaint(CircuitPart circuitPart) {
		repaint();
	}

	private void fireCircuitChanged() {
		if (changeListener != null) {
			changeListener.changedCircuit();
		}
		repaint();
	}

	private void fireStatusText(String msg) {
		if (changeListener != null) {
			changeListener.changedStatusText(msg);
		}
	}

	private void notifyZoomPos(double zoom, Point mousePos) {
		if (changeListener != null)
			changeListener.changedZoomPos(zoom, mousePos);
	}

	@Override
	public int print(Graphics g, PageFormat pf, int pi) {
		if (pi >= 1) return Printable.NO_SUCH_PAGE;
		draw((Graphics2D) g);
		return Printable.PAGE_EXISTS;
	}

	/**
	 * rotate a gate if selected
	 */
	public void rotateSelected() {
		for (CircuitPart part : circuit.getSelected()) {
			if (part instanceof Gate) {
				((Gate) part).rotate();
			}
		}
		fireCircuitChanged();
	}

	public void setAction(CircuitPart g) {
		if (g == null) return;
        circuit.deselectAll();
        // place new gate
        int posX = (int) -offsetX / 10 * 10 + 20;
        int posY = (int) -offsetY / 10 * 10 + 20;
        while (circuit.isPartAtCoordinates(posX, posY)) {
            posX += 40;
            posY += 40;
        }
        g.moveTo(posX, posY);
        circuit.addGate((Gate) g);
        g.select();

        fireStatusText("MSG_ADD_NEW_GATE");
        fireCircuitChanged();
        repaint();
    }

	public void setAction(int actionNumber) {
		Lang lang;
		switch (actionNumber) {
			case ACTION_ADDPOINT: lang = Lang.ADDPOINT;break;
			case ACTION_DELPOINT: lang = Lang.REMOVEPOINT;break;
			case Pin.HIGH: lang = Lang.INPUTHIGH;break;
			case Pin.LOW: lang = Lang.INPUTLOW;break;
			case Pin.NORMAL: lang = Lang.INPUTNORM;break;
			case Pin.INVERTED: lang = Lang.INPUTINV;break;
			default: lang = null;
		}
		if (lang != null) fireStatusText(I18N.tr(lang));
		currentAction = actionNumber;
	}


	private void zoom(double amount) {
		int x = (int) getTransformer().screenToWorldX(getWidth() / 2.);
		int y = (int) getTransformer().screenToWorldY(getHeight() / 2.);
		zoomBy(x, y, amount);
		notifyZoomPos(scaleX, new Point(x, y));

	}
	/**
	 * less zoom
	 */
	public void zoomOut() {
		zoom(-0.5f);
	}

	/**
	 * more zoom
	 */
	public void zoomIn() {
		zoom(0.5f);
	}

	/**
	 * zoom to all
	 */
	public void zoomAll() {
		Rectangle r = circuit.getBoundingBox();

		int zx = getWidth() / r.width;
		int zy = getHeight() / r.height;
		double zf = Math.min(zx, zy);

		int cx = r.x + r.width / 2;
		int cy = r.y + r.height / 2;

		// calculate the circuit's center point
		int x = (int) getTransformer().screenToWorldX(cx);
		int y = (int) getTransformer().screenToWorldY(cy);

		// calculate the current screen center point
		int curX = (int) getTransformer().screenToWorldX(getWidth() / 2.);
		int curY = (int) getTransformer().screenToWorldY(getHeight() / 2.);

		int dx = curX - x;
		int dy = curY - y;
		translate(dx, dy);
		zoomTo(x, y, zf);
		notifyZoomPos(scaleX, new Point(x, y));
	}

	public void gateSettings() {
		CircuitPart[] parts = circuit.getSelected();
		if (parts.length != 1 || !(parts[0] instanceof Gate)) return;
		Gate g = (Gate) parts[0];
		g.showPropertiesUI(this);
		fireCircuitChanged();
	}

}