package ceri.common.util;

/**
 * Utility base class for keeping an object reference field. Useful to avoid resource warnings.
 */
public class Ref<T> {
	protected final T ref;

	protected Ref(T ref) {
		this.ref = ref;
	}
}
