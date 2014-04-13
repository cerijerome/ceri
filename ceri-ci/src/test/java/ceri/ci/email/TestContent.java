package ceri.ci.email;

import java.io.IOException;
import ceri.common.io.IoUtil;

public enum TestContent {
	content0, content1, content2;
	
	private static final String FILE_SUFFIX = ".txt";
	public final String content;
	
	private TestContent() {
		try {
			content = IoUtil.getResourceString(getClass(), name() + FILE_SUFFIX);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
