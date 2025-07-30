package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.Test;

public class ResourcePathBehavior {

	@Test
	public void shouldRepresentationPathAsString() throws IOException {
		try (ResourcePath rp = ResourcePath.of(getClass(), "res", "test")) {
			assertEquals(rp.toString(), rp.path().toString());
		}
	}

	@Test
	public void shouldCreateBySuffix() throws IOException {
		try (ResourcePath rp = ResourcePath.ofSuffix(IoUtilTest.class, "properties")) {
			assertEquals(rp.readString(), "a=b");
		}
	}

	@Test
	public void shouldCreateForRootDir() throws IOException {
		try (ResourcePath rp = ResourcePath.ofRoot(String.class)) {
			List<String> names = IoUtil.listNames(rp.path());
			assertTrue(names.contains("java"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateForFile() throws IOException {
		assertNull(ResourcePath.of(null));
		try (ResourcePath rp = ResourcePath.of(getClass(), "res", "test")) {
			assertUnordered(IoUtil.listNames(rp.path()), "A.txt", "BB.txt", "CCC.txt");
			assertEquals(Files.readString(rp.resolve("A.txt")), "aaa");
		}
		try (ResourcePath rp = ResourcePath.of(getClass(), "res", "test", "BB.txt")) {
			assertEquals(rp.readString(), "bb");
		}
	}

	@Test
	public void shouldCreateForJar() throws IOException {
		assertThrown(() -> ResourcePath.of(Test.class, "\0"));
		try (ResourcePath rp = ResourcePath.of(Test.class, "runner")) {
			List<String> names = IoUtil.listNames(rp.path());
			assertTrue(names.contains("Runner.class"));
		}
	}

	@Test
	public void shouldCreateForModule() throws IOException {
		try (ResourcePath rp = ResourcePath.of(String.class, "ref")) {
			List<String> names = IoUtil.listNames(rp.path());
			assertTrue(names.contains("Finalizer.class"));
		}
	}

}
