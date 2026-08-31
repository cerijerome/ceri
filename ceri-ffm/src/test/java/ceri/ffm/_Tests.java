package ceri.ffm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.Testing;

/**
 * Generated test suite for ceri-ffm
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib.ffm
	ceri.ffm.clib.ffm.CErrNoTest.class, //
	ceri.ffm.clib.ffm.CExceptionTest.class, //
	ceri.ffm.clib.ffm.CFcntlTest.class, //
	ceri.ffm.clib.ffm.CIoctlTest.class, //
	ceri.ffm.clib.ffm.CLibTest.class, //
	ceri.ffm.clib.ffm.CMmanTest.class, //
	ceri.ffm.clib.ffm.CPollTest.class, //
	ceri.ffm.clib.ffm.CSignalTest.class, //
	ceri.ffm.clib.ffm.CStdlibTest.class, //
	ceri.ffm.clib.ffm.CStringTest.class, //
	ceri.ffm.clib.ffm.CTermiosTest.class, //
	ceri.ffm.clib.ffm.CTimeTest.class, //
	ceri.ffm.clib.ffm.CUnistdTest.class, //
	// clib.test
	ceri.ffm.clib.test.CLibVerifierBehavior.class, //
	ceri.ffm.clib.test.TestCLibNativeBehavior.class, //
	// core
	ceri.ffm.core.DecoderBehavior.class, //
	ceri.ffm.core.LastErrorTest.class, //
	// reflect
	ceri.ffm.reflect.CAnnotationsTest.class, //
	ceri.ffm.reflect.CSymbolGenBehavior.class, //
	ceri.ffm.reflect.RefineBehavior.class, //
	ceri.ffm.reflect.TypeNodeBehavior.class, //
	// type
	ceri.ffm.type.IntTypeBehavior.class, //
	ceri.ffm.type.TerminatorBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		Testing.exec(_Tests.class);
	}
}
