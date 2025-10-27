package ceri.common.collect;

import org.junit.Test;
import ceri.common.test.Assert;

public class IdentityCacheTest {
	private static final I I0 = new I(0);

	public record I(int i) {}

	private static I i(int i) {
		return new I(i);
	}

	private static IdentityCache<I, String> cache() {
		return IdentityCache.of();
	}

	@Test
	public void testGet() {
		var cache = cache();
		Assert.equal(cache.get(null, _ -> "_"), null);
		Assert.equal(cache.get(I0, null), null);
		Assert.equal(cache.get(I0, _ -> "I0"), "I0");
		Assert.equal(cache.get(I0, _ -> "I00"), "I0");
		Assert.equal(cache.get(i(0), _ -> "i0"), "i0");
		Assert.equal(cache.get(i(0), _ -> "i00"), "i00");
		Assert.equal(cache.get(i(0), _ -> null), null);
		Assert.equal(cache.get(I0, null), "I0");
		Assert.equal(cache.get(i(0), null), null);
		Assert.equal(cache.get(i(1), null), null);
	}
}
