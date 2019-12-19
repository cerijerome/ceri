package ceri.process.scutil;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.text.StringUtil.lines;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * One line from scutil --nc list.
 * 
 * <pre>
 * * (state) id protocol --> device "name" [protocol:type]
 * </pre>
 */
public class NcListItem {
	private static final Pattern DECODE_REGEX = Pattern
		.compile("(.) \\((.*)\\)\\s+(\\S+) (\\w+) --> (.*)\\s+\"(.*)\"\\s+\\[\\w+:(\\w+)\\]");
	private static final String ENABLED = "*";
	public final boolean enabled;
	public final NcServiceState state;
	public final String passwordHash;
	public final String protocol;
	public final String device;
	public final String name;
	public final String type;

	public static List<NcListItem> fromList(String output) {
		return toList(lines(output).stream().map(NcListItem::from).filter(Objects::nonNull));
	}

	public static NcListItem from(String line) {
		Matcher m = RegexUtil.found(DECODE_REGEX, line);
		if (m == null) return null;
		int i = 1;
		return NcListItem.builder().enabled(NcListItem.ENABLED.equals(m.group(i++)))
			.state(m.group(i++)).passwordHash(m.group(i++)).protocol(m.group(i++))
			.device(m.group(i++)).name(m.group(i++)).type(m.group(i++)).build();
	}

	public static class Builder {
		boolean enabled = false;
		NcServiceState state = NcServiceState.unknown;
		String passwordHash;
		String protocol;
		String device;
		String name;
		String type;

		Builder() {}

		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder state(String state) {
			return state(NcServiceState.from(state));
		}

		public Builder state(NcServiceState state) {
			this.state = state;
			return this;
		}

		public Builder passwordHash(String passwordHash) {
			this.passwordHash = passwordHash;
			return this;
		}

		public Builder protocol(String protocol) {
			this.protocol = protocol;
			return this;
		}

		public Builder device(String device) {
			this.device = device;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public NcListItem build() {
			return new NcListItem(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	NcListItem(Builder builder) {
		enabled = builder.enabled;
		state = builder.state;
		passwordHash = builder.passwordHash;
		protocol = builder.protocol;
		device = builder.device;
		name = builder.name;
		type = builder.type;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(enabled, state, passwordHash, protocol, device, name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NcListItem)) return false;
		NcListItem other = (NcListItem) obj;
		if (enabled != other.enabled) return false;
		if (!EqualsUtil.equals(state, other.state)) return false;
		if (!EqualsUtil.equals(passwordHash, other.passwordHash)) return false;
		if (!EqualsUtil.equals(protocol, other.protocol)) return false;
		if (!EqualsUtil.equals(device, other.device)) return false;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		return true;
	}

	@Override
	public String toString() {
		if (state == NcServiceState.noService) return state.toString();
		return String.format("%s %-16s %s %s --> %-10s %-32s [%s:%s]", enabled ? ENABLED : " ",
			"(" + state + ")", passwordHash, protocol, device, "\"" + name + "\"", protocol, type);
	}

}
