package ceri.process.nmcli;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.collection.Maps;
import ceri.common.text.Regex;
import ceri.common.text.ToString;

/**
 * Output from <b>nmcli show id [id]</b>.
 *
 * <pre>
 * connection.id:                          eth2
 * connection.uuid:                        186053d4-9369-4a4e-87b8-d1f9a419f985
 * connection.stable-id:                   --
 * connection.type:                        802-3-ethernet
 * connection.interface-name:              eth2
 * connection.autoconnect:                 yes
 * GENERAL.STATE:                          activated
 * GENERAL.VPN:                            no
 * </pre>
 */
public class ConShowIdResult {
	public static final ConShowIdResult NULL = builder().build();
	private static final Pattern NAME_VALUE_SPLIT = Pattern.compile("(.*?):\\s*(.*)");
	private static final String CONNECTION_ID = "connection.id";
	private static final String CONNECTION_UUID = "connection.uuid";
	private static final String CONNECTION_TYPE = "connection.type";
	private static final String CONNECTION_INTERFACE_NAME = "connection.interface-name";
	private static final String GENERAL_STATE = "GENERAL.STATE";
	private static final String GENERAL_VPN = "GENERAL.VPN";
	private static final String YES = "yes";
	private static final String NONE = "--";
	public final Map<String, String> values;

	public static ConShowIdResult fromOutput(String output) {
		Builder b = builder();
		for (String line : Regex.Split.LINE.array(output)) {
			var m = Regex.match(NAME_VALUE_SPLIT, line);
			if (m.hasMatch()) b.value(m.group(1), m.group(2));
		}
		return b.build();
	}

	public static ConShowIdResult of(Map<String, String> values) {
		return builder().values(values).build();
	}

	public static class Builder {
		final Map<String, String> values = Maps.link();

		Builder() {}

		public Builder value(String key, String value) {
			values.put(key, value);
			return this;
		}

		public Builder values(Map<String, String> values) {
			this.values.putAll(values);
			return this;
		}

		public ConShowIdResult build() {
			return new ConShowIdResult(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	ConShowIdResult(Builder builder) {
		values = Map.copyOf(builder.values);
	}

	public boolean isNull() {
		return values.isEmpty();
	}

	public String connectionId() {
		return value(CONNECTION_ID);
	}

	public String connectionUuid() {
		return value(CONNECTION_UUID);
	}

	public String connectionType() {
		return value(CONNECTION_TYPE);
	}

	public String connectionInterfaceName() {
		return value(CONNECTION_INTERFACE_NAME);
	}

	public String generalState() {
		return value(GENERAL_STATE);
	}

	public boolean generalVpn() {
		return value(GENERAL_VPN).equals(YES);
	}

	public String value(String key) {
		String value = values.getOrDefault(key, "");
		return NONE.equals(value) ? "" : value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConShowIdResult)) return false;
		ConShowIdResult other = (ConShowIdResult) obj;
		if (!Objects.equals(values, other.values)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, values);
	}

}
