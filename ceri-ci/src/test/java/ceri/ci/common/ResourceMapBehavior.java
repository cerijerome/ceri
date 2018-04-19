package ceri.ci.common;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

public class ResourceMapBehavior {

	@Test
	public void shouldVerifyKeys() throws IOException {
		ResourceMap map = new ResourceMap(getClass(), "res/test", "txt");
		Collection<String> verifiedKeys = map.verifyAll(Arrays.asList("A", "B"));
		assertCollection(verifiedKeys, "A");
	}

	@Test
	public void shouldIgnoreSubDirectories() throws Exception {
		ResourceMap map = new ResourceMap(getClass(), "res", "txt");
		assertCollection(map.keys());
	}

	@Test
	public void shouldIgnoreNonMatchingExtensions() throws Exception {
		ResourceMap map = new ResourceMap(getClass(), "res/test", "jpg");
		assertCollection(map.keys());
	}

	@Test
	public void shouldFindResourcesFromFile() throws Exception {
		ResourceMap map = new ResourceMap(getClass(), "res/test", "txt");
		assertCollection(map.keys(), "A", "BB", "CCC");
		assertThat(map.resources().size(), is(3));
		assertArrayEquals(map.resource("A").data, new byte[] { 'a', 'a', 'a' });
		assertArrayEquals(map.resource("BB").data, new byte[] { 'b', 'b' });
		assertArrayEquals(map.resource("CCC").data, new byte[] { 'c' });
	}

	@Test
	public void shouldFindResourcesFromJar() throws Exception {
		ResourceMap map = new ResourceMap(Assert.class, "");
		assertTrue(map.keys().contains("Assert"));
		assertTrue(map.keys().contains("Before"));
	}

}
