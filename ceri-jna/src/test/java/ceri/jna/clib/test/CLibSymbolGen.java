package ceri.jna.clib.test;

import java.io.IOException;
import java.nio.file.Paths;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.jna.CStdlib;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.test.CSymbolGen;
import ceri.jna.test.CSymbolGen.Arch;

/**
 * Generates c code to print CLib constants on target system.
 */
public class CLibSymbolGen {
	private static final String FILE = "src/test/c/clib-symbols.c";

	public static void main(String[] args) throws IOException {
		var path = clibGen().generateFile(Paths.get(FILE));
		System.out.println("Generated c file: " + path.toAbsolutePath());
	}

	private static CSymbolGen clibGen() throws IOException {
		var gen = CSymbolGen.of();
		gen.pre.lines.include("fcntl", "unistd", "poll", "termios", "signal", "errno", "sys/ioctl");
		gen.pre.lines(Arch.linux).include("linux/serial");
		gen.pre.lines(Arch.apple).include("IOKit/serial/ioss");
		gen.main().lines.size("int", "long", "size_t", "mode_t", "speed_t", "nfds_t", "sigset_t",
			"time_t");
		gen.main().lines.fsize("struct timespec", "tv_sec", "tv_nsec");
		var filter = CSymbolGen.filterDef().exclude("UNDEFINED", "INVALID_FD", "SIGSET_T_SIZE");
		var symInt = CSymbolGen.filter().exclude("SIG_DFL", "SIG_IGN", "SIG_ERR");
		gen.main().lines.add(filter, symInt, CErrNo.class, CFcntl.class, CIoctl.class, CPoll.class,
			CSignal.class, CStdlib.class, CTermios.class, CUnistd.class); // no CTime
		gen.main().lines.add("printf(\"\\n\");");
		return gen;
	}
}
