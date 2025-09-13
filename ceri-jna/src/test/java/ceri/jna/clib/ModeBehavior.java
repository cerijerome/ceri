package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.jna.clib.Mode.Mask;

public class ModeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Mode.of(0127);
		var eq0 = Mode.of(0127);
		var eq1 = Mode.of(Mask.xusr, Mask.wgrp, Mask.rwxo);
		var eq2 = Mode.builder().add(Mask.xusr, Mask.wgrp, Mask.rwxo).build();
		var ne0 = Mode.of(0126);
		var ne1 = Mode.of(Mask.rwxu, Mask.wgrp, Mask.rwxo);
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldEncodeMask() {
		assertEquals(Mask.encode(Mask.rwxu, Mask.rwxg, Mask.rwxo), 0777);
		assertEquals(Mask.encode(Mask.rusr, Mask.wusr, Mask.rgrp, Mask.wgrp, Mask.roth, Mask.woth),
			0666);
		assertEquals(Mask.encode(Mask.fmt), 0170000);
		assertEquals(Mask.encode(Mask.fmt, Mask.fsock, Mask.flnk), 0170000);
	}

	@Test
	public void shouldDecodeMask() {
		assertUnordered(Mode.of(0777).masks(), Mask.rwxu, Mask.rwxg, Mask.rwxo);
		assertUnordered(Mode.of(0666).masks(), Mask.rusr, Mask.wusr, Mask.rgrp, Mask.wgrp,
			Mask.roth, Mask.woth);
		assertUnordered(Mask.decode(0170000), Mask.fmt);
		assertUnordered(Mask.decode(0140000), Mask.fsock);
		assertUnordered(Mask.decode(0120000), Mask.flnk);
	}

	@Test
	public void shouldDetermineIfModeContainsMasks() {
		assertTrue(Mode.of(07).has());
		assertTrue(Mode.of(07).has(Mask.rwxo));
		assertTrue(Mode.of(07).has(Mask.roth, Mask.woth));
		assertFalse(Mode.of(07).has(Mask.roth, Mask.woth, Mask.rusr));
		assertFalse(Mode.of(07).has(Mask.rusr));
	}

	@Test
	public void shouldProvideMaskStringRepresentation() {
		assertEquals(Mode.of(0777).maskString(), "rwxu|rwxg|rwxo");
		assertEquals(Mode.of(0666).maskString(), "rusr|wusr|rgrp|wgrp|roth|woth");
	}
}
