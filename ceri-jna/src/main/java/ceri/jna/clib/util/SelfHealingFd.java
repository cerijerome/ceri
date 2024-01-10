package ceri.jna.clib.util;

import java.io.IOException;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.jna.clib.FileDescriptor;
import ceri.log.io.SelfHealingConnector;

public class SelfHealingFd extends SelfHealingConnector<FileDescriptor>
	implements FileDescriptor.Fixable {
	private final SelfHealingFdConfig config;

	public static SelfHealingFd of(SelfHealingFdConfig config) {
		return new SelfHealingFd(config);
	}
	
	private SelfHealingFd(SelfHealingFdConfig config) {
		super(config.selfHealing);
		this.config = config;
	}

	@Override
	public void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {
		device.acceptValid(fd -> fd.accept(consumer));
	}

	@Override
	public <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
		return device.applyValid(fd -> fd.apply(function));
	}

	@Override
	protected FileDescriptor openConnector() throws IOException {
		return config.open();
	}
}
