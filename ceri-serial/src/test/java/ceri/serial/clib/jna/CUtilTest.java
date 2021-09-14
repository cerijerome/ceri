package ceri.serial.clib.jna;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static ceri.serial.jna.JnaTestUtil.*;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.test.CallSync;
import ceri.serial.jna.JnaUtil;

public class CUtilTest {

	@Test
	public void testValidFd() {
		assertEquals(CLibUtil.validFd(-1), false);
		assertEquals(CLibUtil.validFd(0), true);
		assertEquals(CLibUtil.validFd(1), true);
	}
	
}
