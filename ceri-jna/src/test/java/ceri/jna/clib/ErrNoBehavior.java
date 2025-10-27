package ceri.jna.clib;

import org.junit.Test;
import com.sun.jna.LastErrorException;
import ceri.common.data.TypeValue;
import ceri.common.test.Assert;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CException;

public class ErrNoBehavior {

	@Test
	public void shouldFindFromLastErrorException() {
		Assert.equal(ErrNo.from(new LastErrorException("test")), ErrNo.UNDEFINED);
		Assert.equal(ErrNo.from(ErrNo.EIO.lastError()), ErrNo.EIO);
	}

	@Test
	public void shouldFindFromCException() {
		Assert.equal(ErrNo.from(CException.general("test")), ErrNo.UNDEFINED);
		Assert.equal(ErrNo.from(ErrNo.EIO.error()), ErrNo.EIO);
	}

	@Test
	public void shouldDetermineIfDefined() {
		for (ErrNo error : ErrNo.values()) {
			Assert.equal(error.defined(), error.code >= 0, error.name());
		}
	}

	@Test
	public void shouldProvideTypeValue() {
		Assert.equal(ErrNo.value(CErrNo.EIO), TypeValue.of(CErrNo.EIO, ErrNo.EIO, null));
		Assert.equal(ErrNo.value(1000), TypeValue.of(1000, ErrNo.UNDEFINED, null));
		Assert.equal(ErrNo.value(-1), TypeValue.of(-1, ErrNo.UNDEFINED, null));
	}

	@Test
	public void shouldLookupByCode() {
		Assert.equal(ErrNo.from(-2), ErrNo.UNDEFINED);
		Assert.equal(ErrNo.from(-1), ErrNo.UNDEFINED);
		Assert.equal(ErrNo.from(ErrNo.EAGAIN.code), ErrNo.EAGAIN);
	}

	@Test
	public void shouldProvideLastErrorMessage() {
		Assert.string(ErrNo.EIO.lastError("test"), "%s: [%d] test",
			LastErrorException.class.getName(), ErrNo.EIO.code);
	}
}
