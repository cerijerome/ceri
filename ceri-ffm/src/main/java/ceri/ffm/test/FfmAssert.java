package ceri.ffm.test;

import java.lang.foreign.MemorySegment;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.core.ErrNo;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

public class FfmAssert {

	private FfmAssert() {}
	
	public static MemorySegment memory(MemorySegment actual, int...expected) {
		Assert.array(Primitive.BYTE.getArray(actual, false), expected);
		return actual;
	}
	
	@SafeVarargs
	public static <T> Pointer<T> pointers(Pointer<T> actual, T... expecteds) {
		Assert.array(actual.getArray(expecteds.length, false), expecteds);
		return actual;
	}
	
	public static <T> Pointer<T> pointer(Pointer<T> actual, T expected) {
		Assert.equal(actual.get(), expected);
		return actual;
	}
	
	public static <T extends IntType<T>> T equal(T actual, long expected) {
		Assert.equal(actual.value(), expected);
		return actual;
	}
	
	public static <T> T result(T result, T expected, CErrNo errNo) {
		Assert.equal(result, expected);
		Assert.equal(ErrNo.get(), errNo.code);
		return result;
	}
	
	public static void cexception(Excepts.Runnable<Exception> runnable) {
		Assert.thrown(CException.class, runnable);
	}	
	
	public static void cexception(CErrNo errNo, Excepts.Runnable<Exception> runnable) {
		cexception(errNo.code, runnable);
	}
	
	public static void cexception(int code, Excepts.Runnable<Exception> runnable) {
		Assert.thrown(CException.class, e -> Assert.equal(e.code, code), runnable);
	}	
}
