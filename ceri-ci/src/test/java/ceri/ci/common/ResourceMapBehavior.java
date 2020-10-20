package ceri.ci.common;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.test.LogModifier;

public class ResourceMapBehavior {

	@Test
	public void shouldVerifyKeys() throws IOException {
		ResourceMap map = new ResourceMap(getClass(), "res/test", "txt");
		LogModifier.run(() -> {
			Collection<String> verifiedKeys = map.verifyAll(Arrays.asList("A", "B"));
			assertCollection(verifiedKeys, "A");
		}, Level.ERROR, ResourceMap.class);
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
		assertEquals(map.resources().size(), 3);
		assertArray(map.resource("A").data, 'a', 'a', 'a');
		assertArray(map.resource("BB").data, 'b', 'b');
		assertArray(map.resource("CCC").data, 'c');
	}

	@Test
	public void shouldFindResourcesFromJar() throws Exception {
		ResourceMap map = new ResourceMap(ResourceMap.class, "");
		assertTrue(map.keys().contains("Resource"));
		assertTrue(map.keys().contains("ResourceMap"));
	}

}
