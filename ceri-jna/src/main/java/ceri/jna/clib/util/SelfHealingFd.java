package ceri.jna.clib.util;

import static ceri.common.function.Namer.lambda;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ceri.common.data.FieldTranscoder;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.Namer;
import ceri.common.text.ToString;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;
import ceri.jna.clib.OpenFlag;
import ceri.log.io.SelfHealing;
import ceri.log.io.SelfHealingConnector;

public class SelfHealingFd extends SelfHealingConnector<FileDescriptor>
	implements FileDescriptor.Fixable {
	private final Config config;
	private final FieldTranscoder<IOException, OpenFlag> flags;

	public static class Config {
		private static final Predicate<Exception> DEFAULT_PREDICATE =
			Namer.predicate(CFileDescriptor::isBroken, "CFileDescriptor::isBroken");
		public final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
		public final SelfHealing.Config selfHealing;

		public static Config of(ExceptionSupplier<IOException, FileDescriptor> openFn) {
			return builder(openFn).build();
		}

		public static class Builder {
			final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
			final SelfHealing.Config.Builder selfHealing =
				SelfHealing.Config.builder().brokenPredicate(DEFAULT_PREDICATE);

			Builder(ExceptionSupplier<IOException, ? extends FileDescriptor> openFn) {
				this.openFn = openFn;
			}

			public Builder selfHealing(SelfHealing.Config selfHealing) {
				this.selfHealing.apply(selfHealing);
				return this;
			}

			public Builder selfHealing(Consumer<SelfHealing.Config.Builder> consumer) {
				consumer.accept(selfHealing);
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder(String path, Mode mode, OpenFlag... flags) {
			return builder(path, mode, Arrays.asList(flags));
		}

		public static Builder builder(String path, Mode mode, Collection<OpenFlag> flags) {
			Objects.requireNonNull(path);
			Objects.requireNonNull(mode);
			Objects.requireNonNull(flags);
			return builder(new CFileDescriptor.Opener(path, mode, flags));
		}

		public static Builder
			builder(ExceptionSupplier<IOException, ? extends FileDescriptor> openFn) {
			return new Builder(openFn);
		}

		Config(Builder builder) {
			openFn = builder.openFn;
			selfHealing = builder.selfHealing.build();
		}

		public FileDescriptor open() throws IOException {
			return openFn.get();
		}

		@Override
		public String toString() {
			return ToString.forClass(this, lambda(openFn), selfHealing);
		}
	}

	public static SelfHealingFd of(Config config) {
		return new SelfHealingFd(config);
	}

	private SelfHealingFd(Config config) {
		super(config.selfHealing);
		this.config = config;
		flags = FileDescriptor.flagField(this::flagValue, this::flagValue);
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
	public FieldTranscoder<IOException, OpenFlag> flags() {
		return flags;
	}

	@Override
	protected FileDescriptor openConnector() throws IOException {
		return config.open();
	}

	private int flagValue() throws IOException {
		return device.applyValid(fd -> fd.flags().field().getInt());
	}

	private void flagValue(int value) throws IOException {
		device.acceptValid(fd -> fd.flags().field().set(value));
	}
}
