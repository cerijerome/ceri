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
	ceri.ffm.clib.ffm.CUnistdTest.class, //
	// core
	ceri.ffm.core.DecoderBehavior.class, //
	// type
	ceri.ffm.type.IntTypeBehavior.class, //
	ceri.ffm.type.TerminatorBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		Testing.exec(_Tests.class);
	}
}
