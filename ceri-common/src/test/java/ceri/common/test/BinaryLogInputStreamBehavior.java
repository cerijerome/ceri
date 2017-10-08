package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.BinaryLogInputStream;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;

public class BinaryLogInputStreamBehavior {
	private ByteArrayInputStream bIn;
	private StringBuilder s;
	private BinaryPrinter printer;
	private BinaryLogInputStream in;

	private void init(int bytesPerColumn, byte... bytes) {
		s = new StringBuilder();
		printer =
			BinaryPrinter.builder().bytesPerColumn(bytesPerColumn).out(StringUtil.asPrintStream(s))
				.build();
		bIn = new ByteArrayInputStream(bytes);
		in = new BinaryLogInputStream(printer, bIn);
	}

	@Test
	public void shouldLogReadingOfOneByte() throws IOException {
		init(1, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0, (byte) -1);
		assertThat(in.read(), is(Byte.MIN_VALUE & 0xff));
		assertThat(s.toString(), is("10000000  80  .\n"));
		s.setLength(0);
		assertThat(in.read(), is(Byte.MAX_VALUE & 0xff));
		assertThat(s.toString(), is("01111111  7F  .\n"));
		s.setLength(0);
		assertThat(in.read(), is(0));
		assertThat(s.toString(), is("00000000  00  .\n"));
		s.setLength(0);
		assertThat(in.read(), is(0xff));
		assertThat(s.toString(), is("11111111  FF  .\n"));
	}

	@Test
	public void shouldLogReadingOfBytes() throws IOException {
		byte[] b0 = new byte[] { 0, Byte.MIN_VALUE, Byte.MAX_VALUE, -1, 0 };
		byte[] b1 = new byte[] { Byte.MIN_VALUE, Byte.MAX_VALUE, -1 };
		init(3, b1);
		byte[] b = new byte[5];
		int count = in.read(b, 1, 4);
		assertThat(count, is(3));
		assertArrayEquals(b0, b);
		assertThat(s.toString(), is("10000000 01111111 11111111  80 7F FF  ...\n"));
	}

	@Test
	public void shouldNotLogForEndOfFile() throws IOException {
		init(1, (byte) 0);
		in.read();
		s.setLength(0);
		assertThat(in.read(), is(-1));
		assertThat(s.toString(), is(""));
	}
	
}
