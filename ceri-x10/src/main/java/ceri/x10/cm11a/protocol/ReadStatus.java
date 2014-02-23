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
 * Status read from CM11A after a status request.
 * 
 * <pre>
 * 	Bit range	Description
 * 	111 to 96	Battery timer (set to 0xffff on reset)
 * 	95 to 88	Current time (seconds)
 * 	87 to 80	Current time (minutes ranging from 0 to 119)
 * 	79 to 72	Current time (hours/2, ranging from 0 to 11)
 * 	71 to 63	Current year day (MSB bit 63)
 * 	62 to 56	Day mask (SMTWTFS)
 * 	55 to 52	Monitored house code
 * 	51 to 48	Firmware revision level 0 to 15
 * 	47 to 32	Currently addressed monitored devices
 * 	31 to 16	On / Off status of the monitored devices
 * 	15 to 0		Dim status of the monitored devices
 * </pre>
 */
public class ReadStatus {
	public final short batteryTimer;
	public final Date date;
	public final House house;
	public final int firmware;
	public final short addressed;
	public final short onOff;
	public final short dim;
	private final int hashCode;

	public static class Builder {
		short batteryTimer = 0;
		Date date = new Date();
		House house = House.M;
		int firmware = 1;
		short addressed = 0;
		short onOff = 0;
		short dim = 0;

		public Builder batteryTimer(int batteryTimer) {
			this.batteryTimer = (short) batteryTimer;
			return this;
		}

		public Builder date(Date date) {
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
			this.addressed = (short) addressed;
			return this;
		}

		public Builder onOff(int onOff) {
			this.onOff = (short) onOff;
			return this;
		}

		public Builder dim(int dim) {
			this.dim = (short) dim;
			return this;
		}

		public ReadStatus build() {
			return new ReadStatus(this);
		}
	}

	ReadStatus(Builder builder) {
		batteryTimer = builder.batteryTimer;
		date = builder.date == null ? null : new ImmutableDate(builder.date);
		house = builder.house;
		firmware = builder.firmware;
		addressed = builder.addressed;
		onOff = builder.onOff;
		dim = builder.dim;
		hashCode = HashCoder.hash(batteryTimer, date, house, firmware, addressed, onOff, dim);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ReadStatus)) return false;
		ReadStatus other = (ReadStatus) obj;
		return batteryTimer == other.batteryTimer && EqualsUtil.equals(date, other.date) &&
			EqualsUtil.equals(house, other.house) && firmware == other.firmware &&
			addressed == other.addressed && onOff == other.onOff && dim == other.dim;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this,
			"0x" + Integer.toHexString(batteryTimer & 0xffff), date, house, firmware,
			Integer.toBinaryString(addressed & 0xffff), Integer.toBinaryString(onOff & 0xffff),
			Integer.toBinaryString(dim & 0xffff)).toString();
	}

	public void writeTo(DataOutput out) throws IOException {
		out.writeShort(batteryTimer);
		Data.writeDateTo(date, out);
		out.write(Data.fromHouse(house) << 4 | firmware & 0xf);
		out.writeShort(addressed);
		out.writeShort(onOff);
		out.writeShort(dim);
	}

	public static ReadStatus readFrom(DataInput in) throws IOException {
		Builder builder = new Builder();
		builder.batteryTimer(in.readShort());
		builder.date(Data.readDateFrom(in));
		byte b = in.readByte();
		builder.house(Data.toHouse(b >> 4));
		builder.firmware(b & 0xf);
		builder.addressed(in.readShort());
		builder.onOff(in.readShort());
		builder.dim(in.readShort());
		return builder.build();
	}

}