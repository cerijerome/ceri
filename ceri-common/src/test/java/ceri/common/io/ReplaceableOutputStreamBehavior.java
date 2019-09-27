package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertIterable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.test.TestUtil;

public class ReplaceableOutputStreamBehavior {

	@Test
	public void shouldNotifyListenersOfErrors() throws IOException {
		List<String> list = new ArrayList<>();
		try (OutputStream out = Mockito.mock(OutputStream.class)) {
			try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
				rout.setOutputStream(out);
				Consumer<Exception> consumer = e -> list.add(e.getMessage());
				rout.listeners().listen(consumer);
				doThrow(new IOException("1")).when(out).write(anyInt());
				TestUtil.assertThrown(() -> rout.write(0));
				doThrow(new IOException("2")).when(out).write(anyInt());
				TestUtil.assertThrown(() -> rout.write(0xff));
				rout.listeners().unlisten(consumer);
				assertIterable(list, "1", "2");
			}
		}
	}

	@Test
	public void shouldFailWithAnInvalidStream() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			TestUtil.assertThrown(() -> rout.write(0));
			byte[] buffer = new byte[100];
			TestUtil.assertThrown(() -> rout.write(buffer));
			TestUtil.assertThrown(() -> rout.write(buffer, 1, 98));
			TestUtil.assertThrown(rout::flush);
		}
	}

	@Test
	public void shouldAllowOutputStreamToBeReplaced() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				try (ByteArrayOutputStream out2 = new ByteArrayOutputStream()) {
					rout.setOutputStream(out);
					byte[] buffer = "test".getBytes();
					rout.write(buffer);
					rout.setOutputStream(out2);
					rout.write(buffer, 1, 3);
					assertArray(out.toByteArray(), "test".getBytes());
					assertArray(out2.toByteArray(), "est".getBytes());
				}
			}
		}
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				rout.setOutputStream(out);
				byte[] buffer = "test".getBytes();
				rout.write(buffer);
				rout.write('t');
				rout.write(buffer, 1, 3);
				rout.flush();
				assertArray(out.toByteArray(), "testtest".getBytes());
			}
		}
	}

}
