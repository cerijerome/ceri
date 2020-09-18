package ceri.x10.cm11a.protocol;

import static ceri.common.text.StringUtil.toBinary;
import java.time.LocalDateTime;
import ceri.common.data.ByteArray.Encodable;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.time.DateUtil;
import ceri.common.data.ByteReader;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.x10.command.House;

/**
 * Status read from CM11a after a status request 0x8b.
 * <p/>
 * Protocol doc section 9.
 * 
 * <pre>
 * Bit range  Description
 * 111 to 96  Battery timer (set to 0xffff on reset)
 *  95 to 88  Current time (seconds)
 *  87 to 80  Current time (minutes ranging from 0 to 119)
 *  79 to 72  Current time (hours/2, ranging from 0 to 11)
 *  71 to 63  Current year day (MSB bit 63)
 *  62 to 56  Day mask (SMTWTFS)
 *  55 to 52  Monitored house code
 *  51 to 48  Firmware revision level 0 to 15
 *  47 to 32  Currently addressed monitored devices
 *  31 to 16  On / Off status of the monitored devices
 *  15 to 0   Dim status of the monitored devices
 * </pre>
 */
public class Status implements Encodable {
	private static final int SIZE = 2 + Data.DATE_BYTES + 7;
	private static final int BATTERY_TIMER_RESET = 0xffff;
	public final int batteryTimer;
	public final LocalDateTime date;
	public final House house;
	public final int firmware;
	public final int addressed;
	public final int onOff;
	public final int dim;

	public static Status decode(ByteReader r) {
		Builder builder = new Builder();
		builder.batteryTimer(r.readUshortMsb());
		builder.date(Data.readDateFrom(r));
		int code = r.readUbyte();
		builder.house(Data.decodeHouse(code));
		builder.firmware(Data.decodeLower(code));
		builder.addressed(r.readUshortMsb());
		builder.onOff(r.readUshortMsb());
		builder.dim(r.readUshortMsb());
		return builder.build();
	}

	public static class Builder {
		int batteryTimer = 0;
		LocalDateTime date = DateUtil.nowSec();
		House house = House.M;
		int firmware = 1;
		int addressed = 0;
		int onOff = 0;
		int dim = 0;

		Builder() {}

		public Builder batteryTimerReset() {
			return batteryTimer(BATTERY_TIMER_RESET);
		}

		public Builder batteryTimer(int batteryTimer) {
			this.batteryTimer = batteryTimer;
			return this;
		}

		public Builder date(LocalDateTime date) {
			this.date = date;
			return this;
		}

		public Builder house(House house) {
			this.house = house;
			return this;
		}

		public Builder firmware(int firmware) {
			this.firmware = firmware;
			return this;
		}

		public Builder addressed(int addressed) {
			this.addressed = addressed;
			return this;
		}

		public Builder onOff(int onOff) {
			this.onOff = onOff;
			return this;
		}

		public Builder dim(int dim) {
			this.dim = dim;
			return this;
		}

		public Status build() {
			return new Status(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Status(Builder builder) {
		batteryTimer = builder.batteryTimer;
		date = builder.date;
		house = builder.house;
		firmware = builder.firmware;
		addressed = builder.addressed;
		onOff = builder.onOff;
		dim = builder.dim;
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public void encode(Encoder encoder) {
		encoder.writeShortMsb(batteryTimer);
		Data.writeDateTo(date, encoder);
		encoder.writeByte(Data.encode(house, firmware));
		encoder.writeShortMsb(addressed);
		encoder.writeShortMsb(onOff);
		encoder.writeShortMsb(dim);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(batteryTimer, date, house, firmware, addressed, onOff, dim);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Status)) return false;
		Status other = (Status) obj;
		if (batteryTimer != other.batteryTimer) return false;
		if (!EqualsUtil.equals(date, other.date)) return false;
		if (house != other.house) return false;
		if (firmware != other.firmware) return false;
		if (addressed != other.addressed) return false;
		if (onOff != other.onOff) return false;
		if (dim != other.dim) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s(0x%x,%s,%s,%d,%s,%s,%s)", getClass().getSimpleName(), batteryTimer,
			date, house, firmware, toBinary(addressed, Short.SIZE), toBinary(onOff, Short.SIZE),
			toBinary(dim, Short.SIZE));
	}

}
