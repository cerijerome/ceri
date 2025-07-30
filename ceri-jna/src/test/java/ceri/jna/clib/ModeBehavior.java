package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.jna.clib.Mode.Mask.flnk;
import static ceri.jna.clib.Mode.Mask.fmt;
import static ceri.jna.clib.Mode.Mask.fsock;
import static ceri.jna.clib.Mode.Mask.rgrp;
import static ceri.jna.clib.Mode.Mask.roth;
import static ceri.jna.clib.Mode.Mask.rusr;
import static ceri.jna.clib.Mode.Mask.rwxg;
import static ceri.jna.clib.Mode.Mask.rwxo;
import static ceri.jna.clib.Mode.Mask.rwxu;
import static ceri.jna.clib.Mode.Mask.wgrp;
import static ceri.jna.clib.Mode.Mask.woth;
import static ceri.jna.clib.Mode.Mask.wusr;
import static ceri.jna.clib.Mode.Mask.xusr;
import org.junit.Test;
import ceri.jna.clib.Mode.Mask;

public class ModeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Mode t = Mode.of(0127);
		Mode eq0 = Mode.of(0127);
		Mode eq1 = Mode.of(xusr, wgrp, rwxo);
		Mode eq2 = Mode.builder().add(xusr, wgrp, rwxo).build();
		Mode ne0 = Mode.of(0126);
		Mode ne1 = Mode.of(rwxu, wgrp, rwxo);
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldEncodeMask() {
		assertEquals(Mask.encode(rwxu, rwxg, rwxo), 0777);
		assertEquals(Mask.encode(rusr, wusr, rgrp, wgrp, roth, woth), 0666);
		assertEquals(Mask.encode(fmt), 0170000);
		assertEquals(Mask.encode(fmt, fsock, flnk), 0170000);
	}

	@Test
	public void shouldDecodeMask() {
		assertUnordered(Mode.of(0777).masks(), rwxu, rwxg, rwxo);
		assertUnordered(Mode.of(0666).masks(), rusr, wusr, rgrp, wgrp, roth, woth);
		assertUnordered(Mask.decode(0170000), fmt);
		assertUnordered(Mask.decode(0140000), fsock);
		assertUnordered(Mask.decode(0120000), flnk);
	}

	@Test
	public void shouldDetermineIfModeContainsMasks() {
		assertTrue(Mode.of(07).has());
		assertTrue(Mode.of(07).has(rwxo));
		assertTrue(Mode.of(07).has(roth, woth));
		assertFalse(Mode.of(07).has(roth, woth, rusr));
		assertFalse(Mode.of(07).has(rusr));
	}

	@Test
	public void shouldProvideMaskStringRepresentation() {
		assertEquals(Mode.of(0777).maskString(), "rwxu|rwxg|rwxo");
		assertEquals(Mode.of(0666).maskString(), "rusr|wusr|rgrp|wgrp|roth|woth");
	}

}
