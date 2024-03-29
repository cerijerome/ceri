package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class BlendTypeBehavior {

	@Test
	public void shouldBlendComponentsByAlpha() {
		assertEquals(BlendType.alpha.blend(0, 0), 0L);
		assertEquals(BlendType.alpha.blend(0, -1L), -1L);
		assertEquals(BlendType.alpha.blend(-1L, 0L), -1L);
		assertEquals(BlendType.alpha.blend(-1L, -1L), -1L);
		assertEquals(BlendType.alpha.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		assertEquals(BlendType.alpha.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0x22446688ffaacceeL);
		assertEquals(BlendType.alpha.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x88888888ff888888L);
		assertEquals(BlendType.alpha.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0x88888888ff888888L);
	}

	@Test
	public void shouldBlendComponentSums() {
		assertEquals(BlendType.sum.blend(0, 0), 0L);
		assertEquals(BlendType.sum.blend(0, -1L), -1L);
		assertEquals(BlendType.sum.blend(-1L, 0L), -1L);
		assertEquals(BlendType.sum.blend(-1L, -1L), -1L);
		assertEquals(BlendType.sum.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		assertEquals(BlendType.sum.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L), -1L);
		assertEquals(BlendType.sum.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x99aabbccffddeeffL);
		assertEquals(BlendType.sum.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xffeeddccffbbaa99L);
	}

	@Test
	public void shouldBlendComponentMaximums() {
		assertEquals(BlendType.max.blend(0, 0), 0L);
		assertEquals(BlendType.max.blend(0, -1L), -1L);
		assertEquals(BlendType.max.blend(-1L, 0L), -1L);
		assertEquals(BlendType.max.blend(-1L, -1L), -1L);
		assertEquals(BlendType.max.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		assertEquals(BlendType.max.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ffaacceeL);
		assertEquals(BlendType.max.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x77666688ffaacceeL);
		assertEquals(BlendType.max.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff666677L);
	}

	@Test
	public void shouldBlendComponentDiffs() {
		assertEquals(BlendType.diff.blend(0, 0), 0L);
		assertEquals(BlendType.diff.blend(0, -1L), -1L);
		assertEquals(BlendType.diff.blend(-1L, 0L), -1L);
		assertEquals(BlendType.diff.blend(-1L, -1L), Component.a.mask);
		assertEquals(BlendType.diff.blend(0x2244668800aacceeL, 0xeeccaa88ff664422L),
			0xeeccaa88ff664422L);
		assertEquals(BlendType.diff.blend(0x22446688ffaacceeL, 0xeeccaa88ff664422L),
			0xcc884400ff4488ccL);
		assertEquals(BlendType.diff.blend(0xeeccaa8880664422L, 0x22446688ffaacceeL),
			0x55221144ff77aaddL);
		assertEquals(BlendType.diff.blend(0x2244668880aacceeL, 0xeeccaa88ff664422L),
			0xddaa7744ff112255L);
	}

}
