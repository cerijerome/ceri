package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.byteUnaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionByteUnaryOperatorBehavior {

	@Test
	public void shouldConvertToUnaryOperator() {
		ByteUnaryOperator f = byteUnaryOperator().asByteUnaryOperator();
		assertEquals(f.applyAsByte((byte) 2), (byte) 2);
		assertRte(() -> f.applyAsByte((byte) 1));
		assertRte(() -> f.applyAsByte((byte) 0));
	}

	@Test
	public void shouldConvertFromUnaryOperator() {
		ExceptionByteUnaryOperator<RuntimeException> f =
			ExceptionByteUnaryOperator.of(Std.byteUnaryOperator());
		assertEquals(f.applyAsByte((byte) 1), (byte) 1);
		assertRte(() -> f.applyAsByte((byte) 0));
	}

	@Test
	public void shouldComposeOperators() throws IOException {
		ExceptionByteUnaryOperator<IOException> f =
			byteUnaryOperator().compose(i -> (byte) (i + 1));
		assertEquals(f.applyAsByte((byte) 1), (byte) 2);
		assertIoe(() -> f.applyAsByte((byte) 0));
		assertRte(() -> f.applyAsByte((byte) -1));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		ExceptionByteUnaryOperator<IOException> f =
			byteUnaryOperator().andThen(i -> (byte) (i + 1));
		assertEquals(f.applyAsByte((byte) 2), (byte) 3);
		assertIoe(() -> f.applyAsByte((byte) 1));
		assertRte(() -> f.applyAsByte((byte) 0));
	}
}
