package ceri.jna.util;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.util.OsUtil;

public class JnaOsBehavior {
	private Functions.Closeable override;

	@After
	public void after() {
		Closeables.close(override);
		override = null;
	}

	@Test
	public void testKnown() {
		Assert.equal(JnaOs.known(null), false);
	}

	@Test
	public void testCompatible() {
		Assert.equal(JnaOs.compatible(JnaOs.unknown, JnaOs.unknown), true);
		Assert.equal(JnaOs.compatible(JnaOs.unknown, JnaOs.linux), true);
		Assert.equal(JnaOs.compatible(JnaOs.unknown, JnaOs.mac), true);
		Assert.equal(JnaOs.compatible(JnaOs.linux, JnaOs.unknown), true);
		Assert.equal(JnaOs.compatible(JnaOs.linux, JnaOs.linux), true);
		Assert.equal(JnaOs.compatible(JnaOs.linux, JnaOs.mac), false);
		Assert.equal(JnaOs.compatible(JnaOs.mac, JnaOs.unknown), true);
		Assert.equal(JnaOs.compatible(JnaOs.mac, JnaOs.linux), false);
		Assert.equal(JnaOs.compatible(JnaOs.mac, JnaOs.mac), true);
	}

	@Test
	public void testCurrentCompatibility() {
		override = OsUtil.os("test", null, null);
		Assert.equal(JnaOs.current(JnaOs.unknown), true);
		Assert.equal(JnaOs.current(JnaOs.mac), true);
		Assert.equal(JnaOs.current(JnaOs.linux), true);
		override = JnaOs.linux.override();
		Assert.equal(JnaOs.current(JnaOs.unknown), true);
		Assert.equal(JnaOs.current(JnaOs.mac), false);
		Assert.equal(JnaOs.current(JnaOs.linux), true);
	}

	@Test
	public void testCurrent() {
		override = JnaOs.mac.override();
		Assert.equal(JnaOs.validCurrent(), JnaOs.mac);
		override = JnaOs.linux.override();
		Assert.equal(JnaOs.validCurrent(), JnaOs.linux);
		override = OsUtil.os("test", null, null);
		Assert.thrown(() -> JnaOs.validCurrent());
	}

	@Test
	public void testFrom() {
		Assert.equal(JnaOs.from(null), JnaOs.unknown);
	}

	@Test
	public void shouldModifyFilename() {
		Assert.equal(JnaOs.mac.file(null), null);
		Assert.equal(JnaOs.unknown.file("file.c"), "file.c");
		Assert.equal(JnaOs.linux.file("file"), "file-linux");
		Assert.equal(JnaOs.linux.file("file.c"), "file-linux.c");
	}
}
