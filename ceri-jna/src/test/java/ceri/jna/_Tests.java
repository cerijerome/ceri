package ceri.jna;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-jna
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	ceri.jna.clib.CFileDescriptorBehavior.class, //
	ceri.jna.clib.FileDescriptorBehavior.class, //
	ceri.jna.clib.ModeBehavior.class, //
	ceri.jna.clib.OpenFlagBehavior.class, //
	ceri.jna.clib.SeekBehavior.class, //
	// clib.jna
	ceri.jna.clib.jna.CCallerBehavior.class, //
	ceri.jna.clib.jna.CErrorBehavior.class, //
	ceri.jna.clib.jna.CExceptionBehavior.class, //
	ceri.jna.clib.jna.CLibTest.class, //
	ceri.jna.clib.jna.CTimeTest.class, //
	// clib.test
	ceri.jna.clib.test.TestCLibNativeBehavior.class, //
	ceri.jna.clib.test.TestFileDescriptorBehavior.class, //
	// clib.util
	ceri.jna.clib.util.SelfHealingFdBehavior.class, //
	ceri.jna.clib.util.SelfHealingFdConfigBehavior.class, //
	// test
	ceri.jna.test.JnaTestUtilTest.class, //
	// util
	ceri.jna.util.ArrayPointerBehavior.class, //
	ceri.jna.util.JnaArgsBehavior.class, //
	ceri.jna.util.JnaEnumTest.class, //
	ceri.jna.util.JnaLibraryBehavior.class, //
	ceri.jna.util.JnaMemoryBehavior.class, //
	ceri.jna.util.JnaUtilTest.class, //
	ceri.jna.util.PointerUtilTest.class, //
	ceri.jna.util.StructBehavior.class, //
	ceri.jna.util.StructFieldTest.class, //
	ceri.jna.util.UnionFieldTest.class, //
	ceri.jna.util.VarStructBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
