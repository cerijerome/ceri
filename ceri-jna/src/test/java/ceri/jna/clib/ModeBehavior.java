package ceri.jna.clib;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
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
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldEncodeMask() {
		Assert.equal(Mask.xcoder.encodeInt(Mask.rwxu, Mask.rwxg, Mask.rwxo), 0777);
		Assert.equal(Mask.xcoder.encodeInt(Mask.rusr, Mask.wusr, Mask.rgrp, Mask.wgrp, Mask.roth, Mask.woth),
			0666);
		Assert.equal(Mask.xcoder.encodeInt(Mask.fmt), 0170000);
		Assert.equal(Mask.xcoder.encodeInt(Mask.fmt, Mask.fsock, Mask.flnk), 0170000);
	}

	@Test
	public void shouldDecodeMask() {
		Assert.unordered(Mode.of(0777).masks(), Mask.rwxu, Mask.rwxg, Mask.rwxo);
		Assert.unordered(Mode.of(0666).masks(), Mask.rusr, Mask.wusr, Mask.rgrp, Mask.wgrp,
			Mask.roth, Mask.woth);
		Assert.unordered(Mask.xcoder.decodeAll(0170000), Mask.fmt);
		Assert.unordered(Mask.xcoder.decodeAll(0140000), Mask.fsock);
		Assert.unordered(Mask.xcoder.decodeAll(0120000), Mask.flnk);
	}

	@Test
	public void shouldDetermineIfModeContainsMasks() {
		Assert.yes(Mode.of(07).has());
		Assert.yes(Mode.of(07).has(Mask.rwxo));
		Assert.yes(Mode.of(07).has(Mask.roth, Mask.woth));
		Assert.no(Mode.of(07).has(Mask.roth, Mask.woth, Mask.rusr));
		Assert.no(Mode.of(07).has(Mask.rusr));
	}

	@Test
	public void shouldProvideMaskStringRepresentation() {
		Assert.equal(Mode.of(0777).maskString(), "rwxu|rwxg|rwxo");
		Assert.equal(Mode.of(0666).maskString(), "rusr|wusr|rgrp|wgrp|roth|woth");
	}
}
