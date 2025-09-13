package ceri.common.process;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.text.NumberParser;
import ceri.common.text.Regex;

public class OutputBehavior {

	@Test
	public void shouldParseOutput() {
		Functions.Function<String, int[]> parser =
			s -> Regex.Split.COMMA.stream(s).mapToInt(NumberParser::parseInt).toArray();
		Output<int[]> output = Output.of("1, 2, 3", parser);
		assertEquals(output.out, "1, 2, 3");
		assertEquals(output.toString(), "1, 2, 3");
		assertArray(output.parse(), 1, 2, 3);
	}

	@Test
	public void shouldHandleNulls() {
		assertEquals(Output.of(null, _ -> "test").parse(), null);
		assertEquals(Output.of("test", null).parse(), null);
	}
}
