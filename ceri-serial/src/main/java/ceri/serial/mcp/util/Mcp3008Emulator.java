package ceri.serial.mcp.util;

import java.io.IOException;
import ceri.common.function.ExceptionToIntFunction;
import ceri.serial.mcp.Mcp3008;
import ceri.serial.mcp.Mcp3008Input;

public class Mcp3008Emulator implements Mcp3008 {
	private final ExceptionToIntFunction<IOException, Mcp3008Input> responder;

	public static Mcp3008Emulator of(ExceptionToIntFunction<IOException, Mcp3008Input> responder) {
		if (responder == null) responder = i -> 0;
		return new Mcp3008Emulator(responder);
	}

	private Mcp3008Emulator(ExceptionToIntFunction<IOException, Mcp3008Input> responder) {
		this.responder = responder;
	}

	@Override
	public int value(Mcp3008Input input) throws IOException {
		return responder.applyAsInt(input);
	}

	@Override
	public void close() {}

}
