package ceri.common.function;

import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.test.Captor;

public class VarArgsConsumerBehavior {

	@Test
	public void shouldAcceptVarArgs() {
		var captor = Captor.<String[]>of();
		VarArgsConsumer<String> c = captor::accept;
		c.accept("a", "ab", "abc");
		captor.verify(new String[] { "a", "ab", "abc" });
	}

	@Test
	public void shouldCombineConsumers() {
		var captor = Captor.<String[]>of();
		VarArgsConsumer<String> c1 = captor::accept;
		VarArgsConsumer<String> c2 = (args) -> captor.accept(ArrayUtil.reverse(args.clone()));
		VarArgsConsumer<String> c = c1.andThen(c2);
		c.accept("a", "b", "c");
		captor.verify(new String[] { "a", "b", "c" }, new String[] { "c", "b", "a" });
	}

}
