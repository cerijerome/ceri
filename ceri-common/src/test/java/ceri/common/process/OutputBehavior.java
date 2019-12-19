package ceri.common.process;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.function.Function;
import org.junit.Test;
import ceri.common.text.StringUtil;
import ceri.common.util.PrimitiveUtil;

public class OutputBehavior {

	@Test
	public void shouldParseOutput() {
		Function<String, int[]> parser =
			s -> StringUtil.commaSplit(s).stream().mapToInt(PrimitiveUtil::intValue).toArray();
		Output<int[]> output = Output.of("1, 2, 3", parser);
		assertThat(output.out, is("1, 2, 3"));
		assertThat(output.toString(), is("1, 2, 3"));
		assertArray(output.parse(), 1, 2, 3);
	}

	@Test
	public void shouldHandleNulls() {
		assertNull(Output.of(null, s -> "test").parse());
		assertNull(Output.of("test", null).parse());
	}

}
