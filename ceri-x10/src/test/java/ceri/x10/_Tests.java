package ceri.x10;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.x10.cm11a.Cm11aControllerBehavior;
import ceri.x10.cm11a.EntryBehavior;
import ceri.x10.cm11a.EntryDispatcherBehavior;
import ceri.x10.cm11a.protocol.InputBufferBehavior;
import ceri.x10.cm11a.protocol.ReadDataBehavior;
import ceri.x10.cm11a.protocol.ReadStatusBehavior;
import ceri.x10.cm11a.protocol.WriteDataBehavior;
import ceri.x10.cm11a.protocol.WriteStatusBehavior;
import ceri.x10.cm17a.Cm17aControllerBehavior;
import ceri.x10.cm17a.CommandsBehavior;
import ceri.x10.cm17a.ProcessorBehavior;
import ceri.x10.command.CommandDispatcherBehavior;
import ceri.x10.command.DimCommandBehavior;
import ceri.x10.type.AddressBehavior;
import ceri.x10.type.ExtFunctionBehavior;
import ceri.x10.type.FunctionBehavior;
import ceri.x10.type.UnitBehavior;
import ceri.x10.util.UnexpectedByteExceptionBehavior;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// cm11a
	Cm11aControllerBehavior.class,
	EntryBehavior.class,
	EntryDispatcherBehavior.class,
	ceri.x10.cm11a.ProcessorBehavior.class,
	// cm11a.protocol
	InputBufferBehavior.class,
	ReadDataBehavior.class,
	ReadStatusBehavior.class,
	WriteDataBehavior.class,
	WriteStatusBehavior.class,
	// cm17a
	Cm17aControllerBehavior.class,
	CommandsBehavior.class,
	ProcessorBehavior.class,
	// command
	CommandDispatcherBehavior.class,
	DimCommandBehavior.class,
	// type
	AddressBehavior.class,
	ExtFunctionBehavior.class,
	FunctionBehavior.class,
	UnitBehavior.class,
	// util
	UnexpectedByteExceptionBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
