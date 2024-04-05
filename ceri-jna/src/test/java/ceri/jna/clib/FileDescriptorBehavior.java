package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Captor;

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
		assertCollection(FileDescriptor.NULL.flags().getAll(), OpenFlag.O_RDONLY);
		FileDescriptor.NULL.close();
		captor.verifyInt(); // no calls
	}

}
