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
	ceri.jna.clib.test.CSymbolGenBehavior.class, //
	ceri.jna.clib.test.TestCLibNativeBehavior.class, //
	ceri.jna.clib.test.TestFileDescriptorBehavior.class, //
	// clib.util
	ceri.jna.clib.util.SelfHealingFdBehavior.class, //
	ceri.jna.clib.util.SyncPipeBehavior.class, //
	ceri.jna.clib.util.TtyInputBehavior.class, //
	// io
	ceri.jna.io.JnaInputStreamBehavior.class, //
	ceri.jna.io.JnaOutputStreamBehavior.class, //
	// test
	ceri.jna.test.JnaTestUtilTest.class, //
	// util
	ceri.jna.util.ArrayPointerBehavior.class, //
	ceri.jna.util.CallerBehavior.class, //
	ceri.jna.util.GcMemoryBehavior.class, //
	ceri.jna.util.IntTypeBehavior.class, //
	ceri.jna.util.JnaArgsBehavior.class, //
	ceri.jna.util.JnaEnumTest.class, //
	ceri.jna.util.JnaLibraryBehavior.class, //
	ceri.jna.util.JnaMemoryBehavior.class, //
	ceri.jna.util.JnaUtilTest.class, //
	ceri.jna.util.NulTermTest.class, //
	ceri.jna.util.PointerUtilTest.class, //
	ceri.jna.util.StructBehavior.class, //
	ceri.jna.util.StructFieldTest.class, //
	ceri.jna.util.ThreadBuffersBehavior.class, //
	ceri.jna.util.UnionBehavior.class, //
	ceri.jna.util.UnionFieldBehavior.class, //
	ceri.jna.util.VarStructBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
