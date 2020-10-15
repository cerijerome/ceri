package ceri.common.function;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class AccessorBehavior {

	@Test
	public void shouldOnlyGetWithGetter() {
		int[] array = { 0 };
		Accessor<Integer> setter = Accessor.setter(i -> array[0] = i);
		setter.set(0xff);
		assertThrown(() -> setter.get());
	}

	@Test
	public void shouldOnlySetWithSetter() {
		int[] array = { 0xff };
		var getter = Accessor.getter(() -> array[0]);
		assertThat(getter.get(), is(0xff));
		assertThrown(() -> getter.set(0));
	}

	@Test
	public void shouldAccessWithGetAndSet() {
		int[] array = { 0 };
		var accessor = Accessor.of(() -> array[0], i -> array[0] = i);
		accessor.set(0xff);
		assertThat(accessor.get(), is(0xff));
	}

	@Test
	public void shouldProvideTypeAccess() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], (a, i) -> a[0] = i);
		int[] array = { 1 };
		assertThat(typed.get(array), is(1));
		typed.set(array, -1);
		assertThat(array[0], is(-1));
	}

	@Test
	public void shouldProvideAccessorFromTypedAccessor() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], (a, i) -> a[0] = i);
		int[] array = { 1 };
		var accessor = typed.from(array);
		assertThat(accessor.get(), is(1));
		accessor.set(-1);
		assertThat(array[0], is(-1));
	}

	@Test
	public void shouldProvideTypedGetter() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], null);
		int[] array = { 1 };
		assertThat(typed.get(array), is(1));
		assertThrown(() -> typed.set(array, -1));
	}

	@Test
	public void shouldProvideTypedSetter() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(null, (a, i) -> a[0] = i);
		int[] array = { 1 };
		assertThrown(() -> typed.get(array));
		typed.set(array, -1);
		assertThat(array[0], is(-1));
	}

}
