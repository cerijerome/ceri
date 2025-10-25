package ceri.jna.reflect;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertMap;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.common.test.Assert.illegalArg;
import org.junit.Test;
import ceri.jna.reflect.CAnnotations.CGen;
import ceri.jna.reflect.CAnnotations.CInclude;
import ceri.jna.reflect.CAnnotations.CType;
import ceri.jna.reflect.CAnnotations.CType.Attr;
import ceri.jna.reflect.CAnnotations.CUndefined;
import ceri.jna.util.JnaOs;

public class CAnnotationsTest {

	@CGen(os = JnaOs.linux, target = Integer.class, reload = Long.class, location = "test")
	public static class Gen {}

	@CInclude("inc.h")
	@CInclude({ "aaa.h", "bbb.h" })
	@CInclude(os = JnaOs.linux, value = "linux.h")
	@CInclude(os = JnaOs.mac, value = "mac.h")
	public static class Inc {}

	public static enum Type {
		@CUndefined
		A,
		@CUndefined
		@CType
		B,
		@CType(os = JnaOs.linux, name = "C+")
		C,
		@CType(attrs = { Attr.cenum })
		D,
		@CType(os = JnaOs.mac, name = "EM")
		@CType(os = JnaOs.linux, name = "EL")
		E,
	}

	@Test
	public void testCGenFromAnnotation() {
		assertCGen(CAnnotations.cgen(String.class), CGen.Value.NONE);
		assertCGen(CAnnotations.cgen(Gen.class), CGen.Value.builder(Integer.class).os(JnaOs.linux)
			.reload(Long.class).location("test").value());
	}

	@Test
	public void shouldBuildCGen() {
		var cgen = CGen.Value.builder(Integer.class).value();
		assertArray(cgen.os(), JnaOs.KNOWN.toArray());
	}

	@Test
	public void shouldProvideCGenClasses() {
		assertUnordered(CAnnotations.cgen(Gen.class).classes(), Integer.class, Long.class);
	}

	@Test
	public void shouldProvideCGenLocation() {
		assertEquals(CGen.Value.NONE.location("def"), "def");
		assertEquals(CAnnotations.cgen(Gen.class).location("def"), "test");
	}

	@Test
	public void testCIncludesFromAnnotation() {
		assertMap(CAnnotations.cincludes(Object.class).map());
		var inc = CAnnotations.cincludes(Inc.class);
		assertUnordered(inc.includes(JnaOs.linux), "inc.h", "aaa.h", "bbb.h", "linux.h");
		assertUnordered(inc.includes(JnaOs.mac), "inc.h", "aaa.h", "bbb.h", "mac.h");
	}

	@Test
	public void shouldProvideEmptyIncludes() {
		assertEquals(CAnnotations.cincludes(String.class), CInclude.Value.NONE);
		assertEquals(CInclude.Value.of(), CInclude.Value.NONE);
	}

	@Test
	public void testCTypeFromAnnotation() {
		assertCType(CAnnotations.ctype((Class<?>) null, JnaOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype((Class<?>) null, JnaOs.linux), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.class, JnaOs.mac), CType.Value.DEFAULT);
		assertCType(CAnnotations.ctype(Type.class, JnaOs.linux), CType.Value.DEFAULT);
		assertCType(CAnnotations.ctype(Type.A, JnaOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.A, JnaOs.linux), CType.Value.UNDEFINED);
		illegalArg(() -> CAnnotations.ctype(Type.B, JnaOs.mac));
		illegalArg(() -> CAnnotations.ctype(Type.B, JnaOs.linux));
		assertCType(CAnnotations.ctype(Type.C, JnaOs.mac), CType.Value.UNDEFINED);
		assertCType(CAnnotations.ctype(Type.C, JnaOs.linux), CType.Value.of(JnaOs.linux, "C+"));
		assertCType(CAnnotations.ctype(Type.D, JnaOs.mac), CType.Value.of(Attr.cenum));
		assertCType(CAnnotations.ctype(Type.D, JnaOs.linux), CType.Value.of(Attr.cenum));
		assertCType(CAnnotations.ctype(Type.E, JnaOs.mac), CType.Value.of(JnaOs.mac, "EM"));
		assertCType(CAnnotations.ctype(Type.E, JnaOs.linux), CType.Value.of(JnaOs.linux, "EL"));
	}

	@Test
	public void shouldCreateCType() {
		assertCType(CType.Value.of(), CType.Value.DEFAULT);
		assertCType(CType.Value.of("name"), "name", "value");
		assertCType(CType.Value.of("", "val"), "", "val");
		assertCType(CType.Value.of(JnaOs.linux, Attr.cenum), JnaOs.linux, "", "value", Attr.cenum);
	}

	@Test
	public void shouldProvideAccessToCTypeSettings() {
		assertEquals(CType.Value.UNDEFINED.undefined(), true);
		assertEquals(CType.Value.DEFAULT.undefined(), false);
		assertEquals(CType.Value.DEFAULT.name("def"), "def");
		assertEquals(CType.Value.of("test").name("def"), "test");
		assertEquals(CType.Value.DEFAULT.typedef(), false);
		assertEquals(CType.Value.of(Attr.typedef).typedef(), true);
		assertEquals(CType.Value.DEFAULT.cenum(), false);
		assertEquals(CType.Value.of(Attr.cenum).cenum(), true);
	}

	private static void assertCGen(CGen.Value expected, CGen.Value actual) {
		assertArray(expected.os(), actual.os());
		assertArray(expected.target(), actual.target());
		assertArray(expected.reload(), actual.reload());
		assertEquals(expected.location(), actual.location());
	}

	private static void assertCType(CType.Value expected, CType.Value actual) {
		assertCType(expected, actual.os(), actual.name(), actual.valueField(), actual.attrs());
	}

	private static void assertCType(CType.Value expected, String name, String valueField,
		Attr... attrs) {
		assertCType(expected, JnaOs.NONE, name, valueField, attrs);
	}

	private static void assertCType(CType.Value expected, JnaOs os, String name, String valueField,
		Attr... attrs) {
		assertCType(expected, new JnaOs[] { os }, name, valueField, attrs);
	}

	private static void assertCType(CType.Value expected, JnaOs[] os, String name,
		String valueField, Attr... attrs) {
		assertArray(expected.os(), os);
		assertEquals(expected.name(), name);
		assertEquals(expected.valueField(), valueField);
		assertArray(expected.attrs(), attrs);
	}
}
