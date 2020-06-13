package ceri.serial.clib.util;

import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.util.function.Predicate;
import ceri.common.function.ExceptionSupplier;
import ceri.common.text.ToStringHelper;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.Mode;
import ceri.serial.clib.OpenFlag;

public class SelfHealingFdConfig {
	static final Predicate<Exception> DEFAULT_PREDICATE =
		named(CFileDescriptor::isBroken, "CFileDescriptor::isBroken");
	public final ExceptionSupplier<IOException, CFileDescriptor> openFn;
	public final int recoveryDelayMs;
	public final int fixRetryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static SelfHealingFdConfig of(String path, OpenFlag... flags) {
		return of(path, null, flags);
	}
	
	public static SelfHealingFdConfig of(String path, Mode mode, OpenFlag... flags) {
		return builder(() -> CFileDescriptor.open(path, mode, flags)).build();
	}

	public static class Builder {
		final ExceptionSupplier<IOException, CFileDescriptor> openFn;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder(ExceptionSupplier<IOException, CFileDescriptor> openFn) {
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

	public static Builder builder(ExceptionSupplier<IOException, CFileDescriptor> openFn) {
		return new Builder(openFn);
	}

	SelfHealingFdConfig(Builder builder) {
		openFn = builder.openFn;
		recoveryDelayMs = builder.recoveryDelayMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, openFn, recoveryDelayMs, fixRetryDelayMs, brokenPredicate)
			.toString();
	}

}
