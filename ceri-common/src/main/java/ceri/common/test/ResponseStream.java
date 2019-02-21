package ceri.common.test;

import static ceri.common.util.BasicUtil.shouldNotThrow;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import ceri.common.io.IoStreamUtil;

/**
 * Provides an output stream that responds to input data. Can be created to handle bytes or Strings.
 * Can be used to simulate hardware devices.
 */
public class ResponseStream {
	private final InputStream externalIn;
	private final OutputStream externalOut;
	private final PipedOutputStream internalOut;
	private final Responder responder;

	public static interface Responder {
		byte[] respond(byte[] buffer, int offset, int len);
	}

	public static ResponseStream echo() {
		return of(Arrays::copyOfRange);
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
		internalOut = new PipedOutputStream();
		externalIn = pipedIn(internalOut);
		externalOut = IoStreamUtil.out(this::write);
	}

	public InputStream in() {
		return externalIn;
	}

	public OutputStream out() {
		return externalOut;
	}

	private void write(byte[] b, int offset, int len) throws IOException {
		byte[] dataOut = responder.respond(b, offset, len);
		internalOut.write(dataOut);
	}

	private PipedInputStream pipedIn(PipedOutputStream out) {
		return shouldNotThrow(() -> new PipedInputStream(out));
	}

}
