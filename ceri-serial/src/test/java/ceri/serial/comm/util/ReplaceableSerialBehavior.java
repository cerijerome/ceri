package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.test.TestSerial;

public class ReplaceableSerialBehavior {

	@Test
	public void shouldAccessNullDelegate() throws IOException {
		try (var r = ReplaceableSerial.of()) {
			assertEquals(r.port(), null);
			r.inBufferSize(111); // not applied
			assertEquals(r.inBufferSize(), 0);
			r.outBufferSize(222); // not applied
			assertEquals(r.outBufferSize(), 0);
			assertThrown(() -> r.params(SerialParams.DEFAULT));
			assertEquals(r.params(), SerialParams.NULL);
			assertThrown(() -> r.flowControl(FlowControl.NONE));
			assertEquals(r.flowControl(), FlowControl.NONE);
			assertThrown(() -> r.brk(true));
			assertThrown(() -> r.rts(true));
			assertThrown(r::rts);
			assertThrown(() -> r.dtr(true));
			assertThrown(r::dtr);
			assertThrown(r::cd);
			assertThrown(r::cts);
			assertThrown(r::dsr);
			assertThrown(r::ri);
		}
	}

	@Test
	public void shouldAccessDelegate() throws IOException {
		try (var serial = TestSerial.of(); var r = ReplaceableSerial.of()) {
			serial.open();
			r.set(serial);
			assertEquals(r.port(), "test");
			r.inBufferSize(111);
			assertEquals(r.inBufferSize(), 111);
			r.outBufferSize(222);
			assertEquals(r.outBufferSize(), 222);
			r.params(SerialParams.DEFAULT);
			assertEquals(r.params(), SerialParams.DEFAULT);
			r.flowControl(FlowControl.NONE);
			assertEquals(r.flowControl(), FlowControl.NONE);
			r.brk(true);
			r.rts(true);
			assertEquals(r.rts(), true);
			r.dtr(true);
			assertEquals(r.dtr(), true);
			assertEquals(r.cd(), false);
			assertEquals(r.cts(), false);
			assertEquals(r.dsr(), false);
			assertEquals(r.ri(), false);
		}
	}

}
