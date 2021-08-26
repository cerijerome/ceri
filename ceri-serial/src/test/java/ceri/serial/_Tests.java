package ceri.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-serial
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	ceri.serial.clib.CFileDescriptorBehavior.class, //
	ceri.serial.clib.FileDescriptorBehavior.class, //
	ceri.serial.clib.ModeBehavior.class, //
	ceri.serial.clib.OpenFlagBehavior.class, //
	ceri.serial.clib.SeekBehavior.class, //
	// clib.jna
	ceri.serial.clib.jna.CLibBehavior.class, //
	// clib.util
	ceri.serial.clib.util.ResponseFdBehavior.class, //
	// ftdi
	ceri.serial.ftdi.FtdiBehavior.class, //
	// ftdi.util
	ceri.serial.ftdi.util.SelfHealingFtdiConfigBehavior.class, //
	ceri.serial.ftdi.util.SelfHealingFtdiConnectorBehavior.class, //
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
	// javax.util
	ceri.serial.javax.util.CommPortSupplierBehavior.class, //
	ceri.serial.javax.util.ConnectorNotSetExceptionBehavior.class, //
	ceri.serial.javax.util.ReplaceableSerialConnectorBehavior.class, //
	ceri.serial.javax.util.SelfHealingSerialConfigBehavior.class, //
	ceri.serial.javax.util.SelfHealingSerialConnectorBehavior.class, //
	// jna
	ceri.serial.jna.JnaEnumTest.class, //
	ceri.serial.jna.JnaUtilTest.class, //
	// jna.test
	ceri.serial.jna.test.JnaTestUtilTest.class, //
	// libusb
	ceri.serial.libusb.UsbBehavior.class, //
	// libusb.jna
	ceri.serial.libusb.jna.LibUsbFinderBehavior.class, //
	// spi
	ceri.serial.spi.SpiBehavior.class, //
	ceri.serial.spi.SpiDeviceBehavior.class, //
	ceri.serial.spi.SpiDeviceConfigBehavior.class, //
	ceri.serial.spi.SpiModeBehavior.class, //
	// spi.jna
	ceri.serial.spi.jna.SpiDevTest.class, //
	ceri.serial.spi.jna.SpiDevUtilTest.class, //
	// spi.pulse
	ceri.serial.spi.pulse.PulseBufferBehavior.class, //
	ceri.serial.spi.pulse.PulseCycleBehavior.class, //
	ceri.serial.spi.pulse.PulseStatsBehavior.class, //
	ceri.serial.spi.pulse.SpiPulseConfigBehavior.class, //
	ceri.serial.spi.pulse.SpiPulseTransmitterBehavior.class, //
	// spi.util
	ceri.serial.spi.util.SpiEmulatorBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
