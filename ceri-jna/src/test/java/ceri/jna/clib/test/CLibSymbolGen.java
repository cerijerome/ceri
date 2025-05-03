package ceri.jna.clib.test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import ceri.common.reflect.ClassReInitializer;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.jna.CStdlib;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaOs;

/**
 * Generates c code to print CLib constants on target system.
 */
public class CLibSymbolGen {
	private static final Pattern BAUD_REGEX = Pattern.compile("^B(\\d{6,})$");
	private static final String FILE = "src/test/c/clib-symbols.c";

	public static void main(String[] args) {
		var reinit = ClassReInitializer.builder().init(Gen.class).support(Target.class)
			.support(Target.CLASSES).support(Target.SUPPORT).build();
		JnaOs.forEach(_ -> reinit.reinit());
	}

	public static class Gen {
		static {
			CSymbolGen.generate(gen(Target.CLASSES), JnaOs.current().file(FILE), System.out);
		}
	}

	public static class Target {
		public static final List<Class<?>> CLASSES = List.of(CErrNo.class, CFcntl.class,
			CIoctl.class, CPoll.class, CSignal.class, CStdlib.class, CTermios.class, CUnistd.class);
		public static final List<Class<?>> SUPPORT = List.of(CIoctl.Linux.class);
	}

	public static CSymbolGen gen(List<Class<?>> classes) { // must be public
		var gen = CSymbolGen.of();
		gen.include("fcntl", "unistd", "poll", "termios", "signal", "errno", "sys/ioctl");
		gen.include(JnaOs.linux, "linux/serial");
		gen.include(JnaOs.mac, "IOKit/serial/ioss");
		gen.main.lines.sizes("int", "long", "size_t", "mode_t", "speed_t", "nfds_t", "sigset_t",
			"time_t");
		gen.main.lines.fsize("struct timeval", "tv_sec", "tv_usec");
		gen.main.lines.fsize("struct timespec", "tv_sec", "tv_nsec");
		gen.fieldFilter(fieldFilter());
		gen.intFilter(CSymbolGen.intFilter().exclude("SIG_DFL", "SIG_IGN", "SIG_ERR"));
		gen.main.lines.add(classes);
		return gen;
	}

	private static Predicate<Field> fieldFilter() {
		var os = JnaOs.current();
		var filter = CSymbolGen.fieldFilter().exclude("INVALID_FD", "SIGSET_T_SIZE")
			.add(f -> !invalidErr(f));
		if (os == JnaOs.mac) filter.exclude("SIGPOLL", "CMSPAR", "_IOC_SIZEBITS", "_IOC_SIZEMASK")
			.exclude(CIoctl.Linux.class).add(f -> maxBaud(f, 230400));
		if (os == JnaOs.linux) filter.exclude("IOC_VOID").exclude(CIoctl.Mac.class);
		return filter;
	}

	private static boolean maxBaud(Field f, int max) {
		return RegexUtil.parseFind(BAUD_REGEX, f.getName()).toInt(0) <= max;
	}

	private static boolean invalidErr(Field f) {
		return ReflectUtil.same(f.getDeclaringClass(), CErrNo.class)
			&& ReflectUtil.publicFieldValue(null, f).equals(-1);
	}
}
