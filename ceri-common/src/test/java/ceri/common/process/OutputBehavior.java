package ceri.common.process;

import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.text.Parse;
import ceri.common.text.Regex;

public class OutputBehavior {

	@Test
	public void shouldParseOutput() {
		Functions.Function<String, int[]> parser =
			s -> Regex.Split.COMMA.stream(s).mapToInt(Parse::parseInt).toArray();
		Output<int[]> output = Output.of("1, 2, 3", parser);
		Assert.equal(output.out, "1, 2, 3");
		Assert.equal(output.toString(), "1, 2, 3");
		Assert.array(output.parse(), 1, 2, 3);
	}

	@Test
	public void shouldHandleNulls() {
		Assert.equal(Output.of(null, _ -> "test").parse(), null);
		Assert.equal(Output.of("test", null).parse(), null);
	}
}
