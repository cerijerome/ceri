package ceri.common.io;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestOutputStream;

public class ReplaceableOutputStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceStream() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			var out = TestOutputStream.of();
			rout.replace(out);
			rout.replace(out); // does nothing
			rout.replace(TestOutputStream.of());
			out.close.assertCalls(1);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyListenersOfErrors() throws IOException, InterruptedException {
		ValueCondition<Exception> sync = ValueCondition.of();
		TestOutputStream out = TestOutputStream.of();
		out.write.error.setFrom(ErrorGen.IOX);
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			rout.set(out);
			rout.errors().listen(sync::signal);
			assertThrown(() -> rout.write(0));
			assertThrowable(sync.await(), IOException.class);
			assertThrown(() -> rout.write(0xff));
			assertThrowable(sync.await(), IOException.class);
		}
	}

	@Test
	public void shouldFailWithAnInvalidStream() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			assertThrown(() -> rout.write(0));
			byte[] buffer = new byte[100];
			assertThrown(() -> rout.write(buffer));
			assertThrown(() -> rout.write(buffer, 1, 98));
			assertThrown(rout::flush);
		}
	}

	@Test
	public void shouldAllowOutputStreamToBeReplaced() throws IOException {
		try (ReplaceableOutputStream rout = new ReplaceableOutputStream()) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				try (ByteArrayOutputStream out2 = new ByteArrayOutputStream()) {
					rout.set(out);
					byte[] buffer = "test".getBytes();
					rout.write(buffer);
					rout.set(out2);
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
				rout.set(out);
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
