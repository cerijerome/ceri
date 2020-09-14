package ceri.x10;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-x10
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// cm11a.device
	ceri.x10.cm11a.device.Cm11aDeviceBehavior.class, //
	ceri.x10.cm11a.device.ProcessorBehavior.class, //
	// cm11a.protocol
	ceri.x10.cm11a.protocol.ClockBehavior.class, //
	ceri.x10.cm11a.protocol.DataBehavior.class, //
	ceri.x10.cm11a.protocol.StatusBehavior.class, //
	// cm17a.device
	ceri.x10.cm17a.device.Cm17aDeviceBehavior.class, //
	ceri.x10.cm17a.device.DataTest.class, //
	ceri.x10.cm17a.device.ProcessorBehavior.class, //
	// command
	ceri.x10.command.AddressBehavior.class, //
	ceri.x10.command.CommandBehavior.class, //
	ceri.x10.command.CommandListenerBehavior.class, //
	ceri.x10.command.FunctionTypeBehavior.class, //
	ceri.x10.command.UnitBehavior.class, //
	// util
	ceri.x10.util.X10ControllerTypeBehavior.class, //
	ceri.x10.util.X10UtilTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
