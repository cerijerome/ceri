package ceri.serial.clib.util;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.TestUtil.baseProperties;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.Mode;
import ceri.serial.clib.jna.CLib;
import ceri.serial.clib.test.TestCLibNative;
import ceri.serial.clib.test.TestFileDescriptor;

public class SelfHealingFdConfigBehavior {

	@Test
	public void shouldCreateFromProperties() throws IOException {
		try (var enc = TestCLibNative.register()) {
			var lib = enc.subject;
			var config =
				new SelfHealingFdProperties(baseProperties("self-healing-fd"), "fd").config();
			try (var fd = config.open()) {
				lib.open.assertAuto(List.of("test", CLib.O_RDWR + CLib.O_APPEND, 0666));
				assertEquals(config.fixRetryDelayMs, 123);
				assertEquals(config.recoveryDelayMs, 456);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateFromOpenFunction() throws IOException {
		TestFileDescriptor fd = TestFileDescriptor.of(33);
		CallSync.Get<FileDescriptor> sync = CallSync.supplier(fd);
		var config = SelfHealingFdConfig.of(() -> sync.get(IO_ADAPTER));
		assertEquals(config.open(), fd);
		sync.awaitAuto();
	}

	@Test
	public void shouldSpecifyBrokenPredicate() {
		var config = SelfHealingFdConfig.builder("test", Mode.NONE)
			.brokenPredicate(Objects::nonNull).build();
		assertEquals(config.brokenPredicate.test(null), false);
		assertEquals(config.brokenPredicate.test(new IOException()), true);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var config = SelfHealingFdConfig.of(() -> TestFileDescriptor.of(33));
		assertFind(config, "\\(@\\w+,1000,2000,%s\\)", config.brokenPredicate);
	}

}
