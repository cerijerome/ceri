package ceri.zwave.veralite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Encapsulation of http://wiki.micasaverde.com/index.php/ZWave_Command_Classes
 */
public class Capabilities {
	private static final int PROTOCOL_INFO_COUNT = 6;
	private static final String PIPE_SPLIT = "\\s*\\|\\s*";
	private static final String COMMA_SPLIT = "\\s*\\,\\s*";
	public Set<Capability> capability;
	public final int security;
	public final int basicDeviceClass;
	public final int genericDeviceClass;
	public final int specificDeviceClass;
	private final Collection<CommandClass> commandClasses;

	public static void main(String[] args) {
		String s = "209,140,0,4,17,1,L,R,B,RS,|38,39,112,114,115,117,134,";
		Capabilities cp = create(s);
		System.out.println(cp.toString());
	}

	public static class Builder {
		final Collection<CommandClass> commandClasses = new HashSet<>();
		final Set<Capability> capability = new HashSet<>();
		int security;
		int basicDeviceClass;
		int genericDeviceClass;
		int specificDeviceClass;

		public Builder capability(int value) {
			this.capability.addAll(Capability.create(value));
			return this;
		}

		public Builder capability(Capability...capability) {
			Collections.addAll(this.capability, capability);
			return this;
		}

		public Builder commandClass(CommandClass commandClass) {
			if (commandClass == null) throw new NullPointerException("CommandClass cannot be null");
			if (commandClass != CommandClass.NO_OPERATION) commandClasses.add(commandClass);
			return this;
		}

		public Builder commandClass(int id) {
			return commandClass(CommandClass.byId(id));
		}

		public Capabilities build() {
			return new Capabilities(this);
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
		commandClasses = Collections.unmodifiableCollection(new TreeSet<>(builder.commandClasses));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(capability).append(',');
		b.append(security).append(',');
		b.append(0).append(',');
		b.append(basicDeviceClass).append(',');
		b.append(genericDeviceClass).append(',');
		b.append(specificDeviceClass).append(',');
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
	}

	private static void addCommandClasses(Builder builder, String commandClassIdsStr) {
		for (String commandClassIdStr : commandClassIdsStr.split(COMMA_SPLIT)) {
			builder.commandClass(Integer.parseInt(commandClassIdStr));
		}
	}

}
