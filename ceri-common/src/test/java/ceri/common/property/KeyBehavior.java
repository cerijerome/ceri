package ceri.common.property;

import org.junit.Test;
import ceri.common.test.Assert;

public class KeyBehavior {

	@Test
	public void shouldFailToCreateInvalidKey() {
		Assert.nullPointer(() -> new Key(Separator.DASH, null));
		Assert.nullPointer(() -> Key.of(null, ""));
		Assert.nullPointer(() -> Key.of(Separator.SLASH, (String[]) null).value());
		Assert.nullPointer(() -> Key.chomped(Separator.SLASH, (String[]) null).value());
		Assert.nullPointer(() -> Key.normalized(Separator.SLASH, (String[]) null).value());
	}

	@Test
	public void shouldCreateKeyByJoiningParts() {
		assertKey(Key.of(Separator.SLASH), "");
		assertKey(Key.of(Separator.SLASH, ""), "");
		assertKey(Key.of(Separator.SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.of(Separator.SLASH, "/a", "/bc"), "/a//bc");
	}

	@Test
	public void shouldCreateKeyByChompingParts() {
		assertKey(Key.chomped(Separator.SLASH), "");
		assertKey(Key.chomped(Separator.SLASH, ""), "");
		assertKey(Key.chomped(Separator.SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.chomped(Separator.SLASH, "/a///", "/b//c"), "a/b//c");
	}

	@Test
	public void shouldCreateKeyByNormalizingParts() {
		assertKey(Key.normalized(Separator.SLASH), "");
		assertKey(Key.normalized(Separator.SLASH, ""), "");
		assertKey(Key.normalized(Separator.SLASH, "", null, "a", null, "bc", null), "a/bc");
		assertKey(Key.normalized(Separator.SLASH, "/a///", "/b//c"), "a/b/c");
	}

	@Test
	public void shouldDetermineIfRoot() {
		Assert.equal(Key.of(Separator.DASH).isRoot(), true);
		Assert.equal(Key.of(Separator.DASH, "").isRoot(), true);
		Assert.equal(Key.of(Separator.DASH, "a").isRoot(), false);
	}

	@Test
	public void shouldAppendSubParts() {
		assertKey(Key.of(Separator.DASH, "-a--b-").append("-c", null, "", "d--e"), "-a--b---c-d--e");
	}

	@Test
	public void shouldChompWithSubParts() {
		assertKey(Key.of(Separator.DASH, "-a--b-").chomp("-c", null, "", "d--e"), "a--b-c-d--e");
	}

	@Test
	public void shouldNormalizedWithSubParts() {
		assertKey(Key.of(Separator.DASH, "-a--b-").normalize("-c", null, "", "d--e"), "a-b-c-d-e");
		assertKey(Key.of(Separator.DASH, "-a--b-").normalize(Separator.SLASH, "-c", null, "", "d--e"), "a/b/c/d/e");
	}

	@Test
	public void shouldReturnSameKeyIfUnchanged() {
		var key = Key.of(Separator.DASH, "a/b");
		Assert.same(key.append(null, ""), key);
		Assert.same(key.chomp(null, "", "--"), key);
		Assert.same(key.normalize("", null, "", "-", "--"), key);
		Assert.same(key.normalize(Separator.DASH, null, "", "-", "--"), key);
	}

	private static void assertKey(Key key, String value) {
		Assert.equal(key.value(), value);
	}
}
