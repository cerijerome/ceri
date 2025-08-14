package ceri.process.nmcli;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.process.Columns;
import ceri.common.text.Strings;
import ceri.common.text.ToString;

/**
 * One line from <b>nmcli con show</b> command.
 *
 * <pre>
 * NAME  UUID                                  TYPE      DEVICE
 * eth1  01fa0bf4-b6bd-484f-a9a3-2b10ff701dcd  ethernet  eth1
 * eth0  2e9f0cdd-ea2f-4b63-b146-3b9a897c9e45  ethernet  eth0
 * eth2  186053d4-9369-4a4e-87b8-d1f9a419f985  ethernet  eth2
 * </pre>
 */
public class ConShowItem {
	public static final ConShowItem NULL = new ConShowItem(null, null, null, null);
	private static final String NAME_COLUMN = "NAME";
	private static final String UUID_COLUMN = "UUID";
	private static final String TYPE_COLUMN = "TYPE";
	private static final String DEVICE_COLUMN = "DEVICE";
	public final String name;
	public final String uuid;
	public final String type;
	public final String device;

	public static List<ConShowItem> fromOutput(String output) {
		var lines = Strings.lines(output).toList();
		if (lines.size() <= 1) return List.of();
		var columns = Columns.fromFixedWidthHeader(lines.get(0));
		lines = lines.subList(1, lines.size());
		return Immutable.adaptList(line -> fromNameValues(columns.parseAsMap(line)), lines);
	}

	private static ConShowItem fromNameValues(Map<String, String> map) {
		String name = map.get(NAME_COLUMN);
		String uuid = map.get(UUID_COLUMN);
		String type = map.get(TYPE_COLUMN);
		String device = map.get(DEVICE_COLUMN);
		return new ConShowItem(name, uuid, type, device);
	}

	public static ConShowItem of(String name, String uuid, String type, String device) {
		return new ConShowItem(name, uuid, type, device);
	}

	private ConShowItem(String name, String uuid, String type, String device) {
		this.name = name;
		this.uuid = uuid;
		this.type = type;
		this.device = device;
	}

	public boolean isNull() {
		return name == null && uuid == null && type == null && device == null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uuid, type, device);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConShowItem)) return false;
		ConShowItem other = (ConShowItem) obj;
		if (!Objects.equals(name, other.name)) return false;
		if (!Objects.equals(uuid, other.uuid)) return false;
		if (!Objects.equals(type, other.type)) return false;
		if (!Objects.equals(device, other.device)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, name, uuid, type, device);
	}
}
