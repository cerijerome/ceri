package ceri.jna;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.Testing;

/**
 * Generated test suite for ceri-jna
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// clib
	ceri.jna.clib.CFileDescriptorBehavior.class, //
	ceri.jna.clib.CInputStreamBehavior.class, //
	ceri.jna.clib.COutputStreamBehavior.class, //
	ceri.jna.clib.ErrNoBehavior.class, //
	ceri.jna.clib.FileDescriptorBehavior.class, //
	ceri.jna.clib.MmapBehavior.class, //
	ceri.jna.clib.ModeBehavior.class, //
	ceri.jna.clib.PipeBehavior.class, //
	ceri.jna.clib.PollBehavior.class, //
	ceri.jna.clib.SeekBehavior.class, //
	ceri.jna.clib.SigSetBehavior.class, //
	ceri.jna.clib.SignalBehavior.class, //
	ceri.jna.clib.TermiosBehavior.class, //
	// clib.jna
	ceri.jna.clib.jna.CErrNoBehavior.class, //
	ceri.jna.clib.jna.CExceptionBehavior.class, //
	ceri.jna.clib.jna.CFcntlTest.class, //
	ceri.jna.clib.jna.CIoctlTest.class, //
	ceri.jna.clib.jna.CLibTest.class, //
	ceri.jna.clib.jna.CMmanTest.class, //
	ceri.jna.clib.jna.CPollTest.class, //
	ceri.jna.clib.jna.CSignalTest.class, //
	ceri.jna.clib.jna.CStdlibTest.class, //
	ceri.jna.clib.jna.CTermiosTest.class, //
	ceri.jna.clib.jna.CTimeTest.class, //
	ceri.jna.clib.jna.CUnistdTest.class, //
	ceri.jna.clib.jna.CUtilTest.class, //
	// clib.test
	ceri.jna.clib.test.CLibVerifierBehavior.class, //
	ceri.jna.clib.test.TestCLibNativeBehavior.class, //
	ceri.jna.clib.test.TestFileDescriptorBehavior.class, //
	// clib.util
	ceri.jna.clib.util.SelfHealingFdBehavior.class, //
	ceri.jna.clib.util.SyncPipeBehavior.class, //
	ceri.jna.clib.util.TtyInputBehavior.class, //
	// io
	ceri.jna.io.JnaInputStreamBehavior.class, //
	ceri.jna.io.JnaOutputStreamBehavior.class, //
	// reflect
	ceri.jna.reflect.CAnnotationsTest.class, //
	ceri.jna.reflect.CSymbolGenBehavior.class, //
	// test
	ceri.jna.test.JnaTestUtilTest.class, //
	// type
	ceri.jna.type.ArrayPointerBehavior.class, //
	ceri.jna.type.CLongBehavior.class, //
	ceri.jna.type.CUlongBehavior.class, //
	ceri.jna.type.IntTypeBehavior.class, //
	ceri.jna.type.JnaEnumTest.class, //
	ceri.jna.type.StructBehavior.class, //
	ceri.jna.type.StructFieldTest.class, //
	ceri.jna.type.UnionBehavior.class, //
	ceri.jna.type.UnionFieldBehavior.class, //
	ceri.jna.type.VarStructBehavior.class, //
	// util
	ceri.jna.util.CallerBehavior.class, //
	ceri.jna.util.GcMemoryBehavior.class, //
	ceri.jna.util.JnaArgsBehavior.class, //
	ceri.jna.util.JnaLibraryBehavior.class, //
	ceri.jna.util.JnaMemoryBehavior.class, //
	ceri.jna.util.JnaOsBehavior.class, //
	ceri.jna.util.JnaUtilTest.class, //
	ceri.jna.util.NulTermTest.class, //
	ceri.jna.util.PointerUtilTest.class, //
	ceri.jna.util.ThreadBuffersBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		Testing.exec(_Tests.class);
	}
}
