package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.test.FfmTesting;

public class CErrNoTest {

	@Test
	public void testNormalizedCodes() {
		Assert.unordered(CErrNo.codes(-1, 0, 1, 2, -1, -2), 0, 1, 2, -2);
		CErrNo[] errors = { CErrNo.E2BIG, CErrNo.EUCLEAN, CErrNo.EAUTH };
		var set = CErrNo.codes(errors);
		for (var error : errors)
			Assert.equal(set.contains(error.code), error.defined());
	}

	@Test
	public void testFromException() {
		var ex = CException.of(CErrNo.ENOENT.code, "test");
		Assert.equal(CErrNo.from(ex), CErrNo.ENOENT);
	}

	@Test
	public void testToException() {
		Assert.throwable(CErrNo.ENOENT.error(), CException.class, "\\[2\\] ENOENT");
		Assert.throwable(CErrNo.ENOENT.error("%s", "test"), CException.class,
			"\\[2\\] ENOENT; test");
	}

	@Test
	public void testValue() {
		Assert.equal(new CErrNo.Value(-1).errNo(), CErrNo.UNDEFINED);
		Assert.equal(new CErrNo.Value(CErrNo.EBADF.code).errNo(), CErrNo.EBADF);
		Assert.string(new CErrNo.Value(-1), "-1");
		Assert.string(new CErrNo.Value(99999), "99999");
		Assert.string(new CErrNo.Value(CErrNo.EBADF.code), "EBADF");
	}

	@Test
	public void testOsCoverage() {
		FfmTesting.testForEachOs(CErrNo.class);
	}
}
