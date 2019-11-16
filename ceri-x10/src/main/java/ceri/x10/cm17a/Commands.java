package ceri.x10.cm17a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ceri.common.io.IoUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.x10.command.UnitCommand;
import ceri.x10.type.Address;
import ceri.x10.type.DimFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

/**
 * Stores the transmission sequences for device commands.
 */
public class Commands {
	private static final int BINARY = 2;
	private static final byte HEADER1 = (byte) 0xd5;
	private static final byte HEADER2 = (byte) 0xaa;
	private static final byte FOOTER = (byte) 0xad;
	private static final byte TRANSMISSION_LEN = 5;
	private static final String FILE_SUFFIX = "map";
	private final Map<Key, Short> commands;

	static class Key {
		public final House house;
		public final Unit unit;
		public final FunctionType type;
		private final int hashCode;

		public Key(House house, Unit unit, FunctionType type) {
			this.house = house;
			this.unit = unit;
			this.type = type;
			hashCode = HashCoder.hash(house, unit, type);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Key)) return false;
			Key other = (Key) obj;
			return house == other.house && EqualsUtil.equals(unit, other.unit) &&
				type == other.type;
		}

		@Override
		public String toString() {
			return house.name() + (unit == null ? "" : unit.index) + ":" + type.name();
		}
	}

	/**
	 * Constructor that loads the resource map.
	 */
	public Commands() throws IOException {
		commands = Collections.unmodifiableMap(loadCommands());
	}

	/**
	 * Returns the binary transmission sequence for an on/off command.
	 */
	public byte[] unit(UnitCommand command) {
		Short code = commands.get(new Key(command.house, command.unit, command.type));
		if (code == null) throw new UnsupportedOperationException(command.toString());
		return transmission(code);
	}

	/**
	 * Returns the binary transmission sequence for a dim/bright function (no unit code).
	 */
	public byte[] dim(DimFunction function) {
		Short code = commands.get(new Key(function.house, null, function.type));
		if (code == null) throw new UnsupportedOperationException(function.toString());
		return transmission(code);
	}

	private byte[] transmission(int code) {
		byte[] bytes = new byte[TRANSMISSION_LEN];
		int i = 0;
		bytes[i++] = HEADER1;
		bytes[i++] = HEADER2;
		bytes[i++] = (byte) (code >> 8 & 0xff);
		bytes[i++] = (byte) (code & 0xff);
		bytes[i++] = FOOTER;
		return bytes;
	}

	/**
	 * Loads commands from the resource map.
	 */
	private Map<Key, Short> loadCommands() throws IOException {
		Map<Key, Short> commandTable = new HashMap<>();
		String s = IoUtil.classResourceAsString(getClass(), FILE_SUFFIX);
		try (BufferedReader in = new BufferedReader(new StringReader(s))) {
			String nextLine;
			while ((nextLine = in.readLine()) != null) {
				String unit = nextLine.substring(0, 4).trim();
				String command = nextLine.substring(4, 16).trim();
				String code = nextLine.substring(16).trim();
				commandTable.put(key(unit, command), code(code));
			}
		}
		return commandTable;
	}
	
	private Key key(String unit, String command) {
		FunctionType type = FunctionType.valueOf(command);
		if (unit.length() == 1) return new Key(House.fromChar(unit.charAt(0)), null, type);
		Address address = Address.fromString(unit);
		return new Key(address.house, address.unit, type);
	}
	
	private Short code(String code) {
		return Integer.valueOf(code, BINARY).shortValue();
	}
	
}
