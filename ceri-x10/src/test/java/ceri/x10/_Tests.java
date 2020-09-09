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
	// cm11a.protocol
	ceri.x10.cm11a.protocol.DataBehavior.class, //
	ceri.x10.cm11a.protocol.InputBufferBehavior.class, //
	ceri.x10.cm11a.protocol.ReadDataBehavior.class, //
	ceri.x10.cm11a.protocol.ReadStatusBehavior.class, //
	ceri.x10.cm11a.protocol.WriteDataBehavior.class, //
	ceri.x10.cm11a.protocol.WriteStatusBehavior.class, //
	// cm17a.device
	ceri.x10.cm17a.device.Cm17aDeviceBehavior.class, //
	ceri.x10.cm17a.device.DataTest.class, //
	ceri.x10.cm17a.device.ProcessorBehavior.class, //
	// command
	ceri.x10.command.CommandDispatcherBehavior.class, //
	ceri.x10.command.CommandListenerBehavior.class, //
	ceri.x10.command.DimCommandBehavior.class, //
	// type
	ceri.x10.type.AddressBehavior.class, //
	ceri.x10.type.ExtFunctionBehavior.class, //
	ceri.x10.type.FunctionBehavior.class, //
	ceri.x10.type.UnitBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
