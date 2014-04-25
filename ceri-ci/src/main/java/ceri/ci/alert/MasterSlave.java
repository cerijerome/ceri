package ceri.ci.alert;

public class MasterSlave {
	private final Type type;
	public final int index;
	public final String name;
	
	public static enum Type {
		master, slave
	}
	
	private MasterSlave(Type type, int index) {
		this.type = type;
		this.index = index;
		name = type == Type.master ? type.name() : type.name() + index;
	}

	public boolean isMaster() {
		return type == Type.master;
	}
	
	public static MasterSlave master() {
		return new MasterSlave(Type.master, 0);
	}
	
	public static MasterSlave slave(int index) {
		if (index < 1) throw new IllegalArgumentException("Index must be > 0: " + index);
		return new MasterSlave(Type.slave, index);
	}

	public static MasterSlave createFromEnv() {
		String value = System.getProperty(Type.slave.name());
		if (value == null) return master();
		int index = Integer.parseInt(value);
		return slave(index);
	}
	
}
