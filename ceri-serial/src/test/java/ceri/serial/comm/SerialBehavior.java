package ceri.serial.comm;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class SerialBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Assert.equal(Serial.NULL.port(), "null");
		Serial.NULL.inBufferSize(111);
		Assert.equal(Serial.NULL.inBufferSize(), 0);
		Serial.NULL.outBufferSize(111);
		Assert.equal(Serial.NULL.outBufferSize(), 0);
		Serial.NULL.params(SerialParams.DEFAULT);
		Assert.equal(Serial.NULL.params(), SerialParams.NULL);
		Serial.NULL.flowControls(FlowControl.rtsCtsIn);
		Assert.equal(Serial.NULL.flowControl(), FlowControl.NONE);
		Serial.NULL.brk(true);
		Serial.NULL.rts(true);
		Serial.NULL.dtr(true);
		Assert.no(Serial.NULL.rts());
		Assert.no(Serial.NULL.dtr());
		Assert.no(Serial.NULL.cd());
		Assert.no(Serial.NULL.cts());
		Assert.no(Serial.NULL.dsr());
		Assert.no(Serial.NULL.ri());
		Assert.find(Serial.NULL, ".*NULL$");
	}

}
