package ceri.common.net;

import org.junit.Test;
import ceri.common.test.Assert;

public class EmailUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(EmailUtil.class);
	}

	@Test
	public void testValidEmails() {
		Assert.yes(EmailUtil.isValid("a@y.zz"));
		Assert.yes(EmailUtil.isValid("a.b@y.zz"));
		Assert.yes(EmailUtil.isValid("a@x.y.zz"));
	}

	@Test
	public void testInvalidEmails() {
		Assert.no(EmailUtil.isValid("@zzz"));
		Assert.no(EmailUtil.isValid("yyy"));
		Assert.no(EmailUtil.isValid("yyy@"));
		Assert.no(EmailUtil.isValid("a@z"));
		Assert.no(EmailUtil.isValid("a@z."));
		Assert.no(EmailUtil.isValid("a@yy.z"));
		Assert.no(EmailUtil.isValid("a@y.z."));
		Assert.no(EmailUtil.isValid("a@x.y.z"));
	}

}
