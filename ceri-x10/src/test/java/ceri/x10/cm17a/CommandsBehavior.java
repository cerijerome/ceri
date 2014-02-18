package ceri.x10.cm17a;

import static org.junit.Assert.assertArrayEquals;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.command.CommandFactory;
import ceri.x10.type.DimFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public class CommandsBehavior {
	private Commands commands;
	
	@Before
	public void init() throws IOException {
		commands = new Commands();
	}
	
	@Test
	public void shouldReturnTransmissionSequenceForUnitCommands() {
		assertArrayEquals(commands.unit(CommandFactory.off("A1")),
			bytes(0xd5, 0xaa, 0x60, 0x20, 0xad));
		assertArrayEquals(commands.unit(CommandFactory.on("J16")),
			bytes(0xd5, 0xaa, 0xf4, 0x58, 0xad));
		assertArrayEquals(commands.unit(CommandFactory.on("M1")),
			bytes(0xd5, 0xaa, 0x00, 0x00, 0xad));
	}

	@Test
	public void shouldReturnTransmissionSequenceForDimFunctions() {
		assertArrayEquals(commands.dim(new DimFunction(House.B, FunctionType.DIM, 0)),
			bytes(0xd5, 0xaa, 0x70, 0x98, 0xad));
		assertArrayEquals(commands.dim(new DimFunction(House.H, FunctionType.BRIGHT, 0)),
			bytes(0xd5, 0xaa, 0xb0, 0x88, 0xad));
		assertArrayEquals(commands.dim(new DimFunction(House.M, FunctionType.DIM, 0)),
			bytes(0xd5, 0xaa, 0x00, 0x98, 0xad));
	}

	private byte[] bytes(int...values) {
		byte[] bytes = new byte[values.length];
		int i = 0;
		for (int value : values) bytes[i++] = (byte)value;
		return bytes;
	}

}
