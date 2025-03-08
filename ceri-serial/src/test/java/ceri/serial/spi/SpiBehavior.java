package ceri.serial.spi;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.Direction;

public class SpiBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Spi.NULL.mode(SpiMode.MODE_3);
		Spi.NULL.lsbFirst(true);
		Spi.NULL.bitsPerWord(8);
		Spi.NULL.maxSpeedHz(555);
		assertEquals(Spi.NULL.mode(), SpiMode.MODE_0);
		assertEquals(Spi.NULL.lsbFirst(), false);
		assertEquals(Spi.NULL.bitsPerWord(), 0);
		assertEquals(Spi.NULL.maxSpeedHz(), 0);
		assertFind(Spi.NULL, ".*NULL$");
	}

	@Test
	public void shouldProvideNoOpTransfer() throws IOException {
		Spi.NULL.transfer(Direction.out, 3).execute();
	}
}
