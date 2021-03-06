package ceri.zwave.upnp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;
import ceri.common.text.StringUtil;

/**
 * Encapsulation of http://wiki.micasaverde.com/index.php/ZWave_Command_Classes
 * 209,140,0,4,17,1,L,R,B,RS,|38,39,112,114,115,117,134, 209 => 1101 0001
 * 201,4,0,3,17,0,L,R,|38,39,117, 201 => 1100 1001
 * 211,156,0,4,16,1,L,R,B,RS,|37,39,114,115,117,119,134, 211 => 1101 0011
 */
public class Capabilities {
	private static final int PROTOCOL_INFO_COUNT = 6;
	private static final String PIPE_SPLIT = "\\s*\\|\\s*";
	public final int capability;
	public final int security;
	public final int reserved = 0;
	public final int basicDeviceClass;
	public final int genericDeviceClass;
	public final int specificDeviceClass;
	public final Collection<Flag> flags;
	public final Collection<CommandClass> commandClasses;

	public enum Flag {
		L, // Listens
		R, // Routes
		B, // Beams
		RS, // Routing slave
		W1; // Requires beaming
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
	}

	@Override
	public int hashCode() {
		return Objects.hash(capability, security, basicDeviceClass, genericDeviceClass,
			specificDeviceClass, flags, commandClasses);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Capabilities)) return false;
		Capabilities capabilities = (Capabilities) obj;
		if (capability != capabilities.capability) return false;
		if (security != capabilities.security) return false;
		if (basicDeviceClass != capabilities.basicDeviceClass) return false;
		if (genericDeviceClass != capabilities.genericDeviceClass) return false;
		if (specificDeviceClass != capabilities.specificDeviceClass) return false;
		if (!Objects.equals(flags, capabilities.flags)) return false;
		return Objects.equals(commandClasses, capabilities.commandClasses);
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
		Collection<String> values = StringUtil.commaSplit(protocolInfo);
		if (values.size() < PROTOCOL_INFO_COUNT) throw new IllegalArgumentException(
			"Must have at least " + PROTOCOL_INFO_COUNT + " values: " + protocolInfo);
		Iterator<String> i = values.iterator();
		builder.capability(Integer.parseInt(i.next()));
		builder.security(Integer.parseInt(i.next()));
		i.next(); // 0 reserved
		builder.basicDeviceClass(Integer.parseInt(i.next()));
		builder.genericDeviceClass(Integer.parseInt(i.next()));
		builder.specificDeviceClass(Integer.parseInt(i.next()));
		while (i.hasNext())
			builder.flag(Flag.valueOf(i.next()));
	}

	private static void addCommandClasses(Builder builder, String commandClassIdsStr) {
		for (String commandClassIdStr : StringUtil.commaSplit(commandClassIdsStr)) {
			builder.commandClass(Integer.parseInt(commandClassIdStr));
		}
	}

}
