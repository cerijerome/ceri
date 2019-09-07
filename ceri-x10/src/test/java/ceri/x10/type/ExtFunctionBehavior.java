package ceri.x10.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ExtFunctionBehavior {

	@Test
	public void shouldObeyEqualsContract() {
		ExtFunction ext1 = new ExtFunction(House.I, (byte) -1, (byte) 1);
		ExtFunction ext2 = new ExtFunction(House.I, (byte) -1, (byte) 1);
		ExtFunction ext3 = new ExtFunction(House.J, (byte) -1, (byte) 1);
		ExtFunction ext4 = new ExtFunction(House.I, (byte) 1, (byte) 1);
		ExtFunction ext5 = new ExtFunction(House.I, (byte) -1, (byte) -1);
		assertThat(ext1, is(ext1));
		assertThat(ext1, is(ext2));
		assertNotEquals(null, ext1);
		assertNotEquals(ext1, new Object());
		assertThat(ext1, not(ext3));
		assertThat(ext1, not(ext4));
		assertThat(ext1, not(ext5));
		assertThat(ext1.toString(), is(ext2.toString()));
	}

}
