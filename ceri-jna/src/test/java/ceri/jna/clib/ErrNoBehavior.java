package ceri.jna.clib;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertString;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import ceri.common.data.TypeValue;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CException;

public class ErrNoBehavior {

	@Test
	public void shouldFindFromLastErrorException() {
		assertEquals(ErrNo.from(new LastErrorException("test")), ErrNo.UNDEFINED);
		assertEquals(ErrNo.from(ErrNo.EIO.lastError()), ErrNo.EIO);
	}

	@Test
	public void shouldFindFromCException() {
		assertEquals(ErrNo.from(CException.general("test")), ErrNo.UNDEFINED);
		assertEquals(ErrNo.from(ErrNo.EIO.error()), ErrNo.EIO);
	}

	@Test
	public void shouldDetermineIfDefined() {
		for (ErrNo error : ErrNo.values()) {
			assertEquals(error.defined(), error.code >= 0, error.name());
		}
	}

	@Test
	public void shouldProvideTypeValue() {
		assertEquals(ErrNo.value(CErrNo.EIO), TypeValue.of(CErrNo.EIO, ErrNo.EIO, null));
		assertEquals(ErrNo.value(1000), TypeValue.of(1000, ErrNo.UNDEFINED, null));
		assertEquals(ErrNo.value(-1), TypeValue.of(-1, ErrNo.UNDEFINED, null));
	}

	@Test
	public void shouldLookupByCode() {
		assertEquals(ErrNo.from(-2), ErrNo.UNDEFINED);
		assertEquals(ErrNo.from(-1), ErrNo.UNDEFINED);
		assertEquals(ErrNo.from(ErrNo.EAGAIN.code), ErrNo.EAGAIN);
	}

	@Test
	public void shouldProvideLastErrorMessage() {
		assertString(ErrNo.EIO.lastError("test"), "%s: [%d] test",
			LastErrorException.class.getName(), ErrNo.EIO.code);
	}
}
