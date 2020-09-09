package ceri.x10.cm11a.protocol;

import static java.lang.Integer.toBinaryString;
import static java.lang.Integer.toHexString;
import java.time.LocalDateTime;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteWriter;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
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
	public final LocalDateTime date;
	public final House house;
	public final int firmware;
	public final short addressed;
	public final short onOff;
	public final short dim;

	public static ReadStatus decode(ByteReader r) {
		Builder builder = new Builder();
		builder.batteryTimer(r.readUshortMsb());
		builder.date(Data.readDateFrom(r));
		int b = r.readUbyte();
		builder.house(Data.toHouse(b >> 4));
		builder.firmware(b & 0xf);
		builder.addressed(r.readUshortMsb());
		builder.onOff(r.readUshortMsb());
		builder.dim(r.readUshortMsb());
		return builder.build();
	}

	public static class Builder {
		short batteryTimer = 0;
		LocalDateTime date = LocalDateTime.now();
		House house = House.M;
		int firmware = 1;
		short addressed = 0;
		short onOff = 0;
		short dim = 0;

		Builder() {}
		
		public Builder batteryTimer(int batteryTimer) {
			this.batteryTimer = (short) batteryTimer;
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
		date = builder.date;
		house = builder.house;
		firmware = builder.firmware;
		addressed = builder.addressed;
		onOff = builder.onOff;
		dim = builder.dim;
	}

	public void encode(ByteWriter<?> w) {
		w.writeShortMsb(batteryTimer);
		Data.writeDateTo(date, w);
		w.writeByte(Data.fromHouse(house) << 4 | firmware & 0xf);
		w.writeShortMsb(addressed);
		w.writeShortMsb(onOff);
		w.writeShortMsb(dim);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(batteryTimer, date, house, firmware, addressed, onOff, dim);
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
		return ToStringHelper.createByClass(this, "0x" + toHexString(batteryTimer), date, house,
			firmware, toBinaryString(addressed), toBinaryString(onOff), toBinaryString(dim))
			.toString();
	}

}