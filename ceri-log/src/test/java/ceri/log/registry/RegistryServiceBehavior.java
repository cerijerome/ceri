package ceri.log.registry;

import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts.Consumer;
import ceri.common.property.TypedProperties;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;
import ceri.log.test.LogModifier;

public class RegistryServiceBehavior {
	private static final Consumer<IOException, Properties> NULL_CONSUMER = null;
	private static final String REG_FILENAME = "reg.properties";
	private FileTestHelper files = null;
	private RegistryService service = null;

	@After
	public void after() {
		Closeables.close(service, files);
		service = null;
		files = null;
	}

	@Test
	public void shouldCreateConfigFromProperties() {
		var config = new RegistryService.Properties(TestUtil.typedProperties("registry", "service"))
			.config();
		Assert.equal(config,
			new RegistryService.Config("test", Path.of("test/registry.properties"), 100, 200));
	}

	@Test
	public void shouldCreateNoOpInstance() throws IOException {
		service = RegistryService.of(NULL_CONSUMER, NULL_CONSUMER);
	}

	@Test
	public void shouldLoadProperties() throws IOException {
		init("a.b.c=123", "a.b.d=456");
		Assert.equal(service.registry.apply(p -> p.parse("a.b.c").toInt()), 123);
	}

	@Test
	public void shouldReplaceUpdatesBySource() throws IOException {
		init("a.b.c=100");
		service.registry.queue(p -> inc(p, "a.b.c", 10));
		service.registry.queue("x", p -> inc(p, "a.b.c", 33));
		service.registry.queue("x", p -> inc(p, "a.b.c", 7));
		service.close();
		Assert.find(files.readString(REG_FILENAME), "a\\.b\\.c=117");
	}

	private static void inc(TypedProperties p, String key, int diff) {
		int value = p.parse(key).toInt();
		p.set(value + diff, key);
	}

	@Test
	public void shouldSaveProperties() throws IOException {
		init("");
		service.registry.queue(p -> p.set(123, "a.b.c"));
		service.registry.accept(p -> p.set(456, "a.b.d"));
		service.close();
		var content = files.readString(REG_FILENAME);
		Assert.find(content, "a\\.b\\.c=123");
		Assert.find(content, "a\\.b\\.d=456");
	}

	@Test
	public void shouldProvideSubRegistryAccess() throws IOException {
		init("a.a.a=123", "a.b.a=456");
		var a = service.registry.sub("a");
		var aa = a.sub("a");
		var ab = service.registry.sub("a.b");
		Assert.equal(a.apply(p -> p.parse("a", "a").toInt()), 123);
		Assert.equal(aa.apply(p -> p.parse("a").toInt()), 123);
		Assert.equal(ab.apply(p -> p.parse("a").toInt()), 456);
	}

	@Test
	public void shouldPersistPropertiesOnRequest() throws IOException {
		var save = CallSync.<Properties>consumer(null, true);
		service = RegistryService.of(null, p -> save.accept(p, ExceptionAdapter.io));
		service.registry.accept(p -> p.set(123, "a.b.c"));
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
			service = RegistryService.of(null, p -> save.accept(p, ExceptionAdapter.io), 0, 0);
			service.registry.accept(p -> p.set(123, "a.b.c"));
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
		service = RegistryService.of(null, p -> save.accept(p, ExceptionAdapter.io));
		service.registry.accept(p -> p.set(123, "a.b.c"));
		service.close();
		var p = save.awaitAuto();
		Assert.equal(p.getProperty("a.b.c"), "123");
	}

	@Test
	public void shouldAllowNullPath() throws IOException {
		service = service("reg", null);
		service.registry.accept(p -> p.set(123, "a.b.c")); // no exception thrown
		service.persist(true);
	}

	@Test
	public void shouldIgnoreLoadForMissingFile() throws IOException {
		files = FileTestHelper.builder().build();
		service = service("reg", files.path(REG_FILENAME)); // no exception thrown
	}

	@Test
	public void shouldNotSaveIfEmpty() throws IOException {
		init("a.b.c=123");
		service.registry.accept(p -> p.set(456, "a.b.c"));
		service.registry.accept(p -> p.set(null, "a.b.c")); // removes property
		service.close();
		Assert.equal(files.readString(REG_FILENAME), "a.b.c=123");
	}

	private static RegistryService service(String name, Path path) throws IOException {
		return RegistryService.of(RegistryService.Config.of(name, path));
	}

	private void init(String... lines) throws IOException {
		var content = String.join("\n", lines);
		files = FileTestHelper.builder().file(REG_FILENAME, content).build();
		service = service("reg", files.path(REG_FILENAME));
	}

	// private String regFileContent() throws IOException {
	// return files.readString(REG_FILENAME);
	// }
}
