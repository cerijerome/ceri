package ceri.x10.cm11a.protocol;

import static ceri.x10.util.X10Util.fromNybble;
import static ceri.x10.util.X10Util.octet;
import static java.time.DayOfWeek.SATURDAY;
import static java.util.concurrent.TimeUnit.HOURS;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.math.MathUtil;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

/**
 * Common methods for converting between types and bytes for transmitting to / receiving from the
 * CM11a controller.
 */
public class Data {
	public static final int DATE_BYTES = 5;
	private static final int MAX_DAY_DIFF = 30;
	private static final int MINUTES_IN_HOUR = (int) TimeUnit.HOURS.toMinutes(1);
	private static final IntProvider fromTypes =
		IntArray.Immutable.wrap(6, 14, 2, 10, 1, 9, 5, 13, 7, 15, 3, 11, 0, 8, 4, 12);
	private static final IntProvider toTypes = reverse(fromTypes);

	private Data() {}

	public static House decodeHouse(int code) {
		return toHouse(fromNybble(code, 1));
	}

	public static int decodeLower(int code) {
		return fromNybble(code, 0);
	}

	public static Unit decodeUnit(int code) {
		return toUnit(fromNybble(code, 0));
	}

	public static FunctionType decodeFunctionType(int code) {
		return toFunctionType(fromNybble(code, 0));
	}

	public static int encode(House house, int value) {
		return octet(fromHouse(house), value);
	}

	public static int encode(House house, Unit unit) {
		return octet(fromHouse(house), fromUnit(unit));
	}

	public static int encode(House house, FunctionType type) {
		return octet(fromHouse(house), fromFunctionType(type));
	}

	private static int fromHouse(House house) {
		if (house == null) return 0;
		return fromTypes.getInt(house.id - 1);
	}

	private static House toHouse(int value) {
		return House.fromId(toTypeValue(value));
	}

	private static int fromUnit(Unit unit) {
		if (unit == null) return 0;
		return fromTypes.getInt(unit.value - 1);
	}

	private static Unit toUnit(int value) {
		return Unit.from(toTypeValue(value));
	}

	private static int toTypeValue(int value) {
		return toTypes.getInt(value) + 1;
	}

	private static FunctionType toFunctionType(int value) {
		return FunctionType.from(value + 1);
	}

	private static int fromFunctionType(FunctionType function) {
		return function.id - 1;
	}

	public static int shortChecksum(int sh) {
		return checksum(ByteArray.Immutable.wrap(ByteUtil.toMsb(sh, Short.BYTES)));
	}

	public static int checksum(ByteProvider bytes) {
		int sum = 0;
		for (int i = 0; i < bytes.length(); i++)
			sum += bytes.getByte(i);
		return MathUtil.ubyte(sum);
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

	private static IntProvider reverse(IntProvider provider) {
		int[] reverse = new int[provider.length()];
		for (int i = 0; i < reverse.length; i++)
			reverse[provider.getInt(i)] = i;
		return IntArray.Immutable.wrap(reverse);
	}

}
