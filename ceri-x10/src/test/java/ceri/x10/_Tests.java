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
	ceri.x10.cm11a.device.EntryBehavior.class, //
	ceri.x10.cm11a.device.EntryDispatcherBehavior.class, //
	ceri.x10.cm11a.device.ProcessorBehavior.class, //
	// cm11a.entry
	ceri.x10.cm11a.entry.ClockBehavior.class, //
	ceri.x10.cm11a.entry.DataBehavior.class, //
	ceri.x10.cm11a.entry.InputBufferBehavior.class, //
	ceri.x10.cm11a.entry.ReadDataBehavior.class, //
	ceri.x10.cm11a.entry.StatusBehavior.class, //
	ceri.x10.cm11a.entry.WriteDataBehavior.class, //
	// cm17a.device
	ceri.x10.cm17a.device.Cm17aDeviceBehavior.class, //
	ceri.x10.cm17a.device.DataTest.class, //
	ceri.x10.cm17a.device.ProcessorBehavior.class, //
	// command
	ceri.x10.command.AddressBehavior.class, //
	ceri.x10.command.CommandDispatcherBehavior.class, //
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
