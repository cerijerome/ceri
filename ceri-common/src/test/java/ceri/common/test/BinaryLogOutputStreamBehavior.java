package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.text.StringUtil;

public class BinaryLogOutputStreamBehavior {
	private ByteArrayOutputStream bOut;
	private StringBuilder s;
	private BinaryLogOutputStream out;

	private void init(int bytesPerColumn) {
		s = new StringBuilder();
		BinaryPrinter printer = BinaryPrinter.builder().bytesPerColumn(bytesPerColumn) //
			.out(StringUtil.asPrintStream(s)).build();
		bOut = new ByteArrayOutputStream();
		out = new BinaryLogOutputStream(printer, bOut);
	}

	@Test
	public void shouldLogReadingOfOneByte() throws IOException {
		init(1);
		out.write(Byte.MIN_VALUE);
		assertArrayEquals(bOut.toByteArray(), new byte[] { Byte.MIN_VALUE });
		assertThat(s.toString(), is("10000000  80  .\n"));
		bOut.reset();
		s.setLength(0);
		out.write(Byte.MAX_VALUE);
		assertArrayEquals(bOut.toByteArray(), new byte[] { Byte.MAX_VALUE });
		assertThat(s.toString(), is("01111111  7F  .\n"));
		bOut.reset();
		s.setLength(0);
		out.write(0);
		assertArrayEquals(bOut.toByteArray(), new byte[] { 0 });
		assertThat(s.toString(), is("00000000  00  .\n"));
		bOut.reset();
		s.setLength(0);
		out.write(-1);
		assertArrayEquals(bOut.toByteArray(), new byte[] { -1 });
		assertThat(s.toString(), is("11111111  FF  .\n"));
	}

	@Test
	public void shouldLogReadingOfBytes() throws IOException {
		init(2);
		byte[] b0 = { Byte.MAX_VALUE };
		byte[] b1 = { 0, Byte.MIN_VALUE, -1, 0 };
		out.write(b0);
		assertArrayEquals(bOut.toByteArray(), new byte[] { Byte.MAX_VALUE });
		assertThat(s.toString(), is("01111111           7F     . \n"));
		bOut.reset();
		s.setLength(0);
		out.write(b1, 1, 2);
		assertArrayEquals(bOut.toByteArray(), new byte[] { Byte.MIN_VALUE, -1 });
		assertThat(s.toString(), is("10000000 11111111  80 FF  ..\n"));
	}

}
