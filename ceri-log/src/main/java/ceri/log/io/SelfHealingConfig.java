package ceri.log.io;

import java.util.function.Predicate;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToString;

public class SelfHealingConfig {
	public static final SelfHealingConfig DEFAULT = new Builder().build();
	public static final Predicate<Exception> NULL_PREDICATE = e -> false; 
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static class Builder {
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = NULL_PREDICATE;

		Builder() {}

		public Builder apply(SelfHealingConfig config) {
			if (config.hasBrokenPredicate()) brokenPredicate(config.brokenPredicate);
			return fixRetryDelayMs(config.fixRetryDelayMs).recoveryDelayMs(config.recoveryDelayMs);
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public SelfHealingConfig build() {
			return new SelfHealingConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	
	SelfHealingConfig(Builder builder) {
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public boolean broken(Exception e) {
		return brokenPredicate.test(e);
	}

	public boolean hasBrokenPredicate() {
		return brokenPredicate != NULL_PREDICATE;
	}
	
	@Override
	public String toString() {
		return ToString.forClass(this, fixRetryDelayMs, recoveryDelayMs,
			FunctionUtil.lambdaName(brokenPredicate));
	}
}