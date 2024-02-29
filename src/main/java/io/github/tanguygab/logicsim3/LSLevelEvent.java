package io.github.tanguygab.logicsim3;

public class LSLevelEvent {
	public io.github.tanguygab.logicsim3.CircuitPart source;
	public io.github.tanguygab.logicsim3.LSLevelListener target;

	public boolean level;

	public boolean force;

	public LSLevelEvent(io.github.tanguygab.logicsim3.CircuitPart source) {
		this.source = source;
		this.level = false;
		this.force = false;
	}

	public LSLevelEvent(io.github.tanguygab.logicsim3.CircuitPart source, boolean level) {
		this(source);
		this.level = level;
		this.force = false;
	}

	public LSLevelEvent(io.github.tanguygab.logicsim3.CircuitPart source, boolean level, boolean force) {
		this(source, level);
		this.force = force;
	}

	public LSLevelEvent(io.github.tanguygab.logicsim3.CircuitPart source, boolean level, boolean force, LSLevelListener target) {
		this(source, level, force);
		this.target = target;
	}

	public void setLevel(boolean level) {
		this.level = level;
	}

	@Override
	public String toString() {
		String s = "LevelEvt: (" + source.getClass().getSimpleName() + ") " + source.getId() + " is " + (level ? "HIGH" : "LOW") + " force: " + force;
		if (target != null)
			s += " --> " + ((CircuitPart) target).getId();
		return s;
	}
}
