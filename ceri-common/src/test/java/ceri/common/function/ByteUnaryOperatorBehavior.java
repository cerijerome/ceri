package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ByteUnaryOperatorBehavior {

	@Test
	public void shouldCompose() {
		ByteUnaryOperator operator0 = b -> (byte) -b;
		ByteUnaryOperator operator1 = b -> (byte) (b + 1);
		var operator = operator0.compose(operator1);
		assertEquals(operator.applyAsByte((byte) 0x7f), (byte) 0x80);
	}

	@Test
	public void shouldCombineWithAndThen() {
		ByteUnaryOperator operator0 = b -> (byte) -b;
		ByteUnaryOperator operator1 = b -> (byte) (b + 1);
		var operator = operator0.andThen(operator1);
		assertEquals(operator.applyAsByte((byte) 0x7f), (byte) 0x82);
	}

	@Test
	public void shouldProvideIdentity() {
		assertEquals(ByteUnaryOperator.identity().applyAsByte(Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(ByteUnaryOperator.identity().applyAsByte(Byte.MAX_VALUE), Byte.MAX_VALUE);
	}

}
