package ceri.ffm.reflect;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.reflect.CAnnotations.CGen;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CType.Attr;
import ceri.ffm.reflect.CAnnotations.CUndefined;
import ceri.ffm.util.FfmOs;

public class CAnnotationsTest {

	@CGen(os = FfmOs.linux, target = Integer.class, reload = Long.class, location = "test")
	public static class Gen {}

	@CInclude("inc.h")
	@CInclude({ "aaa.h", "bbb.h" })
	@CInclude(os = FfmOs.linux, value = "linux.h")
	@CInclude(os = FfmOs.mac, value = "mac.h")
	public static class Inc {}

	public static enum Type {
		@CUndefined
		A,
		@CUndefined
		@CType
		B,
		@CType(os = FfmOs.linux, name = "C+")
		C,
		@CType(attrs = { Attr.cenum })
		D,
		@CType(os = FfmOs.mac, name = "EM")
		@CType(os = FfmOs.linux, name = "EL")
		E,
	}

	@Test
	public void testCGenFromAnnotation() {
		assertCGen(CAnnotations.CGen.Value.from(null), CGen.Value.NONE);
		assertCGen(CAnnotations.cgen(null), CGen.Value.NONE);
		assertCGen(CAnnotations.cgen(String.class), CGen.Value.NONE);
		assertCGen(CAnnotations.cgen(Gen.class), CGen.Value.builder(Integer.class).os(FfmOs.linux)
			.reload(Long.class).location("test").value());
	}

	@Test
	public void shouldBuildCGen() {
		var cgen = CGen.Value.builder(Integer.class).value();
		Assert.array(cgen.os(), FfmOs.KNOWN.toArray());
	}

	@Test
	public void shouldProvideCGenClasses() {
		Assert.unordered(CAnnotations.cgen(Gen.class).classes(), Integer.class, Long.class);
	}

	@Test
	public void shouldProvideCGenLocation() {
		Assert.equal(CGen.Value.NONE.location("def"), "def");
		Assert.equal(CAnnotations.cgen(Gen.class).location("def"), "test");
	}

	@Test
	public void testCIncludesFromAnnotation() {
		Assert.map(CAnnotations.cincludes(Object.class).map());
		var inc = CAnnotations.cincludes(Inc.class);
		Assert.unordered(inc.includes(FfmOs.linux), "inc.h", "aaa.h", "bbb.h", "linux.h");
		Assert.unordered(inc.includes(FfmOs.mac), "inc.h", "aaa.h", "bbb.h", "mac.h");
	}

	@Test
	public void shouldProvideEmptyIncludes() {
		Assert.equal(CAnnotations.cincludes(String.class), CInclude.Value.NONE);
		Assert.equal(CInclude.Value.of(), CInclude.Value.NONE);
	}

	@Test
	public void testCTypeFromAnnotation() {
		assertCType(CAnnotations.ctype((Class<?>) null, FfmOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype((Class<?>) null, FfmOs.linux), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.class, FfmOs.mac), CType.Value.DEFAULT);
		assertCType(CAnnotations.ctype(Type.class, FfmOs.linux), CType.Value.DEFAULT);
		assertCType(CAnnotations.ctype(Type.A, FfmOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.A, FfmOs.linux), CType.Value.UNDEFINED);
		Assert.illegalArg(() -> CAnnotations.ctype(Type.B, FfmOs.mac));
		Assert.illegalArg(() -> CAnnotations.ctype(Type.B, FfmOs.linux));
		assertCType(CAnnotations.ctype(Type.C, FfmOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.C, FfmOs.linux), CType.Value.of(FfmOs.linux, "C+"));
		assertCType(CAnnotations.ctype(Type.D, FfmOs.mac), CType.Value.of(Attr.cenum));
		assertCType(CAnnotations.ctype(Type.D, FfmOs.linux), CType.Value.of(Attr.cenum));
		assertCType(CAnnotations.ctype(Type.E, FfmOs.mac), CType.Value.of(FfmOs.mac, "EM"));
		assertCType(CAnnotations.ctype(Type.E, FfmOs.linux), CType.Value.of(FfmOs.linux, "EL"));
	}

	@Test
	public void shouldCreateCType() {
		assertCType(CType.Value.of(), CType.Value.DEFAULT);
		assertCType(CType.Value.of("name"), "name", "value");
		assertCType(CType.Value.of("", "val"), "", "val");
		assertCType(CType.Value.of(FfmOs.linux, Attr.cenum), FfmOs.linux, "", "value", Attr.cenum);
	}

	@Test
	public void shouldProvideAccessToCTypeSettings() {
		Assert.equal(CType.Value.UNDEFINED.undefined(), true);
		Assert.equal(CType.Value.DEFAULT.undefined(), false);
		Assert.equal(CType.Value.DEFAULT.name("def"), "def");
		Assert.equal(CType.Value.of("test").name("def"), "test");
		Assert.equal(CType.Value.DEFAULT.typedef(), false);
		Assert.equal(CType.Value.of(Attr.typedef).typedef(), true);
		Assert.equal(CType.Value.DEFAULT.cenum(), false);
		Assert.equal(CType.Value.of(Attr.cenum).cenum(), true);
	}

	private static void assertCGen(CGen.Value expected, CGen.Value actual) {
		Assert.array(expected.os(), actual.os());
		Assert.array(expected.target(), actual.target());
		Assert.array(expected.reload(), actual.reload());
		Assert.equal(expected.location(), actual.location());
	}

	private static void assertCType(CType.Value expected, CType.Value actual) {
		assertCType(expected, actual.os(), actual.name(), actual.valueField(), actual.attrs());
	}

	private static void assertCType(CType.Value expected, String name, String valueField,
		Attr... attrs) {
		assertCType(expected, FfmOs.NONE, name, valueField, attrs);
	}

	private static void assertCType(CType.Value expected, FfmOs os, String name, String valueField,
		Attr... attrs) {
		assertCType(expected, new FfmOs[] { os }, name, valueField, attrs);
	}

	private static void assertCType(CType.Value expected, FfmOs[] os, String name,
		String valueField, Attr... attrs) {
		Assert.array(expected.os(), os);
		Assert.equal(expected.name(), name);
		Assert.equal(expected.valueField(), valueField);
		Assert.array(expected.attrs(), attrs);
	}
}
