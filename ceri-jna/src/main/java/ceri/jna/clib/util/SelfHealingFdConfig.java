package ceri.jna.clib.util;

import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToString;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;
import ceri.jna.clib.OpenFlag;
import ceri.log.io.SelfHealingConnectorConfig;

public class SelfHealingFileDescriptorConfig {
	static final Predicate<Exception> DEFAULT_PREDICATE =
		named(CFileDescriptor::isBroken, "CFileDescriptor::isBroken");
	public final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
	public final SelfHealingConnectorConfig selfHealing;

	public static SelfHealingFileDescriptorConfig
		of(ExceptionSupplier<IOException, FileDescriptor> openFn) {
		return builder(openFn).build();
	}

	public static class Builder {
		final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
		SelfHealingConnectorConfig.Builder selfHealing =
			SelfHealingConnectorConfig.builder().brokenPredicate(DEFAULT_PREDICATE);

		Builder(ExceptionSupplier<IOException, ? extends FileDescriptor> openFn) {
			this.openFn = openFn;
		}

		public Builder selfHealing(SelfHealingConnectorConfig selfHealing) {
			this.selfHealing.apply(selfHealing);
			return this;
		}

		public Builder selfHealing(Consumer<SelfHealingConnectorConfig.Builder> consumer) {
			consumer.accept(selfHealing);
			return this;
		}

		public SelfHealingFileDescriptorConfig build() {
			return new SelfHealingFileDescriptorConfig(this);
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

	public static Builder builder(ExceptionSupplier<IOException, ? extends FileDescriptor> openFn) {
		return new Builder(openFn);
	}

	SelfHealingFileDescriptorConfig(Builder builder) {
		openFn = builder.openFn;
		selfHealing = builder.selfHealing.build();
	}

	public FileDescriptor open() throws IOException {
		return openFn.get();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, FunctionUtil.lambdaName(openFn), selfHealing);
	}
}
