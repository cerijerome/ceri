package ceri.log.registry;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.ExceptionConsumer;
import ceri.common.property.TypedProperties;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.common.util.CloseableUtil;
import ceri.log.test.LogModifier;

public class RegistryServiceBehavior {
	private static final ExceptionConsumer<IOException, Properties> NULL_CONSUMER = null;
	private static final String REG_FILENAME = "reg.properties";
	private FileTestHelper files = null;
	private RegistryService service = null;

	@After
	public void after() {
		CloseableUtil.close(service, files);
		service = null;
		files = null;
	}

	@Test
	public void should() throws IOException {
		service = RegistryService.of(NULL_CONSUMER, NULL_CONSUMER);
	}

	@Test
	public void shouldLoadProperties() throws IOException {
		init("a.b.c=123", "a.b.d=456");
		assertEquals(service.registry.apply(p -> p.intValue("a.b.c")), 123);
	}

	@Test
	public void shouldReplaceUpdatesBySource() throws IOException {
		init("a.b.c=100");
		service.registry.queue(p -> inc(p, "a.b.c", 10));
		service.registry.queue("x", p -> inc(p, "a.b.c", 33));
		service.registry.queue("x", p -> inc(p, "a.b.c", 7));
		service.close();
		var content = regFileContent();
		assertFind(content, "a\\.b\\.c=117");
	}

	private static void inc(TypedProperties p, String key, int diff) {
		int value = p.intValue(key);
		p.setValue(value + diff, key);
	}

	@Test
	public void shouldSaveProperties() throws IOException {
		init("");
		service.registry.queue(p -> p.setValue(123, "a.b.c"));
		service.registry.accept(p -> p.setValue(456, "a.b.d"));
		service.close();
		var content = regFileContent();
		assertFind(content, "a\\.b\\.c=123");
		assertFind(content, "a\\.b\\.d=456");
	}

	@Test
	public void shouldProvideSubRegistryAccess() throws IOException {
		init("a.a.a=123", "a.b.a=456");
		var a = service.registry.sub("a");
		var aa = a.sub("a");
		var ab = service.registry.sub("a.b");
		assertEquals(a.apply(p -> p.intValue("a", "a")), 123);
		assertEquals(aa.apply(p -> p.intValue("a")), 123);
		assertEquals(ab.apply(p -> p.intValue("a")), 456);
	}

	@Test
	public void shouldPersistPropertiesOnRequest() throws IOException {
		var save = CallSync.<Properties>consumer(null, true);
		service = RegistryService.of(null, p -> save.accept(p, IO_ADAPTER));
		service.registry.accept(p -> p.setValue(123, "a.b.c"));
		service.persist(false);
		save.assertNoCall();
		service.persist(true);
		save.awaitAuto();
	}

	@Test
	public void shouldRetrySaveOnError() throws IOException {
		LogModifier.run(() -> {
			var save = CallSync.<Properties>consumer(null, false);
			save.error.setFrom(IOX, IOX, null);
			service = RegistryService.of(null, p -> save.accept(p, IO_ADAPTER), 0, 0);
			service.registry.accept(p -> p.setValue(123, "a.b.c"));
			service.persist(true);
			save.await(); // error
			save.await(); // error
			save.await(); // ok
			service.close();
		}, Level.OFF, RegistryService.class);
	}

	@Test
	public void shouldSavePropertiesOnClose() throws IOException {
		var save = CallSync.<Properties>consumer(null, true);
		service = RegistryService.of(null, p -> save.accept(p, IO_ADAPTER));
		service.registry.accept(p -> p.setValue(123, "a.b.c"));
		service.close();
		var p = save.awaitAuto();
		assertEquals(p.getProperty("a.b.c"), "123");
	}

	@Test
	public void shouldAllowNullPath() throws IOException {
		service = RegistryService.of("reg", null);
		service.registry.accept(p -> p.setValue(123, "a.b.c")); // no exception thrown
		service.persist(true);
	}

	@Test
	public void shouldIgnoreLoadForMissingFile() throws IOException {
		files = FileTestHelper.builder().build();
		service = RegistryService.of("reg", files.path(REG_FILENAME)); // no exception thrown
	}

	@Test
	public void shouldNotSaveIfEmpty() throws IOException {
		init("a.b.c=123");
		service.registry.accept(p -> p.setValue(456, "a.b.c"));
		service.registry.accept(p -> p.setValue(null, "a.b.c")); // removes property
		service.close();
		var content = regFileContent();
		assertEquals(content, "a.b.c=123");
	}

	private void init(String... lines) throws IOException {
		var content = String.join("\n", lines);
		files = FileTestHelper.builder().file(REG_FILENAME, content).build();
		service = RegistryService.of("reg", files.path(REG_FILENAME));
	}

	private String regFileContent() throws IOException {
		return Files.readString(files.path(REG_FILENAME));
	}
}
