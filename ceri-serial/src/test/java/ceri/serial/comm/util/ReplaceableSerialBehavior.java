package ceri.serial.comm.util;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.test.TestSerial;

public class ReplaceableSerialBehavior {

	@Test
	public void shouldAccessNullDelegate() throws IOException {
		try (var r = ReplaceableSerial.of()) {
			Assert.equal(r.port(), null);
			r.inBufferSize(111); // not applied
			Assert.equal(r.inBufferSize(), 0);
			r.outBufferSize(222); // not applied
			Assert.equal(r.outBufferSize(), 0);
			Assert.thrown(() -> r.params(SerialParams.DEFAULT));
			Assert.equal(r.params(), SerialParams.NULL);
			Assert.thrown(() -> r.flowControl(FlowControl.NONE));
			Assert.equal(r.flowControl(), FlowControl.NONE);
			Assert.thrown(() -> r.brk(true));
			Assert.thrown(() -> r.rts(true));
			Assert.thrown(r::rts);
			Assert.thrown(() -> r.dtr(true));
			Assert.thrown(r::dtr);
			Assert.thrown(r::cd);
			Assert.thrown(r::cts);
			Assert.thrown(r::dsr);
			Assert.thrown(r::ri);
		}
	}

	@Test
	public void shouldAccessDelegate() throws IOException {
		try (var serial = TestSerial.of(); var r = ReplaceableSerial.of()) {
			serial.open();
			r.set(serial);
			Assert.equal(r.port(), "test");
			r.inBufferSize(111);
			Assert.equal(r.inBufferSize(), 111);
			r.outBufferSize(222);
			Assert.equal(r.outBufferSize(), 222);
			r.params(SerialParams.DEFAULT);
			Assert.equal(r.params(), SerialParams.DEFAULT);
			r.flowControl(FlowControl.NONE);
			Assert.equal(r.flowControl(), FlowControl.NONE);
			r.brk(true);
			r.rts(true);
			Assert.equal(r.rts(), true);
			r.dtr(true);
			Assert.equal(r.dtr(), true);
			Assert.equal(r.cd(), false);
			Assert.equal(r.cts(), false);
			Assert.equal(r.dsr(), false);
			Assert.equal(r.ri(), false);
		}
	}
}
