package ceri.serial.ftdi.util;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.jna.Pointer;
import ceri.common.function.Namer;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealing;
import ceri.log.io.SelfHealingConnector;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiDevice;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiTransferControl;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * A self-healing ftdi device. It will automatically reconnect if the cable is removed and
 * reinserted.
 */
public class SelfHealingFtdi extends SelfHealingConnector<Ftdi> implements Ftdi.Fixable {
	private final Config config;
	private final FtdiConfig.Builder ftdiConfig;

	public static class Config {
		public static final Config DEFAULT = builder().build();
		private static final Predicate<Exception> DEFAULT_PREDICATE =
			Namer.predicate(FtdiDevice::isFatal, "Ftdi::isFatal");
		private final Function<Config, Ftdi.Fixable> ftdiFn;
		public final LibUsbFinder finder;
		public final ftdi_interface iface;
		public final FtdiFactory factory;
		public final FtdiConfig ftdi;
		public final SelfHealing.Config selfHealing;

		public static interface FtdiFactory {
			Ftdi open(LibUsbFinder finder, ftdi_interface iface) throws IOException;
		}

		public static Config of(String finder) {
			return of(LibUsbFinder.from(finder));
		}

		public static Config of(LibUsbFinder finder) {
			return builder().finder(finder).build();
		}

		public static class Builder {
			Function<Config, Ftdi.Fixable> ftdiFn = SelfHealingFtdi::of;
			LibUsbFinder finder = LibFtdiUtil.FINDER;
			ftdi_interface iface = ftdi_interface.INTERFACE_ANY;
			FtdiFactory factory = FtdiDevice::open;
			FtdiConfig ftdi = FtdiConfig.NULL;
			SelfHealing.Config.Builder selfHealing =
				SelfHealing.Config.builder().brokenPredicate(DEFAULT_PREDICATE);

			Builder() {}

			/**
			 * Useful to override construction of the ftdi controller.
			 */
			public Builder ftdiFn(Function<Config, Ftdi.Fixable> ftdiFn) {
				this.ftdiFn = ftdiFn;
				return this;
			}

			/**
			 * Useful to override construction of the wrapped ftdi device.
			 */
			public Builder factory(FtdiFactory factory) {
				this.factory = factory;
				return this;
			}

			public Builder finder(String descriptor) {
				return finder(LibUsbFinder.from(descriptor));
			}

			public Builder finder(LibUsbFinder finder) {
				this.finder = finder;
				return this;
			}

			public Builder iface(ftdi_interface iface) {
				this.iface = iface;
				return this;
			}

			public Builder ftdi(FtdiConfig ftdi) {
				this.ftdi = ftdi;
				return this;
			}

			public Builder selfHealing(SelfHealing.Config selfHealing) {
				this.selfHealing.apply(selfHealing);
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		public static Builder builder(Config config) {
			return builder().ftdiFn(config.ftdiFn).finder(config.finder).iface(config.iface)
				.factory(config.factory).ftdi(config.ftdi).selfHealing(config.selfHealing);
		}

		Config(Builder builder) {
			ftdiFn = builder.ftdiFn;
			finder = builder.finder;
			iface = builder.iface;
			factory = builder.factory;
			ftdi = builder.ftdi;
			selfHealing = builder.selfHealing.build();
		}

		/**
		 * Useful to override construction of the ftdi controller.
		 */
		public Ftdi.Fixable ftdi() {
			return ftdiFn.apply(this);
		}

		@Override
		public String toString() {
			return ToString.forClass(this, finder, iface, ftdi, selfHealing);
		}
	}

	public static SelfHealingFtdi of(Config config) {
		return new SelfHealingFtdi(config);
	}

	private SelfHealingFtdi(Config config) {
		super(config.selfHealing);
		this.config = config;
		ftdiConfig = FtdiConfig.builder(config.ftdi);
		start();
	}

	@Override
	public ftdi_usb_strings descriptor() throws IOException {
		return device.applyIfSet(Ftdi::descriptor, ftdi_usb_strings.NULL);
	}

	@Override
	public void usbReset() throws IOException {
		device.acceptValid(Ftdi::usbReset);
	}

	@Override
	public void bitMode(FtdiBitMode bitMode) throws IOException {
		ftdiConfig.bitMode(bitMode);
		device.acceptValid(f -> f.bitMode(bitMode));
	}

	@Override
	public void baud(int baud) throws IOException {
		ftdiConfig.baud(baud);
		device.acceptValid(f -> f.baud(baud));
	}

	@Override
	public void line(FtdiLineParams params) throws IOException {
		ftdiConfig.params(params);
		device.acceptValid(f -> f.line(params));
	}

	@Override
	public void flowControl(FtdiFlowControl flowControl) throws IOException {
		ftdiConfig.flowControl(flowControl);
		device.acceptValid(f -> f.flowControl(flowControl));
	}

	@Override
	public void dtr(boolean state) throws IOException {
		device.acceptValid(f -> f.dtr(state));
	}

	@Override
	public void rts(boolean state) throws IOException {
		device.acceptValid(f -> f.rts(state));
	}

	@Override
	public int readPins() throws IOException {
		return device.applyValid(Ftdi::readPins);
	}

	@Override
	public int pollModemStatus() throws IOException {
		return device.applyValid(Ftdi::pollModemStatus);
	}

	@Override
	public void latencyTimer(int latency) throws IOException {
		ftdiConfig.latencyTimer(latency);
		device.acceptValid(f -> f.latencyTimer(latency));
	}

	@Override
	public int latencyTimer() throws IOException {
		return device.applyValid(Ftdi::latencyTimer);
	}

	@Override
	public void readChunkSize(int size) throws IOException {
		ftdiConfig.readChunkSize(size);
		device.acceptValid(f -> f.readChunkSize(size));
	}

	@Override
	public int readChunkSize() throws IOException {
		return device.applyValid(Ftdi::readChunkSize);
	}

	@Override
	public void writeChunkSize(int size) throws IOException {
		ftdiConfig.writeChunkSize(size);
		device.acceptValid(f -> f.writeChunkSize(size));
	}

	@Override
	public int writeChunkSize() throws IOException {
		return device.applyValid(Ftdi::writeChunkSize);
	}

	@Override
	public void purgeReadBuffer() throws IOException {
		device.acceptValid(Ftdi::purgeReadBuffer);
	}

	@Override
	public void purgeWriteBuffer() throws IOException {
		device.acceptValid(Ftdi::purgeWriteBuffer);
	}

	@Override
	public FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException {
		return device.applyValid(f -> f.readSubmit(buffer, len));
	}

	@Override
	public FtdiTransferControl writeSubmit(Pointer buffer, int len) throws IOException {
		return device.applyValid(f -> f.writeSubmit(buffer, len));
	}

	@Override
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws IOException {
		device.acceptValid(
			f -> f.readStream(callback, packetsPerTransfer, numTransfers, progressIntervalSec));
	}

	@Override
	protected Ftdi openConnector() throws IOException {
		Ftdi ftdi = null;
		try {
			ftdi = config.factory.open(config.finder, config.iface);
			ftdiConfig.build().apply(ftdi);
			return ftdi;
		} catch (RuntimeException | LibUsbException e) {
			LogUtil.close(ftdi);
			throw e;
		}
	}

}
