package ceri.jna.test;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.jna.test.CSymbolGen.Arch;

public class CSymbolGenBehavior {

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
	
}
