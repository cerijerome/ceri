package ceri.x10.cm11a.protocol;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.common.validation.ValidationUtil.validateRange;
import static java.time.DayOfWeek.SATURDAY;
import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.HOURS;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import ceri.common.data.ByteArray;
//import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.math.MathUtil;
import ceri.x10.type.Address;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

/**
 * Methods for converting between types and byte arrays for reading from/writing to the CM11A
 * controller.
 */
public class Data {
	private static final int MAX_DAY_DIFF = 30;
	private static final int MINUTES_IN_HOUR = (int) TimeUnit.HOURS.toMinutes(1);
	private static final Map<FunctionType, Integer> fromFunctionType;
	private static final Map<Integer, FunctionType> toFunctionType;
	public static final ReadData read = new ReadData();
	public static final WriteData write = new WriteData();
	private static final IntProvider fromTypes =
		IntArray.Immutable.wrap(6, 14, 2, 10, 1, 9, 5, 13, 7, 15, 3, 11, 0, 8, 4, 12);
	private static final IntProvider toTypes = reverse(fromTypes);

	// TODO:
	// change new X -> X.of + validate fields
	// change equals/hashcode
	// Entry.of(X) -> X.entry() ?
	// simplify function/command structures (combine as nested classes?)
	// ByteWriter -> encoder?
	// extract common code
	// tests: teq

	public static ExtFunction decodeExtFunction(House house, ByteReader r) {
		int data = r.readUbyte();
		int command = r.readUbyte();
		return ExtFunction.of(house, data, command);
	}

	public static int fromHouse(House house) {
		validateNotNull(house);
		return fromTypes.getInt(house.id - 1);
	}

	public static House toHouse(int value) {
		return House.fromId(toTypeValue(value));
	}

	public static int fromUnit(Unit unit) {
		validateNotNull(unit);
		return fromTypes.getInt(unit.value - 1);
	}

	public static Unit toUnit(int value) {
		return Unit.from(toTypeValue(value));
	}

	public static int toTypeValue(int value) {
		value = value & 0xf;
		validateRange(value, 0, toTypes.length() - 1);
		return toTypes.getInt(value) + 1;
	}

	static {
		fromFunctionType = Map.ofEntries(entry(FunctionType.allUnitsOff, 0x0),
			entry(FunctionType.allLightsOn, 0x1), entry(FunctionType.on, 0x2),
			entry(FunctionType.off, 0x3), entry(FunctionType.dim, 0x4),
			entry(FunctionType.bright, 0x5), entry(FunctionType.allLightsOff, 0x6),
			entry(FunctionType.extended, 0x7), entry(FunctionType.hailRequest, 0x8),
			entry(FunctionType.hailAcknowledge, 0x9), entry(FunctionType.presetDim1, 0xA),
			entry(FunctionType.presetDim2, 0xB), entry(FunctionType.extendedDataXfer, 0xC),
			entry(FunctionType.statusOn, 0xD), entry(FunctionType.statusOff, 0xE),
			entry(FunctionType.statusRequest, 0xF));
		toFunctionType = Collections.unmodifiableMap(reverse(fromFunctionType));
	}

	private Data() {}

	public static int shortChecksum(int sh) {
		return checksum(ByteArray.Immutable.wrap(ByteUtil.toMsb(sh, Short.BYTES)));
	}

	public static int checksum(ByteProvider bytes) {
		int sum = 0;
		for (int i = 0; i < bytes.length(); i++)
			sum += bytes.getByte(i);
		return MathUtil.ubyte(sum);
	}

	public static FunctionType toFunctionType(int data) {
		return toFunctionType.get(data & 0x0f);
	}

	public static byte fromFunctionType(FunctionType function) {
		return fromFunctionType.get(function).byteValue();
	}

	public static Address toAddress(int data) {
		House house = Data.toHouse(data >> 4 & 0xf);
		Unit unit = Data.toUnit(data & 0xf);
		return Address.of(house, unit);
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
	 * 5 bytes read msb first:
	 * B4: 39 to 32 Current time (seconds)
	 * B3: 31 to 24 Current time (minutes ranging from 0 to 119)
	 * B2: 23 to 16 Current time (hours/2, ranging from 0 to 11)
	 * B1: 15 to 8  Current year day (bits 0 to 7)
	 * B0: 7        Current year day (bit 8)
	 *     6 to 0   Day mask (SMTWTFS)
	 * </pre>
	 */
	public static LocalDateTime readDateFrom(ByteReader r) {
		int second = r.readUbyte();
		int minute = r.readUbyte();
		int hour = r.readUbyte() << 1;
		if (minute > MINUTES_IN_HOUR) {
			minute -= MINUTES_IN_HOUR;
			hour++;
		}
		int dayOfYear = r.readUbyte() + ((r.readUbyte() << 1) & 0x100);
		LocalDate date = date(dayOfYear);
		LocalTime time = LocalTime.of(hour, minute, second);
		return LocalDateTime.of(date, time);
	}

	/**
	 * Creates data from a date to send after a time poll request. Returns the offset in the data
	 * array into which data was written.
	 *
	 * <pre>
	 * B4: 39 to 32 Current time (seconds)
	 * B3: 31 to 24 Current time (minutes ranging from 0 to 119)
	 * B2: 23 to 16 Current time (hours/2, ranging from 0 to 11)
	 * B1: 15 to 8  Current year day (bits 0 to 7)
	 * B0: 7        Current year day (bit 8)
	 *     6 to 0   Day mask (SMTWTFS)
	 * </pre>
	 */
	public static void writeDateTo(LocalDateTime dateTime, ByteWriter<?> w) {
		int sec = dateTime.getSecond();
		int min = dateTime.getMinute();
		int hour = dateTime.getHour();
		int day = dateTime.getDayOfYear();
		int dayOfWeek = SATURDAY.getValue() - dateTime.getDayOfWeek().getValue();
		if (dayOfWeek < 0) dayOfWeek = SATURDAY.getValue();
		if ((hour & 1) != 0) min += HOURS.toMinutes(1);
		hour = hour >>> 1;
		w.writeBytes(sec, min, hour, day, (day >>> Byte.SIZE) | (1 << dayOfWeek));
	}

	/**
	 * Determines year from current year and given day.
	 */
	private static LocalDate date(int dayOfYear) {
		LocalDate now = LocalDate.now();
		LocalDate date = LocalDate.ofYearDay(now.getYear(), dayOfYear);
		long days = ChronoUnit.DAYS.between(now, date);
		// Adjust in case end of year time difference
		if (days > MAX_DAY_DIFF) return date.minusYears(1); // dayOfYear is last year
		if (days < -MAX_DAY_DIFF) return date.plusYears(1); // dayOfYear is next year
		return date;
	}

	private static <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> reverse = new HashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet())
			reverse.put(entry.getValue(), entry.getKey());
		return reverse;
	}

	private static IntProvider reverse(IntProvider provider) {
		int[] reverse = new int[provider.length()];
		for (int i = 0; i < reverse.length; i++)
			reverse[provider.getInt(i)] = i;
		return IntArray.Immutable.wrap(reverse);
	}

}
