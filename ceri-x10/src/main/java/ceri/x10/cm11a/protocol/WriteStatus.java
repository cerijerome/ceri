package ceri.x10.cm11a.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import ceri.common.date.ImmutableDate;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;
import ceri.x10.type.House;

/**
 * Status info to write to CM11A after a TIME_POLL request.
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
public class WriteStatus {
	public final Date date;
	public final House house;
	public final boolean clearBatteryTimer;
	public final boolean clearMonitoredStatus;
	public final boolean purgeTimer;
	private final int hashCode;

	public static class Builder {
		Date date = new Date();
		House house = null;
		boolean clearBatteryTimer = false;
		boolean clearMonitoredStatus = false;
		boolean purgeTimer = false;

		public Builder date(Date date) {
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

		public WriteStatus build() {
			return new WriteStatus(this);
		}
	}

	WriteStatus(Builder builder) {
		date = new ImmutableDate(builder.date);
		house = builder.house;
		clearBatteryTimer = builder.clearBatteryTimer;
		clearMonitoredStatus = builder.clearMonitoredStatus;
		purgeTimer = builder.purgeTimer;
		hashCode = HashCoder.hash(date, house, clearBatteryTimer, clearMonitoredStatus, purgeTimer);
	}

	public static WriteStatus createDefault() {
		return new Builder().build();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof WriteStatus)) return false;
		WriteStatus other = (WriteStatus) obj;
		return date.equals(other.date) && EqualsUtil.equals(house, other.house) &&
			clearBatteryTimer == other.clearBatteryTimer &&
			clearMonitoredStatus == other.clearMonitoredStatus && purgeTimer == other.purgeTimer;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, date, house, clearBatteryTimer,
			clearMonitoredStatus, purgeTimer).toString();
	}

	public void writeTo(DataOutput out) throws IOException {
		out.writeByte(Protocol.TIME.value);
		Data.writeDateTo(date, out);
		int b = house == null ? 0 : Data.fromHouse(house) << 4;
		if (clearBatteryTimer) b |= 0x4;
		if (clearMonitoredStatus) b |= 0x2;
		if (purgeTimer) b |= 0x1;
		out.writeByte(b);
	}

	public static WriteStatus readFrom(DataInput in) throws IOException {
		Builder builder = new Builder();
		byte b = in.readByte();
		if (b != Protocol.TIME.value) throw new IllegalArgumentException("Expected TIME byte 0x" +
			Integer.toHexString(Protocol.TIME.value) + ": 0x" + Integer.toHexString(b));
		builder.date(Data.readDateFrom(in));
		b = in.readByte();
		builder.house(Data.toHouse(b >> 4));
		builder.clearBatteryTimer((b & 0x4) != 0);
		builder.clearMonitoredStatus((b & 0x2) != 0);
		builder.purgeTimer((b & 0x1) != 0);
		return builder.build();
	}

}