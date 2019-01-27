package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;

public class ResponseStreamBehavior {

	@Test
	public void shouldProcessStringInput() throws IOException {
		byte[] buffer = new byte[32];
		ResponseStream rs = ResponseStream.ascii(this::reverseString);
		rs.out().write(ascii("hello"));
		int count = rs.in().read(buffer);
		assertAscii(buffer, 0, count, "olleh");
	}

	@Test
	public void shouldProcessBinaryInput() throws IOException {
		byte[] buffer = new byte[32];
		ResponseStream rs = ResponseStream.of(this::reverseBytes);
		rs.out().write(ascii("hello"));
		int count = rs.in().read(buffer);
		assertAscii(buffer, 0, count, "olleh");
	}

	private void assertAscii(byte[] buffer, int offset, int len, String expected) {
		byte[] actual = Arrays.copyOfRange(buffer, offset, len);
		assertArray(actual, ascii(expected));
	}
	
	private byte[] ascii(String s) {
		return s.getBytes(StandardCharsets.US_ASCII);
	}
	
	private byte[] reverseBytes(byte[] b) {
		byte[] buffer = new byte[b.length];
		for (int i = 0; i < b.length; i++) buffer[i] = b[b.length - i - 1];
		return buffer;
	}
	
	private String reverseString(String s) {
		return new StringBuilder(s).reverse().toString();
	}
}
