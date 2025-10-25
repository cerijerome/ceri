package ceri.common.property;

import static ceri.common.property.Separator.DASH;
import static ceri.common.property.Separator.SLASH;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.nullPointer;
import org.junit.Test;
import ceri.common.test.Assert;

public class KeyBehavior {

	@Test
	public void shouldFailToCreateInvalidKey() {
		nullPointer(() -> new Key(DASH, null));
		nullPointer(() -> Key.of(null, ""));
		nullPointer(() -> Key.of(SLASH, (String[]) null).value());
		nullPointer(() -> Key.chomped(SLASH, (String[]) null).value());
		nullPointer(() -> Key.normalized(SLASH, (String[]) null).value());
	}

	@Test
	public void shouldCreateKeyByJoiningParts() {
		assertKey(Key.of(SLASH), "");
		assertKey(Key.of(SLASH, ""), "");
		assertKey(Key.of(SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.of(SLASH, "/a", "/bc"), "/a//bc");
	}

	@Test
	public void shouldCreateKeyByChompingParts() {
		assertKey(Key.chomped(SLASH), "");
		assertKey(Key.chomped(SLASH, ""), "");
		assertKey(Key.chomped(SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.chomped(SLASH, "/a///", "/b//c"), "a/b//c");
	}

	@Test
	public void shouldCreateKeyByNormalizingParts() {
		assertKey(Key.normalized(SLASH), "");
		assertKey(Key.normalized(SLASH, ""), "");
		assertKey(Key.normalized(SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.normalized(SLASH, "/a///", "/b//c"), "a/b/c");
	}

	@Test
	public void shouldDetermineIfRoot() {
		assertEquals(Key.of(DASH).isRoot(), true);
		assertEquals(Key.of(DASH, "").isRoot(), true);
		assertEquals(Key.of(DASH, "a").isRoot(), false);
	}

	@Test
	public void shouldAppendSubParts() {
		assertKey(Key.of(DASH, "-a--b-").append("-c", null, "", "d--e"), "-a--b---c-d--e");
	}

	@Test
	public void shouldChompWithSubParts() {
		assertKey(Key.of(DASH, "-a--b-").chomp("-c", null, "", "d--e"), "a--b-c-d--e");
	}

	@Test
	public void shouldNormalizedWithSubParts() {
		assertKey(Key.of(DASH, "-a--b-").normalize("-c", null, "", "d--e"), "a-b-c-d-e");
		assertKey(Key.of(DASH, "-a--b-").normalize(SLASH, "-c", null, "", "d--e"), "a/b/c/d/e");
	}

	@Test
	public void shouldReturnSameKeyIfUnchanged() {
		var key = Key.of(DASH, "a/b");
		Assert.same(key.append(null, ""), key);
		Assert.same(key.chomp(null, "", "--"), key);
		Assert.same(key.normalize("", null, "", "-", "--"), key);
		Assert.same(key.normalize(DASH, null, "", "-", "--"), key);
	}

	private static void assertKey(Key key, String value) {
		assertEquals(key.value(), value);
	}
}
