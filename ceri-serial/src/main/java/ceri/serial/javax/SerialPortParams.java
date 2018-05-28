package ceri.serial.javax;

import java.util.concurrent.TimeUnit;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class SerialPortParams {
	public static final SerialPortParams DEFAULT = builder().build();
	private static final int START_BITS = 1;
	private static final long MICROS_PER_SECOND = TimeUnit.SECONDS.toMicros(1);
	public final int baudRate;
	public final DataBits dataBits;
	public final StopBits stopBits;
	public final Parity parity;

	public static SerialPortParams of(int baudRate) {
		return builder().baudRate(baudRate).build();
	}

	public static SerialPortParams of(int baudRate, DataBits dataBits, StopBits stopBits,
		Parity parity) {
		return builder().baudRate(baudRate).dataBits(dataBits).stopBits(stopBits).parity(parity)
			.build();
	}

	public static class Builder {
		int baudRate = 9600;
		DataBits dataBits = DataBits._8;
		StopBits stopBits = StopBits._1;
		Parity parity = Parity.none;

		Builder() {}

		public Builder baudRate(int baudRate) {
			this.baudRate = baudRate;
			return this;
		}

		public Builder dataBits(DataBits dataBits) {
			this.dataBits = dataBits;
			return this;
		}

		public Builder stopBits(StopBits stopBits) {
			this.stopBits = stopBits;
			return this;
		}

		public Builder parity(Parity parity) {
			this.parity = parity;
			return this;
		}

		public SerialPortParams build() {
			return new SerialPortParams(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SerialPortParams(Builder builder) {
		baudRate = builder.baudRate;
		dataBits = builder.dataBits;
		stopBits = builder.stopBits;
		parity = builder.parity;
	}

	public double microsPerBit() {
		return (double) MICROS_PER_SECOND / baudRate;
	}

	public double sendTimeMicros(int frames) {
		return microsPerFrame() *frames;
	}

	public double microsPerFrame() {
		return microsPerBit() * bitsPerFrame();
	}

	public int bitsPerFrame() {
		return START_BITS + dataBits.value + (stopBits == StopBits._1 ? 1 : 2) +
			(parity == Parity.none ? 0 : 1);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(baudRate, dataBits, stopBits, parity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SerialPortParams)) return false;
		SerialPortParams other = (SerialPortParams) obj;
		if (baudRate != other.baudRate) return false;
		if (!EqualsUtil.equals(dataBits, other.dataBits)) return false;
		if (!EqualsUtil.equals(stopBits, other.stopBits)) return false;
		if (!EqualsUtil.equals(parity, other.parity)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, baudRate, dataBits, stopBits, parity).toString();
	}

	public static void main(String[] args) {
		SerialPortParams p = builder().baudRate(250000).stopBits(StopBits._2).build();
		System.out.println(p.microsPerBit());
		System.out.println(p.microsPerFrame());
	}

}
