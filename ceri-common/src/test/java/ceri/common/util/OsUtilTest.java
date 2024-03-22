package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class OsUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(OsUtil.class);
	}

	@Test
	public void testDescriptor() {
		assertMatch(OsUtil.os().toString(), ".+;.+;.+");
		assertMatch(OsUtil.os().full(), ".+;.+;.+; mac.*linux.*x86.*arm.*bit64.*");
	}

	@Test
	public void testAws() {
		try (var x = SystemVars.removable("AWS_PATH", "x")) {
			assertTrue(OsUtil.aws());
		}
		try (var x = SystemVars.removable("AWS_PATH", "")) {
			assertFalse(OsUtil.aws());
		}
		try (var x = SystemVars.removable("AWS_PATH", null)) {
			assertFalse(OsUtil.aws());
		}
	}

	@Test
	public void testNameOverride() {
		var orig = OsUtil.os();
		try (var x = OsUtil.os("Mac", null, null)) {
			assertOs(OsUtil.os(), "Mac", orig.arch, orig.version);
			assertTrue(OsUtil.os().mac);
			assertFalse(OsUtil.os().linux);
		}
		try (var x = OsUtil.os("Darwin", null, null)) {
			assertOs(OsUtil.os(), "Darwin", orig.arch, orig.version);
			assertTrue(OsUtil.os().mac);
			assertFalse(OsUtil.os().linux);
		}
		try (var x = OsUtil.os("Linux", null, null)) {
			assertOs(OsUtil.os(), "Linux", orig.arch, orig.version);
			assertFalse(OsUtil.os().mac);
			assertTrue(OsUtil.os().linux);
		}
		try (var x = OsUtil.os("Other", null, null)) {
			assertOs(OsUtil.os(), "Other", orig.arch, orig.version);
			assertFalse(OsUtil.os().mac);
			assertFalse(OsUtil.os().linux);
		}
	}

	@Test
	public void testArchOverride() {
		var orig = OsUtil.os();
		try (var x = OsUtil.os(null, "x86", null)) {
			assertOs(OsUtil.os(), orig.name, "x86", orig.version);
			assertTrue(OsUtil.os().x86);
			assertFalse(OsUtil.os().arm);
			assertFalse(OsUtil.os().bit64);
		}
		try (var x = OsUtil.os(null, "aarch", null)) {
			assertOs(OsUtil.os(), orig.name, "aarch", orig.version);
			assertFalse(OsUtil.os().x86);
			assertTrue(OsUtil.os().arm);
			assertFalse(OsUtil.os().bit64);
		}
		try (var x = OsUtil.os(null, "aarch64", null)) {
			assertOs(OsUtil.os(), orig.name, "aarch64", orig.version);
			assertFalse(OsUtil.os().x86);
			assertTrue(OsUtil.os().arm);
			assertTrue(OsUtil.os().bit64);
		}
		try (var x = OsUtil.os(null, "xxx", null)) {
			assertOs(OsUtil.os(), orig.name, "xxx", orig.version);
			assertFalse(OsUtil.os().x86);
			assertFalse(OsUtil.os().arm);
			assertFalse(OsUtil.os().bit64);
		}
	}

	@Test
	public void testVersionOverride() {
		var orig = OsUtil.os();
		try (var x = OsUtil.os(null, null, "-999")) {
			assertOs(OsUtil.os(), orig.name, orig.arch, "-999");
		}
	}

	private static void assertOs(OsUtil.Os os, String name, String arch, String version) {
		assertEquals(os.name, name, "name");
		assertEquals(os.arch, arch, "name");
		assertEquals(os.version, version, "name");
	}
}
