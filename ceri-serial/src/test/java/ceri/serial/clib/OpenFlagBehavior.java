package ceri.serial.clib;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.serial.clib.OpenFlag.O_CREAT;
import static ceri.serial.clib.OpenFlag.O_RDONLY;
import static ceri.serial.clib.OpenFlag.O_RDWR;
import static ceri.serial.clib.OpenFlag.O_TRUNC;
import static ceri.serial.clib.OpenFlag.O_WRONLY;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class OpenFlagBehavior {

	@Test
	public void shouldEncodeFlags() {
		assertThat(OpenFlag.encode(O_CREAT, O_TRUNC), is(O_CREAT.value | O_TRUNC.value));
		assertThat(OpenFlag.encode(O_RDONLY), is(0));
	}

	@Test
	public void shouldDecodeFlags() {
		assertCollection(OpenFlag.decode(3), O_WRONLY, O_RDWR);
		assertCollection(OpenFlag.decode(0), O_RDONLY);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertThat(OpenFlag.string(3), is("O_WRONLY|O_RDWR"));
		assertThat(OpenFlag.string(0), is("O_RDONLY"));
	}

}
