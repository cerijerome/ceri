package ceri.jna.clib.util;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.jna.clib.jna.CFcntl.O_APPEND;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestFileDescriptor;

public class SelfHealingFdConfigBehavior {

	@Test
	public void shouldCreateFromProperties() throws IOException {
		try (var enc = TestCLibNative.register()) {
			var lib = enc.ref;
			var config =
				new SelfHealingFdProperties(baseProperties("self-healing-fd"), "fd").config();
			try (var fd = config.open()) {
				lib.open.assertAuto(List.of("test", O_RDWR + O_APPEND, 0666));
				assertEquals(config.selfHealing.fixRetryDelayMs, 123);
				assertEquals(config.selfHealing.recoveryDelayMs, 456);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateFromOpenFunction() throws IOException {
		TestFileDescriptor fd = TestFileDescriptor.of(33);
		CallSync.Supplier<FileDescriptor> sync = CallSync.supplier(fd);
		var config = SelfHealingFdConfig.of(() -> sync.get(IO_ADAPTER));
		assertEquals(config.open(), fd);
		sync.awaitAuto();
	}

	@Test
	public void shouldSpecifyBrokenPredicate() {
		var config = SelfHealingFdConfig.builder("test", Mode.NONE)
			.selfHealing(b -> b.brokenPredicate(Objects::nonNull)).build();
		assertEquals(config.selfHealing.brokenPredicate.test(null), false);
		assertEquals(config.selfHealing.brokenPredicate.test(new IOException()), true);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var config = SelfHealingFdConfig.of(() -> TestFileDescriptor.of(33));
		assertFind(config, "\\(2000,1000,%s\\)", config.selfHealing.brokenPredicate);
	}

}
