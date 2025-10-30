package ceri.common.net;

import org.junit.Test;
import ceri.common.test.Assert;

public class EmailsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Emails.class);
	}

	@Test
	public void testValidEmails() {
		Assert.yes(Emails.isValid("a@y.zz"));
		Assert.yes(Emails.isValid("a.b@y.zz"));
		Assert.yes(Emails.isValid("a@x.y.zz"));
	}

	@Test
	public void testInvalidEmails() {
		Assert.no(Emails.isValid("@zzz"));
		Assert.no(Emails.isValid("yyy"));
		Assert.no(Emails.isValid("yyy@"));
		Assert.no(Emails.isValid("a@z"));
		Assert.no(Emails.isValid("a@z."));
		Assert.no(Emails.isValid("a@yy.z"));
		Assert.no(Emails.isValid("a@y.z."));
		Assert.no(Emails.isValid("a@x.y.z"));
	}
}
