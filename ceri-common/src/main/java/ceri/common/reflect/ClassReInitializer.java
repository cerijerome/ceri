package ceri.common.reflect;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import ceri.common.collection.ImmutableUtil;

/**
 * Reloads and re-initializes classes; additional support classes are reloaded if accessed.
 */
public class ClassReInitializer {
	private final List<Class<?>> inits;
	private final List<Class<?>> reloads;

	/**
	 * Creates an instance for one re-init class, and any number of support classes.
	 */
	public static ClassReInitializer of(Class<?> cls, Class<?>... supports) {
		return of(cls, Arrays.asList(supports));
	}

	/**
	 * Creates an instance for one re-init class, and any number of support classes.
	 */
	public static ClassReInitializer of(Class<?> cls, Collection<Class<?>> supports) {
		return builder().init(cls).reload(supports).build();
	}

	public static class Builder {
		private final Set<Class<?>> inits = new LinkedHashSet<>();
		private final Set<Class<?>> reloads = new LinkedHashSet<>();

		private Builder() {}

		public Builder init(Class<?>... inits) {
			return init(Arrays.asList(inits));
		}

		public Builder init(Collection<Class<?>> inits) {
			this.inits.addAll(inits);
			return reload(inits);
		}

		public Builder reload(Class<?>... supports) {
			return reload(Arrays.asList(supports));
		}

		public Builder reload(Collection<Class<?>> supports) {
			this.reloads.addAll(supports);
			return this;
		}

		public ClassReInitializer build() {
			return new ClassReInitializer(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private ClassReInitializer(Builder builder) {
		inits = ImmutableUtil.copyAsList(builder.inits);
		reloads = ImmutableUtil.copyAsList(builder.reloads);
	}

	/**
	 * Reloads and re-initializes the classes.
	 */
	public void reinit() {
		var reloader = ClassReloader.ofNested(reloads);
		for (var cls : inits)
			reloader.forName(cls, true);
	}
}