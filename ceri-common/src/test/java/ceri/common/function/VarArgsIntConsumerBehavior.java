package ceri.common.function;

import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.test.Captor;

public class VarArgsIntConsumerBehavior {

	@Test
	public void shouldAcceptVarArgs() {
		var captor = Captor.<int[]>of();
		VarArgsIntConsumer c = captor::accept;
		c.accept(1, -1, 0);
		captor.verify(new int[] { 1, -1, 0 });
	}

	@Test
	public void shouldCombineConsumers() {
		var captor = Captor.<int[]>of();
		VarArgsIntConsumer c1 = captor::accept;
		VarArgsIntConsumer c2 = (args) -> captor.accept(ArrayUtil.reverseInts(args.clone()));
		VarArgsIntConsumer c = c1.andThen(c2);
		c.accept(1, -1, 0);
		captor.verify(new int[] { 1, -1, 0 }, new int[] { 0, -1, 1 });
	}

}
