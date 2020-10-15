package ceri.common.io;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.Test;

public class ResourcePathBehavior {

	@Test
	public void shouldRepresentationPathAsString() throws IOException {
		try (ResourcePath rp = ResourcePath.of(getClass(), "res", "test")) {
			assertThat(rp.toString(), is(rp.path().toString()));
		}
	}

	@Test
	public void shouldCreateBySuffix() throws IOException {
		try (ResourcePath rp = ResourcePath.ofSuffix(IoUtilTest.class, "properties")) {
			assertThat(rp.readString(), is("a=b"));
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
			assertCollection(IoUtil.listNames(rp.path()), "A.txt", "BB.txt", "CCC.txt");
			assertThat(Files.readString(rp.resolve("A.txt")), is("aaa"));
		}
		try (ResourcePath rp = ResourcePath.of(getClass(), "res", "test", "BB.txt")) {
			assertThat(rp.readString(), is("bb"));
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
