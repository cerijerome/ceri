package ceri.common.process;

import static ceri.common.test.TestUtil.inputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import ceri.common.io.IoStreamUtil;

public class ProcessTestUtil {

	private ProcessTestUtil() {}

	@SuppressWarnings("resource")
	public static Process mockProcess(String in, String err, int exitValue, boolean waitFor)
		throws InterruptedException {
		Process process = mock(Process.class);
		when(process.getInputStream()).thenReturn(inputStream(in));
		when(process.getErrorStream()).thenReturn(inputStream(err));
		when(process.getOutputStream()).thenReturn(IoStreamUtil.nullOut());
		when(process.exitValue()).thenReturn(exitValue);
		when(process.waitFor()).thenReturn(exitValue);
		when(process.waitFor(anyLong(), any())).thenReturn(waitFor);
		return process;
	}

	public static ProcessCommand command(Process process, String... commands) {
		List<String> list = Arrays.asList(commands);
		return ProcessCommand.of(() -> process, () -> list);
	}

}
