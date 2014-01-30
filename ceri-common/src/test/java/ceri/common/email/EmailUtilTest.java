package ceri.common.email;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EmailUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EmailUtil.class);
	}

	@Test
	public void testValidEmails() {
		assertTrue(EmailUtil.isValid("a@y.zz"));
		assertTrue(EmailUtil.isValid("a.b@y.zz"));
		assertTrue(EmailUtil.isValid("a@x.y.zz"));
	}

	@Test
	public void testInvalidEmails() {
		assertFalse(EmailUtil.isValid("@zzz"));
		assertFalse(EmailUtil.isValid("yyy"));
		assertFalse(EmailUtil.isValid("yyy@"));
		assertFalse(EmailUtil.isValid("a@z"));
		assertFalse(EmailUtil.isValid("a@z."));
		assertFalse(EmailUtil.isValid("a@yy.z"));
		assertFalse(EmailUtil.isValid("a@y.z."));
		assertFalse(EmailUtil.isValid("a@x.y.z"));
	}

}
