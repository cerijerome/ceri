package ceri.x10;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-x10
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// cm11a
	ceri.x10.cm11a.Cm11aConfigBehavior.class, //
	ceri.x10.cm11a.Cm11aContainerBehavior.class, //
	// cm11a.device
	ceri.x10.cm11a.device.Cm11aBehavior.class, //
	ceri.x10.cm11a.device.Cm11aDeviceBehavior.class, //
	ceri.x10.cm11a.device.Cm11aDeviceConfigBehavior.class, //
	ceri.x10.cm11a.device.Cm11aEmulatorBehavior.class, //
	// cm11a.protocol
	ceri.x10.cm11a.protocol.ClockBehavior.class, //
	ceri.x10.cm11a.protocol.DataBehavior.class, //
	ceri.x10.cm11a.protocol.EntryBehavior.class, //
	ceri.x10.cm11a.protocol.EntryBufferBehavior.class, //
	ceri.x10.cm11a.protocol.EntryCollectorBehavior.class, //
	ceri.x10.cm11a.protocol.ReceiveTest.class, //
	ceri.x10.cm11a.protocol.StatusBehavior.class, //
	ceri.x10.cm11a.protocol.TransmitTest.class, //
	// cm17a
	ceri.x10.cm17a.Cm17aConfigBehavior.class, //
	ceri.x10.cm17a.Cm17aContainerBehavior.class, //
	// cm17a.device
	ceri.x10.cm17a.device.Cm17aBehavior.class, //
	ceri.x10.cm17a.device.Cm17aConnectorBehavior.class, //
	ceri.x10.cm17a.device.Cm17aDeviceBehavior.class, //
	ceri.x10.cm17a.device.Cm17aDeviceConfigBehavior.class, //
	ceri.x10.cm17a.device.Cm17aEmulatorBehavior.class, //
	ceri.x10.cm17a.device.DataTest.class, //
	// command
	ceri.x10.command.AddressBehavior.class, //
	ceri.x10.command.CommandBehavior.class, //
	ceri.x10.command.CommandListenerBehavior.class, //
	ceri.x10.command.FunctionTypeBehavior.class, //
	ceri.x10.command.UnitBehavior.class, //
	// util
	ceri.x10.util.X10ControllerBehavior.class, //
	ceri.x10.util.X10ControllerTypeBehavior.class, //
	ceri.x10.util.X10UtilTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
