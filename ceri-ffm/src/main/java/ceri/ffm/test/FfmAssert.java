package ceri.ffm.test;

import java.lang.foreign.MemorySegment;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.core.ErrNo;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Primitive;

public class FfmAssert {

	private FfmAssert() {}
	
	public static MemorySegment memory(MemorySegment actual, int...expected) {
		Assert.array(Primitive.BYTE.getArray(actual, false), expected);
		return actual;
	}
	
	public static void clong(IntType.CLong actual, Number expected) {
		if (expected == null) Assert.isNull(actual);
		Assert.equal(actual, new IntType.CLong(expected));
	}
	
	public static void culong(IntType.CUlong actual, Number expected) {
		if (expected == null) Assert.isNull(actual);
		Assert.equal(actual, new IntType.CUlong(expected));
	}
	
	public static int result(int result, int expected, CErrNo errNo) {
		Assert.equal(result, expected);
		Assert.equal(ErrNo.get(), errNo.code);
		return result;
	}
	
	public static void cexception(CErrNo errNo, Excepts.Runnable<Exception> runnable) {
		cexception(errNo.code, runnable);
	}
	
	public static void cexception(int code, Excepts.Runnable<Exception> runnable) {
		Assert.thrown(CException.class, e -> Assert.equal(e.code, code), runnable);
	}	
}
