package ceri.common.factory;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class FactoriesTest {
	static final Factory<Integer, String> testFactory = new Factory<Integer, String>() {
		@Override
		public Integer create(String from) {
			throw new UnsupportedOperationException();
		}
	};

	@Test
	public void testAssign() {
		assertNull(Factories.assign().create(null));
		assertThat(Factories.<Integer>assign().create(1), is(1));
		Object obj = new Object();
		assertThat(Factories.assign().create(obj), is(obj));
	}

	@Test
	public void testNul() {
		assertNull(Factories.nul(null).create(null));
		assertNull(Factories.nul(testFactory).create(null));
		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				testFactory.create("");
			}
		});
	}

	@Test
	public void testCreate() {
		assertException(FactoryException.class, new Runnable() {
			@Override
			public void run() {
				Factories.create(testFactory, "");
			}
		});
	}

	@Test
	public void testArray() {
		final Factory<Integer[], String[]> f =
			Factories.array(StringFactories.TO_INTEGER, Integer.class);
		assertNull(f.create(null));
		assertThat(f.create(new String[0]), is(new Integer[0]));
		assertThat(f.create(new String[] { "1", "2" }), is(new Integer[] { 1, 2 }));
		assertException(NumberFormatException.class, new Runnable() {
			@Override
			public void run() {
				f.create(new String[] { "" });
			}
		});
	}

	@Test
	public void testList() {
		final Factory<List<Double>, Iterable<String>> f = Factories.list(StringFactories.TO_DOUBLE);
		assertNull(f.create(null));
		assertThat(f.create(Collections.<String>emptyList()), is(Collections.<Double>emptyList()));
		assertThat(f.create(Arrays.asList("0.0", "1.0")), is(Arrays.asList(0.0, 1.0)));
		assertException(NumberFormatException.class, new Runnable() {
			@Override
			public void run() {
				f.create(Collections.singleton(""));
			}
		});
	}

	@Test
	public void testSet() {
		final Factory<List<Long>, Iterable<String>> f = Factories.list(StringFactories.TO_LONG);
		assertNull(f.create(null));
		assertThat(f.create(Collections.<String>emptyList()), is(Collections.<Long>emptyList()));
		assertThat(f.create(Arrays.asList("0", "1")), is(Arrays.asList(0L, 1L)));
		assertException(NumberFormatException.class, new Runnable() {
			@Override
			public void run() {
				f.create(Collections.singleton(""));
			}
		});
	}

}
