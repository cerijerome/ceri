package ceri.ffm.type;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.test.FfmTestTypes.s16;
import ceri.ffm.test.FfmTestTypes.s32;
import ceri.ffm.test.FfmTestTypes.s64;
import ceri.ffm.test.FfmTestTypes.s8;
import ceri.ffm.test.FfmTestTypes.u16;
import ceri.ffm.test.FfmTestTypes.u32;
import ceri.ffm.test.FfmTestTypes.u64;
import ceri.ffm.test.FfmTestTypes.u8;

public class IntTypeBehavior {

	@Test
	public void shouldDetermineIfSameValue() {
		Assert.equal(s8.MAX.sameAs(new u8(0x7f)), true);
		Assert.equal(new u8(0x7f).sameAs(s8.MAX), true);
		Assert.equal(s16.MAX.sameAs(new u16(0x7fff)), true);
		Assert.equal(new u16(0x7fff).sameAs(s16.MAX), true);
		Assert.equal(s32.MAX.sameAs(new u32(0x7fffffff)), true);
		Assert.equal(new u32(0x7fffffff).sameAs(s32.MAX), true);
		Assert.equal(s64.MAX.sameAs(new u64(0x7fffffff_ffffffffL)), true);
		Assert.equal(new u64(0x7fffffff_ffffffffL).sameAs(s64.MAX), true);
		Assert.equal(u8.MAX.sameAs(new s16(0xff)), true);
		Assert.equal(new s16(0xff).sameAs(u8.MAX), true);
		Assert.equal(u16.MAX.sameAs(new s32(0xffff)), true);
		Assert.equal(new s32(0xffff).sameAs(u16.MAX), true);
		Assert.equal(u32.MAX.sameAs(new s64(0xffffffffL)), true);
		Assert.equal(new s64(0xffffffffL).sameAs(u32.MAX), true);
		Assert.equal(u64.MAX.sameAs(new u64(-1L)), true);
		Assert.equal(new s64(0x7fffffff_ffffffffL).sameAs(s64.MAX), true);
		Assert.equal(new u64(-1L).sameAs(u64.MAX), true);
	}
	
	@Test
	public void shouldDetermineIfNotSameValue() {
		Assert.equal(u32.MAX.sameAs(new s64(0xffffffff)), false);
		Assert.equal(u64.MAX.sameAs(new u64(-1)), false);
		Assert.equal(new u64(-1L).sameAs(new s64(-1L)), false);
		Assert.equal(new s64(-1L).sameAs(new u64(-1L)), false);
	}
}
