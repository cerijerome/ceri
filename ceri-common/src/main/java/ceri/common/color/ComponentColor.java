package ceri.common.color;

public interface ComponentColor<T extends ComponentColor<T>> {

	boolean hasAlpha();

	void verify();

	T normalize();

	T limit();

}
