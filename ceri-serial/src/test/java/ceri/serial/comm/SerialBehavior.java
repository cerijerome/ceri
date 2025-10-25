package ceri.serial.comm;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFind;
import java.io.IOException;
import org.junit.Test;

public class SerialBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		assertEquals(Serial.NULL.port(), "null");
		Serial.NULL.inBufferSize(111);
		assertEquals(Serial.NULL.inBufferSize(), 0);
		Serial.NULL.outBufferSize(111);
		assertEquals(Serial.NULL.outBufferSize(), 0);
		Serial.NULL.params(SerialParams.DEFAULT);
		assertEquals(Serial.NULL.params(), SerialParams.NULL);
		Serial.NULL.flowControls(FlowControl.rtsCtsIn);
		assertEquals(Serial.NULL.flowControl(), FlowControl.NONE);
		Serial.NULL.brk(true);
		Serial.NULL.rts(true);
		Serial.NULL.dtr(true);
		assertFalse(Serial.NULL.rts());
		assertFalse(Serial.NULL.dtr());
		assertFalse(Serial.NULL.cd());
		assertFalse(Serial.NULL.cts());
		assertFalse(Serial.NULL.dsr());
		assertFalse(Serial.NULL.ri());
		assertFind(Serial.NULL, ".*NULL$");
	}

}
