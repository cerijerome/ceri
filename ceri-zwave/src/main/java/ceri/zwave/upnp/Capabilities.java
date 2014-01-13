package ceri.zwave.upnp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulation of http://wiki.micasaverde.com/index.php/ZWave_Command_Classes
 * 209,140,0,4,17,1,L,R,B,RS,|38,39,112,114,115,117,134,
 * 209 => 1101 0001
 * 201,4,0,3,17,0,L,R,|38,39,117,
 * 201 => 1100 1001
 * 211,156,0,4,16,1,L,R,B,RS,|37,39,114,115,117,119,134,
 * 211 => 1101 0011
 */
public class Capabilities {
	private static final int PROTOCOL_INFO_COUNT = 6;
	private static final String PIPE_SPLIT = "\\s*\\|\\s*";
	private static final String COMMA_SPLIT = "\\s*\\,\\s*";
	public final int capability;
	public final int security;
	public final int reserved = 0;
	public final int basicDeviceClass;
	public final int genericDeviceClass;
	public final int specificDeviceClass;
	public final Collection<Flag> flags;
	public final Collection<CommandClass> commandClasses;
	private final int hashCode;

	public static void main(String[] args) {
		String s = "209,140,0,4,17,1,L,R,B,RS,|38,39,112,114,115,117,134,";
		System.out.println(s);
		Capabilities cp = create(s);
		System.out.println(cp.toString());
	}

	public static enum Flag {
		L,	// Listens
		R,	// Routes
		B,	// Beams
		RS,	// Routing slave
		W1;	// Requires beaming
	}

	public static class Builder {
		int capability;
		int security;
		int basicDeviceClass;
		int genericDeviceClass;
		int specificDeviceClass;
		final Collection<Flag> flags = new HashSet<>();
		final Collection<CommandClass> commandClasses = new HashSet<>();

		Builder() {}
		
		public Builder capability(int capability) {
			this.capability = capability;
			return this;
		}

		public Builder security(int security) {
			this.security = security;
			return this;
		}

		public Builder basicDeviceClass(int basicDeviceClass) {
			this.basicDeviceClass = basicDeviceClass;
			return this;
		}

		public Builder genericDeviceClass(int genericDeviceClass) {
			this.genericDeviceClass = genericDeviceClass;
			return this;
		}

		public Builder specificDeviceClass(int specificDeviceClass) {
			this.specificDeviceClass = specificDeviceClass;
			return this;
		}

		public Builder flag(Flag flag) {
			flags.add(flag);
			return this;
		}

		public Builder commandClass(int id) {
			return commandClass(CommandClass.byId(id));
		}

		public Builder commandClass(CommandClass commandClass) {
			if (commandClass == null) throw new NullPointerException("CommandClass cannot be null");
			if (commandClass != CommandClass.NO_OPERATION) commandClasses.add(commandClass);
			return this;
		}

		public Capabilities build() {
			return new Capabilities(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Capabilities(Builder builder) {
		capability = builder.capability;
		security = builder.security;
		basicDeviceClass = builder.basicDeviceClass;
		genericDeviceClass = builder.genericDeviceClass;
		specificDeviceClass = builder.specificDeviceClass;
		flags = Collections.unmodifiableCollection(new TreeSet<>(builder.flags));
		commandClasses = Collections.unmodifiableCollection(new TreeSet<>(builder.commandClasses));
		hashCode =
			HashCoder.hash(capability, security, basicDeviceClass, genericDeviceClass,
				specificDeviceClass, flags, commandClasses);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Capabilities)) return false;
		Capabilities capabilities = (Capabilities)obj;
		if (capability != capabilities.capability) return false;
		if (security != capabilities.security) return false;
		if (basicDeviceClass != capabilities.basicDeviceClass) return false;
		if (genericDeviceClass != capabilities.genericDeviceClass) return false;
		if (specificDeviceClass != capabilities.specificDeviceClass) return false;
		if (!EqualsUtil.equals(flags,  capabilities.flags)) return false;
		return EqualsUtil.equals(commandClasses, capabilities.commandClasses);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(capability).append(',');
		b.append(security).append(',');
		b.append(reserved).append(',');
		b.append(basicDeviceClass).append(',');
		b.append(genericDeviceClass).append(',');
		b.append(specificDeviceClass).append(',');
		for (Flag flag : flags)
			b.append(flag.name()).append(',');
		if (!commandClasses.isEmpty()) b.append('|');
		for (CommandClass commandClass : commandClasses)
			b.append(commandClass.id).append(',');
		return b.toString();
	}

	public static Capabilities create(String s) {
		Builder builder = builder();
		String[] ss = s.split(PIPE_SPLIT);
		if (ss.length == 0) throw new IllegalArgumentException("No capabilities: " + s);
		addProtocolInfo(builder, ss[0]);
		if (ss.length > 1) addCommandClasses(builder, ss[1]);
		return builder.build();
	}

	private static void addProtocolInfo(Builder builder, String protocolInfo) {
		String[] valuesStr = protocolInfo.split(COMMA_SPLIT);
		if (valuesStr.length < PROTOCOL_INFO_COUNT) throw new IllegalArgumentException(
			"Must have at least " + PROTOCOL_INFO_COUNT + " values: " + protocolInfo);
		int i = 0;
		builder.capability(Integer.parseInt(valuesStr[i++]));
		builder.security(Integer.parseInt(valuesStr[i++]));
		i++; // 0 reserved
		builder.basicDeviceClass(Integer.parseInt(valuesStr[i++]));
		builder.genericDeviceClass(Integer.parseInt(valuesStr[i++]));
		builder.specificDeviceClass(Integer.parseInt(valuesStr[i++]));
		while (i < valuesStr.length)
			builder.flag(Flag.valueOf(valuesStr[i++]));
	}

	private static void addCommandClasses(Builder builder, String commandClassIdsStr) {
		for (String commandClassIdStr : commandClassIdsStr.split(COMMA_SPLIT)) {
			builder.commandClass(Integer.parseInt(commandClassIdStr));
		}
	}

}
