package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.jna.clib.FileDescriptor.FLAGS;
import static ceri.jna.clib.FileDescriptor.Open.CREAT;
import static ceri.jna.clib.FileDescriptor.Open.RDONLY;
import static ceri.jna.clib.FileDescriptor.Open.RDWR;
import static ceri.jna.clib.FileDescriptor.Open.TRUNC;
import static ceri.jna.clib.FileDescriptor.Open.WRONLY;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.jna.clib.FileDescriptor.Open;

public class FileDescriptorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNoOpFd() throws IOException {
		var captor = Captor.ofInt();
		FileDescriptor.NULL.accept(captor::accept);
		assertEquals(FileDescriptor.NULL.apply(fd -> {
			captor.accept(fd);
			return 0;
		}), null);
		assertEquals(FileDescriptor.NULL.in().read(), 0);
		FileDescriptor.NULL.out().write(0);
		FileDescriptor.NULL.blocking(false);
		assertCollection(FLAGS.get(FileDescriptor.NULL), RDONLY);
		FileDescriptor.NULL.close();
		assertFind(FileDescriptor.NULL, ".*NULL$");
		captor.verifyInt(); // no calls
	}

	@Test
	public void shouldEncodeOpens() {
		assertEquals(Open.encode(CREAT, TRUNC), CREAT.value | TRUNC.value);
		assertEquals(Open.encode(RDONLY), 0);
	}

	@Test
	public void shouldDecodeOpens() {
		assertCollection(Open.decode(3), WRONLY, RDWR);
		assertCollection(Open.decode(0), RDONLY);
	}

	@Test
	public void shouldProvideOpenStringRepresentation() {
		assertEquals(Open.string(3), "WRONLY|RDWR");
		assertEquals(Open.string(0), "RDONLY");
	}
}
