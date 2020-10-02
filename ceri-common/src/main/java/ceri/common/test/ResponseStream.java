package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.PipedStream;
import ceri.common.text.ToString;

/**
 * Provides an output stream that responds to input data. Can be created to handle bytes or Strings.
 * Can be used to simulate hardware devices.
 */
public class ResponseStream {
	private final OutputStream out;
	private final PipedStream piped;
	private final Responder responder;

	public interface Responder {
		byte[] respond(byte[] buffer, int offset, int len);

		static Responder named(Responder responder, String name) {
			return new Responder() {
				@Override
				public byte[] respond(byte[] buffer, int offset, int len) {
					return responder.respond(buffer, offset, len);
				}

				@Override
				public String toString() {
					return name;
				}
			};
		}
	}

	public static ResponseStream echo() {
		return of(Responder.named(Arrays::copyOfRange, "echo"));
	}

	public static ResponseStream ascii(Function<String, String> responder) {
		return string(responder, StandardCharsets.ISO_8859_1);
	}

	public static ResponseStream string(Function<String, String> responder, Charset charset) {
		return of((buffer, offset, len) -> {
			String s = new String(buffer, offset, len, charset);
			return responder.apply(s).getBytes(charset);
		});
	}

	public static ResponseStream of(Function<byte[], byte[]> responder) {
		return of((buffer, offset, len) -> responder
			.apply(Arrays.copyOfRange(buffer, offset, offset + len)));
	}

	public static ResponseStream of(Responder responder) {
		return new ResponseStream(responder);
	}

	private ResponseStream(Responder responder) {
		this.responder = responder;
		piped = PipedStream.of();
		out = IoStreamUtil.out(this::write);
	}

	public InputStream in() {
		return piped.in();
	}

	public OutputStream out() {
		return out;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, responder);
	}

	@SuppressWarnings("resource")
	private void write(byte[] b, int offset, int len) throws IOException {
		byte[] dataOut = responder.respond(b, offset, len);
		piped.out().write(dataOut);
	}

}
