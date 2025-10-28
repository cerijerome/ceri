package ceri.jna.clib.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;
import ceri.log.io.SelfHealing;
import ceri.log.io.SelfHealingConnector;

public class SelfHealingFd extends SelfHealingConnector<FileDescriptor>
	implements FileDescriptor.Fixable {
	private final Config config;

	public static class Config {
		private static final Functions.Predicate<Exception> DEFAULT_PREDICATE =
			Lambdas.register(CFileDescriptor::isBroken, "CFileDescriptor::isBroken");
		public final Excepts.Supplier<IOException, ? extends FileDescriptor> openFn;
		public final SelfHealing.Config selfHealing;

		public static Config of(Excepts.Supplier<IOException, FileDescriptor> openFn) {
			return builder(openFn).build();
		}

		public static class Builder {
			final Excepts.Supplier<IOException, ? extends FileDescriptor> openFn;
			final SelfHealing.Config.Builder selfHealing =
				SelfHealing.Config.builder().brokenPredicate(DEFAULT_PREDICATE);

			Builder(Excepts.Supplier<IOException, ? extends FileDescriptor> openFn) {
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

		public static Builder builder(String path, Mode mode, Open... flags) {
			return builder(path, mode, Arrays.asList(flags));
		}

		public static Builder builder(String path, Mode mode, Collection<Open> flags) {
			Objects.requireNonNull(path);
			Objects.requireNonNull(mode);
			Objects.requireNonNull(flags);
			return builder(new CFileDescriptor.Opener(path, mode, flags));
		}

		public static Builder
			builder(Excepts.Supplier<IOException, ? extends FileDescriptor> openFn) {
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
			return ToString.forClass(this, Lambdas.name(openFn), selfHealing);
		}
	}

	public static class Properties extends TypedProperties.Ref {
		private final FileDescriptorProperties fd;
		private final SelfHealing.Properties selfHealing;

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
			fd = new FileDescriptorProperties(ref);
			selfHealing = new SelfHealing.Properties(ref);
		}

		public Config config() {
			return Config.builder(fd.opener()).selfHealing(selfHealing.config()).build();
		}
	}

	public static SelfHealingFd of(Config config) {
		return new SelfHealingFd(config);
	}

	private SelfHealingFd(Config config) {
		super(config.selfHealing);
		this.config = config;
	}

	@Override
	public void accept(Excepts.IntConsumer<IOException> consumer) throws IOException {
		device.acceptValid(fd -> fd.accept(consumer));
	}

	@Override
	public <T> T apply(Excepts.IntFunction<IOException, T> function) throws IOException {
		return device.applyValid(fd -> fd.apply(function));
	}

	@Override
	public int flags() throws IOException {
		return device.applyValid(FileDescriptor::flags);
	}

	@Override
	public void flags(int flags) throws IOException {
		device.acceptValid(fd -> fd.flags(flags));
	}

	@Override
	protected FileDescriptor openConnector() throws IOException {
		return config.open();
	}
}
