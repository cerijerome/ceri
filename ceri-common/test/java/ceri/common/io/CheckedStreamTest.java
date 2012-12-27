package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class CheckedStreamTest {

	@Test
	public void testBasic() throws IOException {
		byte[] b = new byte[10000];
		for (int i = 0; i < b.length; i++)
			b[i] = (byte) i;

		for (int len = 1; len < 10000; len++) {
			System.out.println(len);
			Checksum inChecksum = new Adler32();
			InputStream bIn = new ByteArrayInputStream(b);
			InputStream bfIn = new BufferedInputStream(bIn);
			InputStream cIn = new CheckedInputStream(bfIn, inChecksum);
			try (InputStream in = cIn) {
				byte[] buffer = new byte[len];
				int count = in.read(buffer);
				assertThat(count, is(buffer.length));
			}

			Checksum outChecksum = new Adler32();
			ByteArrayOutputStream bOut = new ByteArrayOutputStream(50);
			OutputStream bfOut = new BufferedOutputStream(bOut);
			OutputStream cOut = new CheckedOutputStream(bfOut, outChecksum);
			try (OutputStream out = cOut) {
				out.write(b, 0, len);
				out.flush();
			}
			assertThat(inChecksum.getValue(), is(outChecksum.getValue()));
		}
	}

	@Test
	public void testWithZip() throws IOException {
		int n;
		Checksum outChecksum = new Adler32();
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		CheckedOutputStream cOut =
			new CheckedOutputStream(new BufferedOutputStream(bOut), outChecksum);
		try (ZipOutputStream zOut = new ZipOutputStream(cOut)) {
			int count = 1000;
			for (int i = 0; i < count; i++) {
				ZipEntry entry = new ZipEntry("z" + (100 + i));
				entry.setTime(0);
				zOut.putNextEntry(entry);
				zOut.write("abcdefghijklmnopqrstuvwxyz".getBytes());
				zOut.closeEntry();
			}
			zOut.flush();
			zOut.finish();
		}

		System.out.println("out-chk: " + outChecksum.getValue());
		byte[] b = bOut.toByteArray();
		System.out.println(b.length + ": " + TestUtil.toReadableString(b));

		Checksum inChecksum = new Adler32();
		ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		BufferedInputStream bfIn = new BufferedInputStream(bIn);
		CheckedInputStream cIn = new CheckedInputStream(bfIn, inChecksum);
		try (ZipInputStream zIn = new ZipInputStream(cIn)) {
			ZipEntry entry;
			byte[] buffer = new byte[20000];
			n = zIn.read(buffer);
			System.out.println(n);
	
			while ((entry = zIn.getNextEntry()) != null) {
				System.out.print(entry.getName() + ": ");
				n = zIn.read(buffer);
				System.out.println(n + ": " + new String(buffer, 0, n));
				zIn.closeEntry();
			}
			InputStream in = cIn;
			while (true) {
				n = in.read(new byte[100000]);
				System.out.print(n + " ");
				if (n == -1 || n == 0) break;
			}
		}
		System.out.println();
		System.out.println("out-chk: " + outChecksum.getValue());
		System.out.println("in-chk:  " + inChecksum.getValue());
	}

}
