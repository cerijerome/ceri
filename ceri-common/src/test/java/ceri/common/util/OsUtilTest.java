package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class OsUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(OsUtil.class);
	}

	@Test
	public void testDescriptor() {
		Assert.match(OsUtil.os().toString(), ".+;.+;.+");
		Assert.match(OsUtil.os().full(), ".+;.+;.+; mac.*linux.*x86.*arm.*bit64.*");
	}

	@Test
	public void testAws() {
		try (var _ = SystemVars.removable("AWS_PATH", "x")) {
			Assert.yes(OsUtil.aws());
		}
		try (var _ = SystemVars.removable("AWS_PATH", "")) {
			Assert.no(OsUtil.aws());
		}
		try (var _ = SystemVars.removable("AWS_PATH", null)) {
			Assert.no(OsUtil.aws());
		}
	}

	@Test
	public void testNameOverride() {
		var orig = OsUtil.os();
		try (var _ = OsUtil.os("Mac", null, null)) {
			assertOs(OsUtil.os(), "Mac", orig.arch, orig.version);
			Assert.yes(OsUtil.os().mac);
			Assert.no(OsUtil.os().linux);
		}
		try (var _ = OsUtil.os("Darwin", null, null)) {
			assertOs(OsUtil.os(), "Darwin", orig.arch, orig.version);
			Assert.yes(OsUtil.os().mac);
			Assert.no(OsUtil.os().linux);
		}
		try (var _ = OsUtil.os("Linux", null, null)) {
			assertOs(OsUtil.os(), "Linux", orig.arch, orig.version);
			Assert.no(OsUtil.os().mac);
			Assert.yes(OsUtil.os().linux);
		}
		try (var _ = OsUtil.os("Other", null, null)) {
			assertOs(OsUtil.os(), "Other", orig.arch, orig.version);
			Assert.no(OsUtil.os().mac);
			Assert.no(OsUtil.os().linux);
		}
	}

	@Test
	public void testArchOverride() {
		var orig = OsUtil.os();
		try (var _ = OsUtil.os(null, "x86", null)) {
			assertOs(OsUtil.os(), orig.name, "x86", orig.version);
			Assert.yes(OsUtil.os().x86);
			Assert.no(OsUtil.os().arm);
			Assert.no(OsUtil.os().bit64);
		}
		try (var _ = OsUtil.os(null, "aarch", null)) {
			assertOs(OsUtil.os(), orig.name, "aarch", orig.version);
			Assert.no(OsUtil.os().x86);
			Assert.yes(OsUtil.os().arm);
			Assert.no(OsUtil.os().bit64);
		}
		try (var _ = OsUtil.os(null, "aarch64", null)) {
			assertOs(OsUtil.os(), orig.name, "aarch64", orig.version);
			Assert.no(OsUtil.os().x86);
			Assert.yes(OsUtil.os().arm);
			Assert.yes(OsUtil.os().bit64);
		}
		try (var _ = OsUtil.os(null, "xxx", null)) {
			assertOs(OsUtil.os(), orig.name, "xxx", orig.version);
			Assert.no(OsUtil.os().x86);
			Assert.no(OsUtil.os().arm);
			Assert.no(OsUtil.os().bit64);
		}
	}

	@Test
	public void testConditionals() {
		try (var _ = OsUtil.os("Mac", "aarch64", null)) {
			Assert.equal(OsUtil.os().mac("y", "n"), "y");
			Assert.equal(OsUtil.os().linux("y", "n"), "n");
			Assert.equal(OsUtil.os().x86("y", "n"), "n");
			Assert.equal(OsUtil.os().arm("y", "n"), "y");
			Assert.equal(OsUtil.os().bit64("y", "n"), "y");
		}
		try (var _ = OsUtil.os("Linux", "x86", null)) {
			Assert.equal(OsUtil.os().mac("y", "n"), "n");
			Assert.equal(OsUtil.os().linux("y", "n"), "y");
			Assert.equal(OsUtil.os().x86("y", "n"), "y");
			Assert.equal(OsUtil.os().arm("y", "n"), "n");
			Assert.equal(OsUtil.os().bit64("y", "n"), "n");
		}
	}

	@Test
	public void testVersionOverride() {
		var orig = OsUtil.os();
		try (var _ = OsUtil.os(null, null, "-999")) {
			assertOs(OsUtil.os(), orig.name, orig.arch, "-999");
			assertOs(OsUtil.value(), orig.name, orig.arch, orig.version);
		}
	}

	private static void assertOs(OsUtil.Os os, String name, String arch, String version) {
		Assert.equal(os.name, name, "name");
		Assert.equal(os.arch, arch, "name");
		Assert.equal(os.version, version, "name");
	}
}
