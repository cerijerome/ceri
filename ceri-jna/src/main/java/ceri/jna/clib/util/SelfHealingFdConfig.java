package ceri.jna.clib.util;

import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.ToString;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;
import ceri.jna.clib.OpenFlag;

public class SelfHealingFdConfig {
	static final Predicate<Exception> DEFAULT_PREDICATE =
		named(CFileDescriptor::isBroken, "CFileDescriptor::isBroken");
	public final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
	public final int recoveryDelayMs;
	public final int fixRetryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static SelfHealingFdConfig of(ExceptionSupplier<IOException, FileDescriptor> openFn) {
		return builder(openFn).build();
	}

	public static class Builder {
		final ExceptionSupplier<IOException, ? extends FileDescriptor> openFn;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder(ExceptionSupplier<IOException, ? extends FileDescriptor> openFn) {
			this.openFn = openFn;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public SelfHealingFdConfig build() {
			return new SelfHealingFdConfig(this);
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

	SelfHealingFdConfig(Builder builder) {
		openFn = builder.openFn;
		recoveryDelayMs = builder.recoveryDelayMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public FileDescriptor open() throws IOException {
		return openFn.get();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, ReflectUtil.toStringOrHash(openFn), recoveryDelayMs,
			fixRetryDelayMs, ReflectUtil.toStringOrHash(brokenPredicate));
	}

}
