package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// comm
	ceri.serial.comm.ParityBehavior.class, //
	ceri.serial.comm.SerialBehavior.class, //
	ceri.serial.comm.SerialEventBehavior.class, //
	ceri.serial.comm.SerialParamsBehavior.class, //
	ceri.serial.comm.SerialPortBehavior.class, //
	ceri.serial.comm.StopBitsBehavior.class, //
	// comm.jna
	ceri.serial.comm.jna.CSerialTest.class, //
	// comm.test
	ceri.serial.comm.test.SerialTesterBehavior.class, //
	ceri.serial.comm.test.TestSerialBehavior.class, //
	// comm.util
	ceri.serial.comm.util.MacUsbLocatorBehavior.class, //
	ceri.serial.comm.util.PortSupplierBehavior.class, //
	ceri.serial.comm.util.ReplaceableSerialBehavior.class, //
	ceri.serial.comm.util.SelfHealingSerialBehavior.class, //
	ceri.serial.comm.util.SerialConfigBehavior.class, //
	ceri.serial.comm.util.SerialPortLocatorBehavior.class, //
	ceri.serial.comm.util.SerialPropertiesBehavior.class, //
	// ftdi
	ceri.serial.ftdi.FtdiBehavior.class, //
	ceri.serial.ftdi.FtdiBitModeBehavior.class, //
	ceri.serial.ftdi.FtdiDeviceBehavior.class, //
	ceri.serial.ftdi.FtdiLineParamsBehavior.class, //
	ceri.serial.ftdi.FtdiProgressInfoBehavior.class, //
	// ftdi.jna
	ceri.serial.ftdi.jna.LibFtdiBaudBehavior.class, //
	ceri.serial.ftdi.jna.LibFtdiStreamTest.class, //
	ceri.serial.ftdi.jna.LibFtdiTest.class, //
	ceri.serial.ftdi.jna.LibFtdiUtilTest.class, //
	// ftdi.test
	ceri.serial.ftdi.test.TestFtdiBehavior.class, //
	// ftdi.util
	ceri.serial.ftdi.util.FtdiConfigBehavior.class, //
	ceri.serial.ftdi.util.FtdiPropertiesBehavior.class, //
	ceri.serial.ftdi.util.SelfHealingFtdiBehavior.class, //
	// i2c
	ceri.serial.i2c.DeviceIdBehavior.class, //
	ceri.serial.i2c.I2cAddressBehavior.class, //
	ceri.serial.i2c.I2cBehavior.class, //
	ceri.serial.i2c.I2cDeviceBehavior.class, //
	// i2c.jna
	ceri.serial.i2c.jna.I2cDevTest.class, //
	// i2c.smbus
	ceri.serial.i2c.smbus.SmBusBehavior.class, //
	ceri.serial.i2c.smbus.SmBusDeviceBehavior.class, //
	ceri.serial.i2c.smbus.SmBusEmulatorBehavior.class, //
	ceri.serial.i2c.smbus.SmBusI2cBehavior.class, //
	// i2c.util
	ceri.serial.i2c.util.I2cEmulatorBehavior.class, //
	ceri.serial.i2c.util.I2cUtilTest.class, //
	// libusb
	ceri.serial.libusb.UsbBehavior.class, //
	ceri.serial.libusb.UsbDescriptorsBehavior.class, //
	ceri.serial.libusb.UsbDeviceBehavior.class, //
	ceri.serial.libusb.UsbDeviceHandleBehavior.class, //
	ceri.serial.libusb.UsbEventsBehavior.class, //
	ceri.serial.libusb.UsbHotPlugBehavior.class, //
	ceri.serial.libusb.UsbTransferBehavior.class, //
	// libusb.jna
	ceri.serial.libusb.jna.LibUsbAudioTest.class, //
	ceri.serial.libusb.jna.LibUsbExceptionBehavior.class, //
	ceri.serial.libusb.jna.LibUsbFinderBehavior.class, //
	ceri.serial.libusb.jna.LibUsbTerminalTypeTest.class, //
	ceri.serial.libusb.jna.LibUsbTest.class, //
	ceri.serial.libusb.jna.LibUsbUtilTest.class, //
	// libusb.test
	ceri.serial.libusb.test.LibUsbSampleDataTest.class, //
	ceri.serial.libusb.test.LibUsbTestDataBehavior.class, //
	ceri.serial.libusb.test.TestLibUsbNativeBehavior.class, //
	// spi
	ceri.serial.spi.SpiBehavior.class, //
	ceri.serial.spi.SpiDeviceBehavior.class, //
	ceri.serial.spi.SpiModeBehavior.class, //
	// spi.jna
	ceri.serial.spi.jna.SpiDevTest.class, //
	ceri.serial.spi.jna.SpiDevUtilTest.class, //
	// spi.pulse
	ceri.serial.spi.pulse.PulseBufferBehavior.class, //
	ceri.serial.spi.pulse.PulseCycleBehavior.class, //
	ceri.serial.spi.pulse.SpiPulseConfigBehavior.class, //
	ceri.serial.spi.pulse.SpiPulseTransmitterBehavior.class, //
	// spi.test
	ceri.serial.spi.test.TestSpiBehavior.class, //
	// spi.util
	ceri.serial.spi.util.SpiEmulatorBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
