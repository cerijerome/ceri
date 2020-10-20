package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
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
		assertEquals(getter.get(), 0xff);
		assertThrown(() -> getter.set(0));
	}

	@Test
	public void shouldAccessWithGetAndSet() {
		int[] array = { 0 };
		var accessor = Accessor.of(() -> array[0], i -> array[0] = i);
		accessor.set(0xff);
		assertEquals(accessor.get(), 0xff);
	}

	@Test
	public void shouldProvideTypeAccess() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], (a, i) -> a[0] = i);
		int[] array = { 1 };
		assertEquals(typed.get(array), 1);
		typed.set(array, -1);
		assertEquals(array[0], -1);
	}

	@Test
	public void shouldProvideAccessorFromTypedAccessor() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], (a, i) -> a[0] = i);
		int[] array = { 1 };
		var accessor = typed.from(array);
		assertEquals(accessor.get(), 1);
		accessor.set(-1);
		assertEquals(array[0], -1);
	}

	@Test
	public void shouldProvideTypedGetter() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(a -> a[0], null);
		int[] array = { 1 };
		assertEquals(typed.get(array), 1);
		assertThrown(() -> typed.set(array, -1));
	}

	@Test
	public void shouldProvideTypedSetter() {
		Accessor.Typed<int[], Integer> typed = Accessor.typed(null, (a, i) -> a[0] = i);
		int[] array = { 1 };
		assertThrown(() -> typed.get(array));
		typed.set(array, -1);
		assertEquals(array[0], -1);
	}

}
