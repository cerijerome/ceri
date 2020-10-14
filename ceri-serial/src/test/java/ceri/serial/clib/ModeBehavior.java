package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.clib.Mode.Mask.flnk;
import static ceri.serial.clib.Mode.Mask.fmt;
import static ceri.serial.clib.Mode.Mask.fsock;
import static ceri.serial.clib.Mode.Mask.rgrp;
import static ceri.serial.clib.Mode.Mask.roth;
import static ceri.serial.clib.Mode.Mask.rusr;
import static ceri.serial.clib.Mode.Mask.rwxg;
import static ceri.serial.clib.Mode.Mask.rwxo;
import static ceri.serial.clib.Mode.Mask.rwxu;
import static ceri.serial.clib.Mode.Mask.wgrp;
import static ceri.serial.clib.Mode.Mask.woth;
import static ceri.serial.clib.Mode.Mask.wusr;
import static ceri.serial.clib.Mode.Mask.xusr;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.serial.clib.Mode.Mask;

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
		assertThat(Mask.encode(rwxu, rwxg, rwxo), is(0777));
		assertThat(Mask.encode(rusr, wusr, rgrp, wgrp, roth, woth), is(0666));
		assertThat(Mask.encode(fmt), is(0170000));
		assertThat(Mask.encode(fmt, fsock, flnk), is(0170000));
	}

	@Test
	public void shouldDecodeMask() {
		assertCollection(Mode.of(0777).masks(), rwxu, rwxg, rwxo);
		assertCollection(Mode.of(0666).masks(), rusr, wusr, rgrp, wgrp, roth, woth);
		assertCollection(Mask.decode(0170000), fmt);
		assertCollection(Mask.decode(0140000), fsock);
		assertCollection(Mask.decode(0120000), flnk);
	}

	@Test
	public void shouldDetermineIfModeContainsMasks() {
		assertThat(Mode.of(07).has(), is(true));
		assertThat(Mode.of(07).has(rwxo), is(true));
		assertThat(Mode.of(07).has(roth, woth), is(true));
		assertThat(Mode.of(07).has(roth, woth, rusr), is(false));
		assertThat(Mode.of(07).has(rusr), is(false));
	}

	@Test
	public void shouldProvideMaskStringRepresentation() {
		assertThat(Mode.of(0777).maskString(), is("rwxu|rwxg|rwxo"));
		assertThat(Mode.of(0666).maskString(), is("rusr|wusr|rgrp|wgrp|roth|woth"));
	}

}
