package ceri.zwave.veralite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public enum Capability {
	L(0x80), // Listens
	R(0x40), // Routes
	B(0x10), // Beams
	RS(0x01), // Routing slave
	W1(0x00); // Requires beaming

	// 209,140,0,4,17,1,L,R,B,RS,|38,39,112,114,115,117,134,
	// 209 => 1101 0001
	// 201,4,0,3,17,0,L,R,|38,39,117,
	// 201 => 1100 1001
	// 211,156,0,4,16,1,L,R,B,RS,|37,39,114,115,117,119,134,
	// 211 => 1101 0011
	
	private static final Pattern COMMA_SPLIT = Pattern.compile("\\s*,\\s*");
	public final int value;

	private Capability(int value) {
		this.value = value;
	}

	public static int value(Collection<Capability> capabilities) {
		int value = 0;
		for (Capability capability : capabilities)
			value |= capability.value;
		return value;
	}

	public static Set<Capability> create(String list) {
		Set<Capability> capabilities = new HashSet<>();
		for (String item : COMMA_SPLIT.split(list))
			capabilities.add(Capability.valueOf(item));
		return capabilities;
	}

	public static Set<Capability> create(int value) {
		Set<Capability> capabilities = new HashSet<>();
		for (Capability capability : Capability.values())
			if ((value & capability.value) != 0) capabilities.add(capability);
		return capabilities;
	}

}
