package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class OsTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Os.class);
	}

	@Test
	public void testDescriptor() {
		Assert.match(Os.info().toString(), ".+;.+;.+");
		Assert.match(Os.info().full(), ".+;.+;.+; mac.*linux.*x86.*arm.*bit64.*");
	}

	@Test
	public void testAws() {
		try (var _ = SystemVars.removable("AWS_PATH", "x")) {
			Assert.yes(Os.aws());
		}
		try (var _ = SystemVars.removable("AWS_PATH", "")) {
			Assert.no(Os.aws());
		}
		try (var _ = SystemVars.removable("AWS_PATH", null)) {
			Assert.no(Os.aws());
		}
	}

	@Test
	public void testNameOverride() {
		var orig = Os.info();
		try (var _ = Os.info("Mac", null, null)) {
			assertOs(Os.info(), "Mac", orig.arch, orig.version);
			Assert.yes(Os.info().mac);
			Assert.no(Os.info().linux);
		}
		try (var _ = Os.info("Darwin", null, null)) {
			assertOs(Os.info(), "Darwin", orig.arch, orig.version);
			Assert.yes(Os.info().mac);
			Assert.no(Os.info().linux);
		}
		try (var _ = Os.info("Linux", null, null)) {
			assertOs(Os.info(), "Linux", orig.arch, orig.version);
			Assert.no(Os.info().mac);
			Assert.yes(Os.info().linux);
		}
		try (var _ = Os.info("Other", null, null)) {
			assertOs(Os.info(), "Other", orig.arch, orig.version);
			Assert.no(Os.info().mac);
			Assert.no(Os.info().linux);
		}
	}

	@Test
	public void testArchOverride() {
		var orig = Os.info();
		try (var _ = Os.info(null, "x86", null)) {
			assertOs(Os.info(), orig.name, "x86", orig.version);
			Assert.yes(Os.info().x86);
			Assert.no(Os.info().arm);
			Assert.no(Os.info().bit64);
		}
		try (var _ = Os.info(null, "aarch", null)) {
			assertOs(Os.info(), orig.name, "aarch", orig.version);
			Assert.no(Os.info().x86);
			Assert.yes(Os.info().arm);
			Assert.no(Os.info().bit64);
		}
		try (var _ = Os.info(null, "aarch64", null)) {
			assertOs(Os.info(), orig.name, "aarch64", orig.version);
			Assert.no(Os.info().x86);
			Assert.yes(Os.info().arm);
			Assert.yes(Os.info().bit64);
		}
		try (var _ = Os.info(null, "xxx", null)) {
			assertOs(Os.info(), orig.name, "xxx", orig.version);
			Assert.no(Os.info().x86);
			Assert.no(Os.info().arm);
			Assert.no(Os.info().bit64);
		}
	}

	@Test
	public void testConditionals() {
		try (var _ = Os.info("Mac", "aarch64", null)) {
			Assert.equal(Os.info().mac("y", "n"), "y");
			Assert.equal(Os.info().linux("y", "n"), "n");
			Assert.equal(Os.info().x86("y", "n"), "n");
			Assert.equal(Os.info().arm("y", "n"), "y");
			Assert.equal(Os.info().bit64("y", "n"), "y");
		}
		try (var _ = Os.info("Linux", "x86", null)) {
			Assert.equal(Os.info().mac("y", "n"), "n");
			Assert.equal(Os.info().linux("y", "n"), "y");
			Assert.equal(Os.info().x86("y", "n"), "y");
			Assert.equal(Os.info().arm("y", "n"), "n");
			Assert.equal(Os.info().bit64("y", "n"), "n");
		}
	}

	@Test
	public void testVersionOverride() {
		var orig = Os.info();
		try (var _ = Os.info(null, null, "-999")) {
			assertOs(Os.info(), orig.name, orig.arch, "-999");
			assertOs(Os.value(), orig.name, orig.arch, orig.version);
		}
	}

	private static void assertOs(Os.Info os, String name, String arch, String version) {
		Assert.equal(os.name, name, "name");
		Assert.equal(os.arch, arch, "name");
		Assert.equal(os.version, version, "name");
	}
}
