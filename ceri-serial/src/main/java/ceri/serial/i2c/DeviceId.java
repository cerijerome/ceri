package ceri.serial.i2c;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import ceri.common.data.ByteUtil;
import ceri.common.util.HashCoder;

public class DeviceId {
	public static final int BYTES = 3;
	private static final Map<Integer, Company> companyIds = assignedManufacturerIds();
	private static final int MANUFACTURER_MASK = ByteUtil.maskInt(12);
	private static final int MANUFACTURER_BIT = 12;
	private static final int PART_MASK = ByteUtil.maskInt(9);
	private static final int PART_BIT = 3;
	private static final int REVISION_MASK = ByteUtil.maskInt(3);
	public final int manufacturer;
	public final int part;
	public final int revision;

	public static void main(String[] args) {
		System.out.println(decode(0));
	}

	/**
	 * Companies with assigned manufacturer ids.
	 */
	public static enum Company {
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
		validateRange(manufacturer, 0, MANUFACTURER_MASK);
		validateRange(part, 0, MANUFACTURER_MASK);
		validateRange(revision, 0, MANUFACTURER_MASK);
		return new DeviceId(manufacturer, part, revision);
	}

	private DeviceId(int manufacturer, int part, int revision) {
		this.manufacturer = manufacturer;
		this.part = part;
		this.revision = revision;
	}

	public int encode() {
		return (manufacturer << MANUFACTURER_BIT) | (part << PART_BIT) | revision;
	}

	public byte[] encodeBytes() {
		return ByteUtil.toMsb(encode(), BYTES);
	}

	public Company company() {
		return companyIds.getOrDefault(manufacturer, Company.unknown);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(manufacturer, part, revision);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DeviceId)) return false;
		DeviceId other = (DeviceId) obj;
		if (manufacturer != other.manufacturer) return false;
		if (part != other.part) return false;
		if (revision != other.revision) return false;
		return true;
	}

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		Company company = company();
		return company == Company.unknown ?
			String.format("%s(0x%x,0x%x,0x%x)", name, manufacturer, part, revision) :
			String.format("%s(%s,0x%x,0x%x)", name, company, part, revision);
	}

	private static Map<Integer, Company> assignedManufacturerIds() {
		Map<Integer, Company> map = new LinkedHashMap<>();
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
		return Collections.unmodifiableMap(map);
	}

}
