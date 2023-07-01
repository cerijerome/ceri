package ceri.log.io;

import java.util.function.Predicate;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToString;

public class SelfHealingConnectorConfig {
	public static final SelfHealingConnectorConfig DEFAULT = new Builder().build();
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static class Builder {
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = e -> false;

		Builder() {}

		public Builder apply(SelfHealingConnectorConfig config) {
			return fixRetryDelayMs(config.fixRetryDelayMs).recoveryDelayMs(config.recoveryDelayMs)
				.brokenPredicate(config.brokenPredicate);
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

		public SelfHealingConnectorConfig build() {
			return new SelfHealingConnectorConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	
	SelfHealingConnectorConfig(Builder builder) {
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public boolean broken(Exception e) {
		return brokenPredicate.test(e);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, fixRetryDelayMs, recoveryDelayMs,
			FunctionUtil.lambdaName(brokenPredicate));
	}
}