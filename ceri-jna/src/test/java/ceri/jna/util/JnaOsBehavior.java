package ceri.jna.util;

import static ceri.common.test.Assert.assertEquals;
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
		assertEquals(JnaOs.known(null), false);
	}

	@Test
	public void testCompatible() {
		assertEquals(JnaOs.compatible(JnaOs.unknown, JnaOs.unknown), true);
		assertEquals(JnaOs.compatible(JnaOs.unknown, JnaOs.linux), true);
		assertEquals(JnaOs.compatible(JnaOs.unknown, JnaOs.mac), true);
		assertEquals(JnaOs.compatible(JnaOs.linux, JnaOs.unknown), true);
		assertEquals(JnaOs.compatible(JnaOs.linux, JnaOs.linux), true);
		assertEquals(JnaOs.compatible(JnaOs.linux, JnaOs.mac), false);
		assertEquals(JnaOs.compatible(JnaOs.mac, JnaOs.unknown), true);
		assertEquals(JnaOs.compatible(JnaOs.mac, JnaOs.linux), false);
		assertEquals(JnaOs.compatible(JnaOs.mac, JnaOs.mac), true);
	}

	@Test
	public void testCurrentCompatibility() {
		override = OsUtil.os("test", null, null);
		assertEquals(JnaOs.current(JnaOs.unknown), true);
		assertEquals(JnaOs.current(JnaOs.mac), true);
		assertEquals(JnaOs.current(JnaOs.linux), true);
		override = JnaOs.linux.override();
		assertEquals(JnaOs.current(JnaOs.unknown), true);
		assertEquals(JnaOs.current(JnaOs.mac), false);
		assertEquals(JnaOs.current(JnaOs.linux), true);
	}

	@Test
	public void testCurrent() {
		override = JnaOs.mac.override();
		assertEquals(JnaOs.validCurrent(), JnaOs.mac);
		override = JnaOs.linux.override();
		assertEquals(JnaOs.validCurrent(), JnaOs.linux);
		override = OsUtil.os("test", null, null);
		Assert.thrown(() -> JnaOs.validCurrent());
	}

	@Test
	public void testFrom() {
		assertEquals(JnaOs.from(null), JnaOs.unknown);
	}

	@Test
	public void shouldModifyFilename() {
		assertEquals(JnaOs.mac.file(null), null);
		assertEquals(JnaOs.unknown.file("file.c"), "file.c");
		assertEquals(JnaOs.linux.file("file"), "file-linux");
		assertEquals(JnaOs.linux.file("file.c"), "file-linux.c");
	}
}
