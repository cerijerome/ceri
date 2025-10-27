package ceri.jna.clib;

import static ceri.jna.clib.FileDescriptor.FLAGS;
import static ceri.jna.clib.FileDescriptor.Open.CREAT;
import static ceri.jna.clib.FileDescriptor.Open.RDONLY;
import static ceri.jna.clib.FileDescriptor.Open.RDWR;
import static ceri.jna.clib.FileDescriptor.Open.TRUNC;
import static ceri.jna.clib.FileDescriptor.Open.WRONLY;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.jna.clib.FileDescriptor.Open;

public class FileDescriptorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNoOpFd() throws IOException {
		var captor = Captor.ofInt();
		FileDescriptor.NULL.accept(captor::accept);
		Assert.equal(FileDescriptor.NULL.apply(fd -> {
			captor.accept(fd);
			return 0;
		}), null);
		Assert.equal(FileDescriptor.NULL.in().read(), 0);
		FileDescriptor.NULL.out().write(0);
		FileDescriptor.NULL.blocking(false);
		Assert.unordered(FLAGS.getAll(FileDescriptor.NULL), RDONLY);
		FileDescriptor.NULL.close();
		Assert.find(FileDescriptor.NULL, ".*NULL$");
		captor.verifyInt(); // no calls
	}

	@Test
	public void shouldEncodeOpens() {
		Assert.equal(Open.encode(CREAT, TRUNC), CREAT.value | TRUNC.value);
		Assert.equal(Open.encode(RDONLY), 0);
	}

	@Test
	public void shouldDecodeOpens() {
		Assert.unordered(Open.decode(3), WRONLY, RDWR);
		Assert.unordered(Open.decode(0), RDONLY);
	}

	@Test
	public void shouldProvideOpenStringRepresentation() {
		Assert.equal(Open.string(3), "WRONLY|RDWR");
		Assert.equal(Open.string(0), "RDONLY");
	}
}
