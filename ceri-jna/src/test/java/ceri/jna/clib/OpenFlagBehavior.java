package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.jna.clib.OpenFlag.O_CREAT;
import static ceri.jna.clib.OpenFlag.O_RDONLY;
import static ceri.jna.clib.OpenFlag.O_RDWR;
import static ceri.jna.clib.OpenFlag.O_TRUNC;
import static ceri.jna.clib.OpenFlag.O_WRONLY;
import org.junit.Test;

public class OpenFlagBehavior {

	@Test
	public void shouldEncodeFlags() {
		assertEquals(OpenFlag.xcoder.encodeInt(O_CREAT, O_TRUNC), O_CREAT.value | O_TRUNC.value);
		assertEquals(OpenFlag.xcoder.encodeInt(O_RDONLY), 0);
	}

	@Test
	public void shouldDecodeFlags() {
		assertCollection(OpenFlag.xcoder.decodeAll(3), O_WRONLY, O_RDWR);
		assertCollection(OpenFlag.xcoder.decodeAll(0), O_RDONLY);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(OpenFlag.string(3), "O_WRONLY|O_RDWR");
		assertEquals(OpenFlag.string(0), "O_RDONLY");
	}

}
