package ceri.x10.cm11a.protocol;

import static ceri.common.data.DataUtil.expect;
import java.time.LocalDateTime;
import ceri.common.data.ByteArray.Encodable;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.date.DateUtil;
import ceri.common.data.ByteReader;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.x10.command.House;

/**
 * Data to send for a set interface clock request 0xa5 from the CM11a device.
 * <p/>
 * Protocol doc section 8.
 * 
 * <pre>
 * 	Bit range	Description
 * 	55 to 48	timer download header (0x9b)
 * 	47 to 40	Current time (seconds)
 * 	39 to 32	Current time (minutes ranging from 0 to 119)
 * 	31 to 24	Current time (hours/2, ranging from 0 to 11)
 * 	23 to 16	Current year day (bits 0 to 7)
 * 	15   		Current year day (bit 8)
 * 	14 to 8		Day mask (SMTWTFS)
 * 	7 to 4		Monitored house code
 * 	3    		Reserved
 * 	2    		Battery timer clear flag
 * 	1    		Monitored status clear flag
 * 	0    		Timer purge flag
 * </pre>
 */
public class Clock implements Encodable {
	private static final int SIZE = 1 + Data.DATE_BYTES + 1;
	private static final int CLEAR_BATTERY_TIMER_FLAG = 0x4;
	private static final int CLEAR_MONITORED_STATUS_FLAG = 0x2;
	private static final int PURGE_TIMER_FLAG = 0x1;
	public final LocalDateTime date;
	public final House house;
	public final boolean clearBatteryTimer;
	public final boolean clearMonitoredStatus;
	public final boolean purgeTimer;

	public static Clock decode(ByteReader r) {
		expect(r, Protocol.TIME.value);
		Builder builder = new Builder();
		builder.date(Data.readDateFrom(r));
		int code = r.readUbyte();
		builder.house(Data.decodeHouse(code));
		builder.clearBatteryTimer(clearBatteryTimer(code));
		builder.clearMonitoredStatus(clearMonitoredStatus(code));
		builder.purgeTimer(purgeTimer(code));
		return builder.build();
	}

	public static Clock of() {
		return builder().build();
	}

	public static Clock of(House house) {
		return builder().house(house).build();
	}

	public static class Builder {
		LocalDateTime date = DateUtil.nowSec();
		House house = null;
		boolean clearBatteryTimer = false;
		boolean clearMonitoredStatus = false;
		boolean purgeTimer = false;

		Builder() {}

		public Builder date(LocalDateTime date) {
			this.date = date;
			return this;
		}

		public Builder house(House house) {
			this.house = house;
			return this;
		}

		public Builder clearBatteryTimer(boolean clearBatteryTimer) {
			this.clearBatteryTimer = clearBatteryTimer;
			return this;
		}

		public Builder clearMonitoredStatus(boolean clearMonitoredStatus) {
			this.clearMonitoredStatus = clearMonitoredStatus;
			return this;
		}

		public Builder purgeTimer(boolean purgeTimer) {
			this.purgeTimer = purgeTimer;
			return this;
		}

		public Clock build() {
			return new Clock(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Clock(Builder builder) {
		date = builder.date;
		house = builder.house;
		clearBatteryTimer = builder.clearBatteryTimer;
		clearMonitoredStatus = builder.clearMonitoredStatus;
		purgeTimer = builder.purgeTimer;
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public void encode(Encoder encoder) {
		encoder.writeByte(Protocol.TIME.value);
		Data.writeDateTo(date, encoder);
		int code = Data.encode(house, flags(clearBatteryTimer, clearMonitoredStatus, purgeTimer));
		encoder.writeByte(code);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(date, house, clearBatteryTimer, clearMonitoredStatus, purgeTimer);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Clock)) return false;
		Clock other = (Clock) obj;
		if (!EqualsUtil.equals(date, other.date)) return false;
		if (house != other.house) return false;
		if (clearBatteryTimer != other.clearBatteryTimer) return false;
		if (clearMonitoredStatus != other.clearMonitoredStatus) return false;
		if (purgeTimer != other.purgeTimer) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, date, house, clearBatteryTimer, clearMonitoredStatus, purgeTimer)
			.toString();
	}

	private static boolean clearBatteryTimer(int code) {
		return (code & CLEAR_BATTERY_TIMER_FLAG) != 0;
	}

	private static boolean clearMonitoredStatus(int code) {
		return (code & CLEAR_MONITORED_STATUS_FLAG) != 0;
	}

	private static boolean purgeTimer(int code) {
		return (code & PURGE_TIMER_FLAG) != 0;
	}

	private static int flags(boolean clearBatteryTimer, boolean clearMonitoredStatus,
		boolean purgeTimer) {
		return (clearBatteryTimer ? CLEAR_BATTERY_TIMER_FLAG : 0) |
			(clearMonitoredStatus ? CLEAR_MONITORED_STATUS_FLAG : 0) |
			(purgeTimer ? PURGE_TIMER_FLAG : 0);
	}

}
