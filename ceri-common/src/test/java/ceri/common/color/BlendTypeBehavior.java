package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;

public class BlendTypeBehavior {

	@Test
	public void shouldBlendComponentsByAlpha() {
		Assert.equal(Blend.alpha.blend(0, 0), 0L);
		Assert.equal(Blend.alpha.blend(0, -1L), -1L);
		Assert.equal(Blend.alpha.blend(-1L, 0L), -1L);
		Assert.equal(Blend.alpha.blend(-1L, -1L), -1L);
		Assert.equal(Blend.alpha.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		Assert.equal(Blend.alpha.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0x22446688ffaacceeL);
		Assert.equal(Blend.alpha.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x88888888ff888888L);
		Assert.equal(Blend.alpha.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0x88888888ff888888L);
	}

	@Test
	public void shouldBlendComponentSums() {
		Assert.equal(Blend.sum.blend(0, 0), 0L);
		Assert.equal(Blend.sum.blend(0, -1L), -1L);
		Assert.equal(Blend.sum.blend(-1L, 0L), -1L);
		Assert.equal(Blend.sum.blend(-1L, -1L), -1L);
		Assert.equal(Blend.sum.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		Assert.equal(Blend.sum.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L), -1L);
		Assert.equal(Blend.sum.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x99aabbccffddeeffL);
		Assert.equal(Blend.sum.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xffeeddccffbbaa99L);
	}

	@Test
	public void shouldBlendComponentMaximums() {
		Assert.equal(Blend.max.blend(0, 0), 0L);
		Assert.equal(Blend.max.blend(0, -1L), -1L);
		Assert.equal(Blend.max.blend(-1L, 0L), -1L);
		Assert.equal(Blend.max.blend(-1L, -1L), -1L);
		Assert.equal(Blend.max.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		Assert.equal(Blend.max.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ffaacceeL);
		Assert.equal(Blend.max.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x77666688ffaacceeL);
		Assert.equal(Blend.max.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff666677L);
	}

	@Test
	public void shouldBlendComponentDiffs() {
		Assert.equal(Blend.diff.blend(0, 0), 0L);
		Assert.equal(Blend.diff.blend(0, -1L), -1L);
		Assert.equal(Blend.diff.blend(-1L, 0L), -1L);
		Assert.equal(Blend.diff.blend(-1L, -1L), Component.a.mask);
		Assert.equal(Blend.diff.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		Assert.equal(Blend.diff.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0xcc884400ff4488ccL);
		Assert.equal(Blend.diff.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x55221144ff77aaddL);
		Assert.equal(Blend.diff.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xddaa7744ff112255L);
	}

}
