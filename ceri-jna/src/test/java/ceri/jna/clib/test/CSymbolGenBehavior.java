package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNotFound;
import static java.lang.reflect.AccessFlag.FINAL;
import static java.lang.reflect.AccessFlag.STATIC;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.jna.clib.test.CSymbolGen.Arch;

public class CSymbolGenBehavior {

	public static class C {
		public static final int IGNORE = -1;
		public static final int VAL1 = 1;
		public static final int VAL2 = 2;
		public static final String VAL3 = "3";
		public static int VAL4 = 4;
		public static final int PRIV1 = 1;
		public static final int PRIV2 = 2;

		public static class Sub {
			public static final int IGNORE = -1;
			public static final int SUB1 = 1;
		}

		public static class Empty {}
	}

	@Test
	public void shouldGenerateTestCode() throws IOException {
		var code = CSymbolGen.generateForTest();
		assertFind(code, "\\Q#define EMPTY\n\\E");
		assertFind(code, "\\Q#define STRING \"string\"\n\\E");
		assertFind(code, "\\QSYM(UNDEFINED)\\E");
		assertFind(code, "\\QSYM(EMPTY)\\E");
		assertFind(code, "\\QSYM(STRING)\\E");
	}

	@Test
	public void shouldGenerateArchSpecificHeaderCode() throws IOException {
		var gen = CSymbolGen.of();
		gen.pre.lines(Arch.linux).include("linux");
		gen.pre.lines(Arch.apple).add("#define apple 1");
		var code = gen.generate();
		assertFind(code, "\\Q#ifdef __linux__\n#include <linux.h>\n#endif\\E");
		assertFind(code, "\\Q#ifdef __APPLE__\n#define apple 1\n#endif\\E");
	}

	@Test
	public void shouldGenerateStructFieldSize() throws IOException {
		var gen = CSymbolGen.of();
		gen.main().lines.fsize("struct timespec", "tv_sec", "tv_nsec");
		var code = gen.generate();
		assertFind(code, "\\QFSIZE(struct timespec,tv_sec)\\E");
		assertFind(code, "\\QFSIZE(struct timespec,tv_nsec)\\E");
	}

	@Test
	public void shouldAddClassFields() throws IOException {
		var gen = CSymbolGen.of();
		gen.main().lines.add(CSymbolGen.filterDef().exclude("IGNORE").excludeRegex("PRIV\\d+"),
			C.class);
		var code = gen.generate();
		assertFind(code, "\\QSYMI(VAL1);\\E");
		assertFind(code, "\\QSYMI(VAL2);\\E");
		assertFind(code, "\\QSYMI(SUB1);\\E");
		assertNotFound(code, "\\QIGNORE\\E");
		assertNotFound(code, "\\QVAL3\\E");
		assertNotFound(code, "\\QVAL4\\E");
		assertNotFound(code, "\\QPRIV\\E");
	}

	@Test
	public void shouldGenerateSymbolType() throws IOException {
		var gen = CSymbolGen.of();
		gen.main().lines.add(CSymbolGen.filter().flags(STATIC, FINAL).exclude("IGNORE"),
			CSymbolGen.filter().excludeRegex("PRIV\\d+"), C.class);
		var code = gen.generate();
		assertFind(code, "\\QSYMI(VAL1);\\E");
		assertFind(code, "\\QSYM(PRIV1);\\E");
	}

	@Test
	public void shouldGenerateFile() throws IOException {
		try (var files = FileTestHelper.builder().build()) {
			var gen = CSymbolGen.of();
			gen.main().lines.symi("test1");
			gen.generateFile(files.path("test.c"));
			var code = Files.readString(files.path("test.c"));
			assertFind(code, "\\QSYMI(test1);\\E");
		}
	}

}
