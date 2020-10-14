package ceri.process.util;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.mockito.ArgumentCaptor;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

public class ProcessTestUtil {

	private ProcessTestUtil() {}

	public static Processor mockProcessor(String output) throws IOException {
		Processor p = mock(Processor.class);
		when(p.exec(any(Parameters.class))).thenReturn(output);
		return p;
	}

	public static void assertParameters(Processor mockProcessor, String... params)
		throws IOException {
		ArgumentCaptor<Parameters> captor = ArgumentCaptor.forClass(Parameters.class);
		verify(mockProcessor, times(1)).exec(captor.capture());
		assertThat(captor.getValue(), is(Parameters.ofAll(params)));
	}

}
