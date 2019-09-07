package ceri.x10.cm17a;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.cm17a.Commands.Key;
import ceri.x10.command.CommandFactory;
import ceri.x10.type.DimFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class CommandsBehavior {
	private Commands commands;

	@Before
	public void init() throws IOException {
		commands = new Commands();
	}

	@Test
	public void shouldHaveKeysThatFollowTheEqualsContract() {
		Key key1 = new Key(House.J, Unit._13, FunctionType.ON);
		Key key2 = new Key(House.J, Unit._13, FunctionType.ON);
		Key key3 = new Key(House.J, Unit._13, FunctionType.OFF);
		Key key4 = new Key(House.J, Unit._12, FunctionType.ON);
		Key key5 = new Key(House.K, Unit._13, FunctionType.ON);
		Key key6 = new Key(House.J, null, FunctionType.DIM);
		assertThat(key1, is(key2));
		assertThat(key1, is(key1));
		assertThat(key1.toString(), is(key1.toString()));
		assertNotEquals(null, key1);
		assertThat(key1, not(new Object()));
		assertThat(key1, not(key3));
		assertThat(key1, not(key4));
		assertThat(key1, not(key5));
		assertThat(key6, is(key6));
		assertThat(key6.toString(), is(key6.toString()));
	}

	@Test
	public void shouldReturnTransmissionSequenceForUnitCommands() {
		assertArrayEquals(commands.unit(CommandFactory.off("A1")), bytes(0xd5, 0xaa, 0x60, 0x20,
			0xad));
		assertArrayEquals(commands.unit(CommandFactory.on("J16")), bytes(0xd5, 0xaa, 0xf4, 0x58,
			0xad));
		assertArrayEquals(commands.unit(CommandFactory.on("M1")), bytes(0xd5, 0xaa, 0x00, 0x00,
			0xad));
	}

	@Test
	public void shouldReturnTransmissionSequenceForDimFunctions() {
		assertArrayEquals(commands.dim(new DimFunction(House.B, FunctionType.DIM, 0)), bytes(0xd5,
			0xaa, 0x70, 0x98, 0xad));
		assertArrayEquals(commands.dim(new DimFunction(House.H, FunctionType.BRIGHT, 0)), bytes(
			0xd5, 0xaa, 0xb0, 0x88, 0xad));
		assertArrayEquals(commands.dim(new DimFunction(House.M, FunctionType.DIM, 0)), bytes(0xd5,
			0xaa, 0x00, 0x98, 0xad));
	}

	private byte[] bytes(int... values) {
		byte[] bytes = new byte[values.length];
		int i = 0;
		for (int value : values)
			bytes[i++] = (byte) value;
		return bytes;
	}

}
