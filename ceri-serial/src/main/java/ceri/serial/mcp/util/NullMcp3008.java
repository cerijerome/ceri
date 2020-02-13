package ceri.serial.mcp.util;

import java.io.IOException;
import ceri.serial.mcp.Mcp3008;
import ceri.serial.mcp.Mcp3008Input;

public class NullMcp3008 implements Mcp3008 {

	@Override
	public int value(Mcp3008Input input) throws IOException {
		return 0;
	}

	@Override
	public void close() {}

}
