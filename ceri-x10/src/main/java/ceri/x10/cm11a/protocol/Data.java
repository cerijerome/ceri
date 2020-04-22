package ceri.x10.cm11a.protocol;

import static java.util.Map.entry;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.x10.type.Address;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

/**
 * Methods for converting between types and byte arrays for reading from/writing to the CM11A
 * controller.
 */
public class Data {
	private static final int MINUTES_IN_HOUR = (int) TimeUnit.HOURS.toMinutes(1);
	private static final Map<House, Integer> fromHouse;
	private static final Map<Integer, House> toHouse;
	private static final Map<Unit, Integer> fromUnit;
	private static final Map<Integer, Unit> toUnit;
	private static final Map<FunctionType, Integer> fromFunctionType;
	private static final Map<Integer, FunctionType> toFunctionType;
	public static final ReadData read = new ReadData();
	public static final WriteData write = new WriteData();

	static {
		fromHouse = Map.ofEntries(entry(House.A, 0x06), entry(House.B, 0x0E), entry(House.C, 0x02),
			entry(House.D, 0x0A), entry(House.E, 0x01), entry(House.F, 0x09), entry(House.G, 0x05),
			entry(House.H, 0x0D), entry(House.I, 0x07), entry(House.J, 0x0F), entry(House.K, 0x03),
			entry(House.L, 0x0B), entry(House.M, 0x00), entry(House.N, 0x08), entry(House.O, 0x04),
			entry(House.P, 0x0C));
		toHouse = Collections.unmodifiableMap(reverse(fromHouse));
	}

	static {
		fromUnit = Map.ofEntries(entry(Unit._1, 0x06), entry(Unit._2, 0x0E), entry(Unit._3, 0x02),
			entry(Unit._4, 0x0A), entry(Unit._5, 0x01), entry(Unit._6, 0x09), entry(Unit._7, 0x05),
			entry(Unit._8, 0x0D), entry(Unit._9, 0x07), entry(Unit._10, 0x0F),
			entry(Unit._11, 0x03), entry(Unit._12, 0x0B), entry(Unit._13, 0x00),
			entry(Unit._14, 0x08), entry(Unit._15, 0x04), entry(Unit._16, 0x0C));
		toUnit = Collections.unmodifiableMap(reverse(fromUnit));
	}

	static {
		fromFunctionType = Map.ofEntries(entry(FunctionType.ALL_UNITS_OFF, 0x0),
			entry(FunctionType.ALL_LIGHTS_ON, 0x1), entry(FunctionType.ON, 0x2),
			entry(FunctionType.OFF, 0x3), entry(FunctionType.DIM, 0x4),
			entry(FunctionType.BRIGHT, 0x5), entry(FunctionType.ALL_LIGHTS_OFF, 0x6),
			entry(FunctionType.EXTENDED, 0x7), entry(FunctionType.HAIL_REQUEST, 0x8),
			entry(FunctionType.HAIL_ACKNOWLEDGE, 0x9), entry(FunctionType.PRESET_DIM_1, 0xA),
			entry(FunctionType.PRESET_DIM_2, 0xB), entry(FunctionType.EXTENDED_DATA_XFER, 0xC),
			entry(FunctionType.STATUS_ON, 0xD), entry(FunctionType.STATUS_OFF, 0xE),
			entry(FunctionType.STATUS_REQUEST, 0xF));
		toFunctionType = Collections.unmodifiableMap(reverse(fromFunctionType));
	}

	private Data() {}

	public static byte shortChecksum(int sh) {
		return checksum(Immutable.wrap(ByteUtil.toMsb(sh, Short.BYTES)));
	}

	public static byte checksum(ByteProvider bytes) {
		int sum = 0;
		for (int i = 0; i < bytes.length(); i++)
			sum += bytes.getByte(i);
		return (byte) sum;
	}

	public static House toHouse(int data) {
		return toHouse.get(data & 0x0f);
	}

	public static Unit toUnit(int data) {
		return toUnit.get(data & 0x0f);
	}

	public static FunctionType toFunctionType(int data) {
		return toFunctionType.get(data & 0x0f);
	}

	public static byte fromHouse(House house) {
		return fromHouse.get(house).byteValue();
	}

	public static byte fromUnit(Unit unit) {
		return fromUnit.get(unit).byteValue();
	}

	public static byte fromFunctionType(FunctionType function) {
		return fromFunctionType.get(function).byteValue();
	}

	public static Address toAddress(int data) {
		House house = Data.toHouse(data >> 4 & 0xf);
		Unit unit = Data.toUnit(data & 0xf);
		return new Address(house, unit);
	}

	public static byte fromAddress(Address address) {
		int house = Data.fromHouse(address.house);
		int unit = Data.fromUnit(address.unit);
		return (byte) (house << 4 | unit);
	}

	/**
	 * Creates a date from data received from a status response.
	 *
	 * <pre>
	 * 	39 to 32	Current time (seconds)
	 * 	31 to 24	Current time (minutes ranging from 0 to 119)
	 * 	23 to 16	Current time (hours/2, ranging from 0 to 11)
	 * 	15 to 8 	Current year day (bits 0 to 7)
	 * 	7    		Current year day (bit 8)
	 * 	6 to 0 		Day mask (SMTWTFS)
	 * </pre>
	 */
	public static Date readDateFrom(ByteReader r) {
		int second = r.readByte();
		int minute = r.readByte();
		int hour = r.readByte() * 2;
		if (minute > MINUTES_IN_HOUR) {
			minute -= MINUTES_IN_HOUR;
			hour++;
		}
		int dayOfYear = r.readUbyte();
		dayOfYear += (r.readByte() << 1) & 0x100;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
		return cal.getTime();
	}

	/**
	 * Creates data from a date to send after a time poll request. Returns the offset in the data
	 * array into which data was written.
	 *
	 * <pre>
	 * 	39 to 32	Current time (seconds)
	 * 	31 to 24	Current time (minutes ranging from 0 to 119)
	 * 	23 to 16	Current time (hours/2, ranging from 0 to 11)
	 * 	15 to 8 	Current year day (bits 0 to 7)
	 * 	7    		Current year day (bit 8)
	 * 	6 to 0 		Day mask (SMTWTFS)
	 * </pre>
	 */
	public static void writeDateTo(Date date, ByteWriter<?> w) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int day = cal.get(Calendar.DAY_OF_YEAR);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour % 2 > 0) min += TimeUnit.HOURS.toMinutes(1);
		hour = hour / 2;
		dayOfWeek = 1 << (Calendar.SATURDAY - dayOfWeek);
		w.writeByte(sec);
		w.writeByte(min);
		w.writeByte(hour);
		w.writeByte(day & 0xff);
		w.writeByte((day & 0x100) >> 1 | dayOfWeek);
	}

	private static <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> reverse = new HashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet())
			reverse.put(entry.getValue(), entry.getKey());
		return reverse;
	}

}
