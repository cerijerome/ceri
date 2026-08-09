package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.clib.ffm.CString.wchar_t;

public class CStringTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CString.class);
	}

	@Test
	public void testWchar() {
		var wchars = wchar_t.$.ofAll('t', 'e', 's', 't');
		Assert.equal(wchars[0].value(), (long) 't');
		Assert.equal(wchars[1].value(), (long) 'e');
		Assert.equal(wchars[2].value(), (long) 's');
		Assert.equal(wchars[3].value(), (long) 't');
	}
}
