package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var params = SerialParams.of(1200, DataBits._7, StopBits._1, Parity.odd);
		SerialConfig t = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(222).build();
		SerialConfig eq0 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(222).build();
		SerialConfig ne0 = SerialConfig.DEFAULT;
		SerialConfig ne1 = SerialConfig.of(1200);
		SerialConfig ne2 =
			SerialConfig.of(SerialParams.of(1200, DataBits._7, StopBits._1, Parity.odd));
		SerialConfig ne3 = SerialConfig.builder().params(params).flowControl(FlowControl.NONE)
			.inBufferSize(111).outBufferSize(222).build();
		SerialConfig ne4 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(112).outBufferSize(222).build();
		SerialConfig ne5 = SerialConfig.builder().params(params).flowControl(FlowControl.rtsCtsIn)
			.inBufferSize(111).outBufferSize(223).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldReplaceSerialParams() {
		assertEquals(SerialConfig.DEFAULT.replace(null).params, SerialParams.DEFAULT);
		assertEquals(SerialConfig.DEFAULT.replace(SerialParams.DEFAULT).params,
			SerialParams.DEFAULT);
		assertEquals(SerialConfig.DEFAULT.replace(SerialParams.NULL).params, SerialParams.NULL);
	}

}
