package ceri.process.nmcli;

import java.util.List;
import java.util.Map;
import ceri.common.collection.CollectionUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.log.process.parse.Columns;

/**
 * One line from nmcli con status
 * 
 * <pre>
 * NAME                  UUID               DEVICES    DEFAULT  VPN   MASTER-PATH                                 
 * Wired connection 1    8X-4X-4X-4X-12X    eth0       yes      no    --
 * </pre>
 */
public class ConStatusItem {
	private static final String NAME_COLUMN = "NAME";
	private static final String UUID_COLUMN = "UUID";
	private static final String DEVICES_COLUMN = "DEVICES";
	private static final String DEFAULT_COLUMN = "DEFAULT";
	private static final String VPN_COLUMN = "VPN";
	private static final String MASTER_PATH_COLUMN = "MASTER-PATH";
	public final String name;
	public final String uuid;
	public final String devices;
	public final String def;
	public final String vpn;
	public final String masterPath;

	public static List<ConStatusItem> fromOutput(String output) {
		List<Map<String, String>> lines = Columns.parseOutputWithHeader(output);
		return CollectionUtil.toList(ConStatusItem::fromNameValues, lines);
	}

	private static ConStatusItem fromNameValues(Map<String, String> map) {
		return builder().name(map.get(NAME_COLUMN)).uuid(map.get(UUID_COLUMN)).devices(
			map.get(DEVICES_COLUMN)).def(map.get(DEFAULT_COLUMN)).vpn(map.get(VPN_COLUMN))
			.masterPath(map.get(MASTER_PATH_COLUMN)).build();
	}

	public static class Builder {
		String name;
		String uuid;
		String devices;
		String def;
		String vpn;
		String masterPath;

		Builder() {}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder uuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public Builder devices(String devices) {
			this.devices = devices;
			return this;
		}

		public Builder def(String def) {
			this.def = def;
			return this;
		}

		public Builder vpn(String vpn) {
			this.vpn = vpn;
			return this;
		}

		public Builder masterPath(String masterPath) {
			this.masterPath = masterPath;
			return this;
		}

		public ConStatusItem build() {
			return new ConStatusItem(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	ConStatusItem(Builder builder) {
		name = builder.name;
		uuid = builder.uuid;
		devices = builder.devices;
		def = builder.def;
		vpn = builder.vpn;
		masterPath = builder.masterPath;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, uuid, devices, def, vpn, masterPath);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConStatusItem)) return false;
		ConStatusItem other = (ConStatusItem) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(uuid, other.uuid)) return false;
		if (!EqualsUtil.equals(devices, other.devices)) return false;
		if (!EqualsUtil.equals(def, other.def)) return false;
		if (!EqualsUtil.equals(vpn, other.vpn)) return false;
		if (!EqualsUtil.equals(masterPath, other.masterPath)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, uuid, devices, def, vpn, masterPath)
			.toString();
	}

}
