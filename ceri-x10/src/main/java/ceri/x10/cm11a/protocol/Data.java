package ceri.x10.cm11a.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
		Map<House, Integer> map = new HashMap<>();
		map.put(House.A, 0x06);
		map.put(House.B, 0x0E);
		map.put(House.C, 0x02);
		map.put(House.D, 0x0A);
		map.put(House.E, 0x01);
		map.put(House.F, 0x09);
		map.put(House.G, 0x05);
		map.put(House.H, 0x0D);
		map.put(House.I, 0x07);
		map.put(House.J, 0x0F);
		map.put(House.K, 0x03);
		map.put(House.L, 0x0B);
		map.put(House.M, 0x00);
		map.put(House.N, 0x08);
		map.put(House.O, 0x04);
		map.put(House.P, 0x0C);
		fromHouse = Collections.unmodifiableMap(map);
		toHouse = Collections.unmodifiableMap(reverse(fromHouse));
	}

	static {
		Map<Unit, Integer> map = new HashMap<>();
		map.put(Unit._1, 0x06);
		map.put(Unit._2, 0x0E);
		map.put(Unit._3, 0x02);
		map.put(Unit._4, 0x0A);
		map.put(Unit._5, 0x01);
		map.put(Unit._6, 0x09);
		map.put(Unit._7, 0x05);
		map.put(Unit._8, 0x0D);
		map.put(Unit._9, 0x07);
		map.put(Unit._10, 0x0F);
		map.put(Unit._11, 0x03);
		map.put(Unit._12, 0x0B);
		map.put(Unit._13, 0x00);
		map.put(Unit._14, 0x08);
		map.put(Unit._15, 0x04);
		map.put(Unit._16, 0x0C);
		fromUnit = Collections.unmodifiableMap(map);
		toUnit = Collections.unmodifiableMap(reverse(fromUnit));
	}

	static {
		Map<FunctionType, Integer> map = new HashMap<>();
		map.put(FunctionType.ALL_UNITS_OFF, 0x0);
		map.put(FunctionType.ALL_LIGHTS_ON, 0x1);
		map.put(FunctionType.ON, 0x2);
		map.put(FunctionType.OFF, 0x3);
		map.put(FunctionType.DIM, 0x4);
		map.put(FunctionType.BRIGHT, 0x5);
		map.put(FunctionType.ALL_LIGHTS_OFF, 0x6);
		map.put(FunctionType.EXTENDED, 0x7);
		map.put(FunctionType.HAIL_REQUEST, 0x8);
		map.put(FunctionType.HAIL_ACKNOWLEDGE, 0x9);
		map.put(FunctionType.PRESET_DIM_1, 0xA);
		map.put(FunctionType.PRESET_DIM_2, 0xB);
		map.put(FunctionType.EXTENDED_DATA_XFER, 0xC);
		map.put(FunctionType.STATUS_ON, 0xD);
		map.put(FunctionType.STATUS_OFF, 0xE);
		map.put(FunctionType.STATUS_REQUEST, 0xF);
		fromFunctionType = Collections.unmodifiableMap(map);
		toFunctionType = Collections.unmodifiableMap(reverse(fromFunctionType));
	}

	private Data() {}

	public static byte checksum(byte...bytes) {
		int sum = 0;
		for (byte b : bytes) sum += b;
		return (byte)(sum & 0xff);
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
	public static Date readDateFrom(DataInput in) throws IOException {
		int second = in.readByte();
		int minute = in.readByte();
		int hour = in.readByte() * 2;
		if (minute > MINUTES_IN_HOUR) {
			minute -= MINUTES_IN_HOUR;
			hour++;
		}
		int dayOfYear = in.readByte();
		dayOfYear += (in.readByte() << 1) & 0x100;
		Calendar cal = Calendar.getInstance();
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
	public static void writeDateTo(Date date, DataOutput out) throws IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int day = cal.get(Calendar.DAY_OF_YEAR);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour % 2 > 0) min += TimeUnit.HOURS.toMinutes(1);
		hour = hour / 2;
		dayOfWeek = 1 << (Calendar.SATURDAY - dayOfWeek);
		out.write(sec);
		out.write(min);
		out.write(hour);
		out.write(day & 0xff);
		out.write((day & 0x100) >> 1 | dayOfWeek);
	}

	private static <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> reverse = new HashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet())
			reverse.put(entry.getValue(), entry.getKey());
		return reverse;
	}

}
