package ceri.ffm.reflect;

import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.io.IoStream;
import ceri.common.io.SystemIo;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.common.text.Strings;
import ceri.ffm.reflect.CAnnotations.CGen;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CUndefined;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.reflect.Refine.Unsigned;
import ceri.ffm.type.Group;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Struct;
import ceri.ffm.util.FfmOs;

public class CSymbolGenBehavior {
	private PrintStream nullOut = IoStream.nullPrint();
	private FileTestHelper files;
	private SystemIo sys;

	@CGen(target = CTestGen.class, location = "src/test/c/__test__/")
	public static class TestSymbols {}

	@CGen(os = FfmOs.linux, target = {}, location = "src/test/c/__test__/named.c")
	public static class TestNamed {}

	@CType(valueField = "name")
	public enum BadE {
		e("E");

		public final String name;

		private BadE(String name) {
			this.name = name;
		}
	}

	public enum BadE2 {
		e(1);

		public final int code;

		private BadE2(int code) {
			this.code = code;
		}
	}

	@Size(Integer.BYTES)
	@Unsigned
	public static class BadI extends IntType<BadI> {
		public BadI(Integer i) {
			super(i);
		}
	}

	@Fields("i")
	public static class BadS extends Struct<BadS> {
		public int i;

		public BadS(int i) {
			this.i = i;
		}
	}

	@CUndefined
	public static class Undefined {}

	@After
	public void after() {
		Closeables.close(sys, files);
		files = null;
		sys = null;
	}

	@Test
	public void shouldCustomizeAutoGen() throws IOException {
		initFile();
		CSymbolGen.Auto.gen(TestNamed.class, (os, gen) -> {
			if (os == FfmOs.linux) gen.includes.add("custom.h");
		});
		var c = files.readString("named-linux.c");
		assertLines(c, "#include <custom.h>");
	}

	@Test
	public void shouldAutoGenSymbols() throws IOException {
		initFile();
		CSymbolGen.Auto.gen(TestSymbols.class);
		var c = files.readString("test-symbols-mac.c");
		assertLines(c, "#include <symbols.h>", "CERI_VSYMI(Fb,145);", "CERI_VSYMI(Fs,65424);",
			"CERI_VSYMI(Fi,4294967183);", "CERI_VSYMI(Fbs,-111);", "CERI_VSYMI(Fss,-112);",
			"CERI_VSYMI(Fis,-113);", "CERI_VSYMI(FL,114); /* Fl */", "CERI_VSYMI(Flm,117);",
			"// Ignore: Fll = 117", "CERI_VSYMI(a,0);", "CERI_VSYMI(B,2); /* b */",
			"\t// Ignore: c = 4", "CERI_VSIZE(IB,1); /* Ib */", "CERI_VSIZE(Ii,4);",
			"CERI_VSIZE(Il,8);", "CERI_VSYMI(NFi,200);");
		assertSizes(c, "CERI_VSIZE(struct S,%s);", "CERI_VSIZE(ST,%s); /* St */",
			"CERI_VSIZE(union U,%s);");
		c = files.readString("test-symbols-linux.c");
		assertLines(c, "#include <symbols.h>", "CERI_VSYMI(Fb,145);", "CERI_VSYMI(Fs,65424);",
			"CERI_VSYMI(Fi,4294967183);", "CERI_VSYMI(Fbs,-111);", "CERI_VSYMI(Fss,-112);",
			"CERI_VSYMI(Fis,-113);", "CERI_VSYMI(FL,114); /* Fl */", "// Ignore: Flm = 117",
			"CERI_VSYMI(Fll,117);", "CERI_VSYMI(a,1);", "CERI_VSYMI(B,2); /* b */",
			"// Ignore: c = 4", "CERI_VSIZE(IB,1); /* Ib */", "CERI_VSIZE(Ii,4);",
			"CERI_VSIZE(Il,8);", "CERI_VSYMI(NFi,200);");
		assertSizes(c, "CERI_VSIZE(struct S,%s);", "CERI_VSIZE(ST,%s); /* St */",
			"CERI_VSIZE(union U,%s);");
	}

	@Test
	public void shouldFilterTypes() {
		var gen = CSymbolGen.of().out(nullOut);
		gen.overrides.includes.add((_, _) -> true, (_, _) -> null);
		gen.overrides.classes.add((c, _) -> IntType.class.isAssignableFrom(c));
		gen.overrides.classes.add((c, _) -> Group.class.isAssignableFrom(c));
		gen.overrides.fields.add((_, _) -> true);
		gen.overrides.enums.add((e, _) -> !e.name().equals("c"));
		gen.overrides.enums.add((e, _) -> e.name().equals("c"), CType.Value.of("C"));
		gen.add(CTestGen.class);
		var c = gen.generate();
		assertLines(c, "#include <symbols.h>", "CERI_VSYMI(C,4); /* c */");
	}

	@Test
	public void shouldGenerateMacros() {
		var gen = CSymbolGen.of().out(null);
		gen.macros.sym("sym", null);
		gen.macros.symi("sym", 111);
		gen.macros.size("struct s");
		gen.macros.fsize("struct s", "field");
		var c = gen.generate();
		assertLines(c, "CERI_SYM(sym);", "CERI_SYMI(sym); /* 111|0x6f */", "CERI_SIZE(struct s);",
			"CERI_FSIZE(struct s,field);");
	}

	@Test
	public void shouldAddLines() {
		var gen = CSymbolGen.of().out(nullOut);
		gen.includes.add("inc.h");
		gen.lines.append("test");
		gen.lines.addIfDef("def1", () -> gen.lines.add("if1"));
		gen.lines.addIfDef(null, () -> gen.lines.add("if2"), () -> gen.lines.add("else2"));
		gen.lines.addIf("test3", () -> gen.lines.add("if3"));
		gen.lines.addIf("test4", () -> gen.lines.add("if4"), () -> gen.lines.add("else4"));
		var c = gen.generate();
		assertLines(c, "#include <inc.h>");
		Assert.contains(c, "test", "#if defined(def1)", "if1", "#endif", "if2", "else2",
			"#if test3", "if3", "#endif", "#if test4", "if4", "#else", "else4", "#endif");
	}

	@Test
	public void shouldFailForBadTypes() {
		var gen = CSymbolGen.of().out(nullOut);
		Assert.illegalArg(() -> gen.add(BadE.class)); // invalid value field
		Assert.illegalArg(() -> gen.add(BadE2.class)); // non-standard value field name
		Assert.illegalArg(() -> gen.add(BadS.class)); // unsupported constructor
		Assert.illegalArg(() -> gen.add(BadI.class)); // unsupported constructor
		gen.add(Undefined.class); // ignored
	}

	private void initFile() throws IOException {
		files = FileTestHelper.builder("src/test/c").root("__test__").build();
		sys = SystemIo.of();
		sys.out(nullOut);
	}

	private static void assertSizes(String gen, String... formats) {
		for (var format : formats) { // don't check size values
			var line = Strings.format("\\Q" + format + "\\E", "\\E\\d+\\Q");
			Assert.find(gen, line);
		}
	}

	private static void assertLines(String gen, String... lines) {
		for (var line : lines)
			Assert.contains(gen, line);
	}
}
