package ceri.serial.mcp;

import java.io.IOException;
import ceri.common.data.ByteUtil;

/**
 * Interface for the SPI-controlled, 8-channel, 10-bit MCP3008 ADC.
 */
public interface Mcp3008 extends AutoCloseable {
	int DATA_SIZE = 3;
	int BITS = 10;
	int MAX_VALUE = ByteUtil.maskInt(BITS);
	int CHANNELS = Mcp3008Channel.COUNT;

	int value(Mcp3008Input input) throws IOException;

	default double ratio(Mcp3008Input input) throws IOException {
		return value(input) / MAX_VALUE;
	}

}
