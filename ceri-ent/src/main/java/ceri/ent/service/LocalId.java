package ceri.ent.service;

/**
 * Simple id manager for local instances.
 */
public class LocalId {
	private long nextId = 0;
	
	public LocalId() {
		this(0);
	}
	
	public LocalId(long nextId) {
		this.nextId = nextId;
	}
	
	public void taken(long id) {
		if (id >= nextId) nextId = id + 1;
	}
	
	public long checkId() {
		return nextId;
	}
	
	public long takeId() {
		return nextId++;
	}
	
}
