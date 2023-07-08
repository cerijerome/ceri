package ceri.jna.clib.util;

import java.io.IOException;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.jna.clib.FileDescriptor;
import ceri.log.io.SelfHealingConnector;

public class SelfHealingFileDescriptor extends SelfHealingConnector<FileDescriptor>
	implements FileDescriptor {
	private final SelfHealingFileDescriptorConfig config;

	private SelfHealingFileDescriptor(SelfHealingFileDescriptorConfig config) {
		super(config.selfHealing);
		this.config = config;
	}

	@Override
	public void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {
		acceptValidConnector(fd -> fd.accept(consumer));
	}

	@Override
	public <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
		return applyValidConnector(fd -> fd.apply(function));
	}

	@Override
	protected FileDescriptor openConnector() throws IOException {
		return config.open();
	}
}
