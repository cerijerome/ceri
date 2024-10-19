package ceri.common.property;

import static ceri.common.property.Separator.DASH;
import static ceri.common.property.Separator.SLASH;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNpe;
import static ceri.common.test.AssertUtil.assertSame;
import org.junit.Test;

public class KeyBehavior {

	@Test
	public void shouldFailToCreateInvalidKey() {
		assertNpe(() -> new Key(DASH, null));
		assertNpe(() -> Key.of(null, ""));
		assertNpe(() -> Key.of(SLASH, (String[]) null).value());
		assertNpe(() -> Key.chomped(SLASH, (String[]) null).value());
		assertNpe(() -> Key.normalized(SLASH, (String[]) null).value());
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
		assertSame(key.append(null, ""), key);
		assertSame(key.chomp(null, "", "--"), key);
		assertSame(key.normalize("", null, "", "-", "--"), key);
		assertSame(key.normalize(DASH, null, "", "-", "--"), key);
	}

	private static void assertKey(Key key, String value) {
		assertEquals(key.value(), value);
	}
}
