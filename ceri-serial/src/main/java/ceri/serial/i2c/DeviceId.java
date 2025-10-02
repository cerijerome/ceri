package ceri.serial.i2c;

import java.util.Map;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.data.ByteUtil;
import ceri.common.util.Validate;

public record DeviceId(int manufacturer, int part, int revision) {

	public static final DeviceId NONE = new DeviceId(0, 0, 0);
	public static final int BYTES = 3;
	private static final Map<Integer, Company> companyIds = assignedManufacturerIds();
	private static final int MANUFACTURER_MASK = ByteUtil.maskInt(12);
	private static final int MANUFACTURER_BIT = 12;
	private static final int PART_MASK = ByteUtil.maskInt(9);
	private static final int PART_BIT = 3;
	private static final int REVISION_MASK = ByteUtil.maskInt(3);

	/**
	 * Companies with assigned manufacturer ids.
	 */
	public enum Company {
		unknown, // fallback
		NXP_Semiconductor,
		Ramtron_International,
		Analog_Devices,
		STMicroelectronics,
		ON_Semiconductor,
		Sprintek_Corporation,
		ESPROS_Photonics_AG,
		Fujitsu_Semiconductor,
		Flir,
		O2Micro,
		Atmel;
	}

	public static DeviceId decode(int value) {
		int manufacturer = (value >>> MANUFACTURER_BIT) & MANUFACTURER_MASK;
		int part = (value >>> PART_BIT) & PART_MASK;
		int revision = value & REVISION_MASK;
		return new DeviceId(manufacturer, part, revision);
	}

	public static DeviceId of(int manufacturer, int part, int revision) {
		Validate.range(manufacturer, 0, MANUFACTURER_MASK);
		Validate.range(part, 0, PART_MASK);
		Validate.range(revision, 0, REVISION_MASK);
		return new DeviceId(manufacturer, part, revision);
	}

	public int encode() {
		return (manufacturer << MANUFACTURER_BIT) | (part << PART_BIT) | revision;
	}

	public byte[] encodeBytes() {
		return ByteUtil.toMsb(encode(), BYTES);
	}

	public Company company() {
		if (isNone()) return Company.unknown;
		return companyIds.getOrDefault(manufacturer, Company.unknown);
	}

	public boolean isNone() {
		return manufacturer == 0 && part == 0 && revision == 0;
	}

	@Override
	public String toString() {
		var name = getClass().getSimpleName();
		if (isNone()) return name + "(none)";
		var company = company();
		return company == Company.unknown ?
			String.format("%s(0x%x,0x%x,0x%x)", name, manufacturer, part, revision) :
			String.format("%s(%s,0x%x,0x%x)", name, company, part, revision);
	}

	private static Map<Integer, Company> assignedManufacturerIds() {
		var map = Maps.<Integer, Company>link();
		int i = 0;
		map.put(i++, Company.NXP_Semiconductor); // 0
		map.put(i++, Company.NXP_Semiconductor);
		map.put(i++, Company.NXP_Semiconductor);
		map.put(i++, Company.NXP_Semiconductor);
		map.put(i++, Company.Ramtron_International);
		map.put(i++, Company.Analog_Devices);
		map.put(i++, Company.STMicroelectronics);
		map.put(i++, Company.ON_Semiconductor);
		map.put(i++, Company.Sprintek_Corporation); // 8
		map.put(i++, Company.ESPROS_Photonics_AG);
		map.put(i++, Company.Fujitsu_Semiconductor);
		map.put(i++, Company.Flir);
		map.put(i++, Company.O2Micro);
		map.put(i++, Company.Atmel); // 13
		return Immutable.wrap(map);
	}
}
