package ceri.common.process;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import java.util.function.Function;
import org.junit.Test;
import ceri.common.text.ParseUtil;
import ceri.common.text.StringUtil;

public class OutputBehavior {

	@Test
	public void shouldParseOutput() {
		Function<String, int[]> parser =
			s -> StringUtil.commaSplit(s).stream().mapToInt(ParseUtil::parseInt).toArray();
		Output<int[]> output = Output.of("1, 2, 3", parser);
		assertEquals(output.out, "1, 2, 3");
		assertEquals(output.toString(), "1, 2, 3");
		assertArray(output.parse(), 1, 2, 3);
	}

	@Test
	public void shouldHandleNulls() {
		assertNull(Output.of(null, _ -> "test").parse());
		assertNull(Output.of("test", null).parse());
	}

}
