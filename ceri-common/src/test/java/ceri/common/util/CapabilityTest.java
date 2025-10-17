package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class CapabilityTest {
	private static final Capability.IsEmpty IS_EMPTY_TRUE = () -> true;
	private static final Capability.IsEmpty IS_EMPTY_FALSE = () -> false;
	private static final Capability.Enabled ENABLED_TRUE = () -> true;
	private static final Capability.Enabled ENABLED_FALSE = () -> false;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Capability.class);
	}

	@Test
	public void testIntIdDefault() {
		var id = new Capability.IntId() {};
		assertEquals(Capability.IntId.id(null, null), 0);
		assertEquals(Capability.IntId.id(null, 100), 100);
		assertEquals(Capability.IntId.id(id, null), id.id());
		assertEquals(Capability.IntId.id(id, 100), 100);
	}

	@Test
	public void testIntIdCompare() {
		assertEquals(id(0).compareTo(id(-1)), 1);
		assertEquals(id(0).compareTo(id(0)), 0);
		assertEquals(id(0).compareTo(id(1)), -1);
	}

	@Test
	public void testIntIdPredicate() {
		var predicate = Capability.IntId.by(i -> i > 0);
		assertEquals(predicate.test(null), false);
		assertEquals(predicate.test(id(0)), false);
		assertEquals(predicate.test(id(1)), true);
	}

	@Test
	public void testLongIdDefault() {
		var id = new Capability.LongId() {};
		assertEquals(Capability.LongId.id(null, null), 0L);
		assertEquals(Capability.LongId.id(null, 100L), 100L);
		assertEquals(Capability.LongId.id(id, null), id.id());
		assertEquals(Capability.LongId.id(id, 100L), 100L);
	}

	@Test
	public void testLongIdCompare() {
		assertEquals(id(0L).compareTo(id(-1L)), 1);
		assertEquals(id(0L).compareTo(id(0L)), 0);
		assertEquals(id(0L).compareTo(id(1L)), -1);
	}

	@Test
	public void testLongIdPredicate() {
		var predicate = Capability.LongId.by(l -> l > 0L);
		assertEquals(predicate.test(null), false);
		assertEquals(predicate.test(id(0L)), false);
		assertEquals(predicate.test(id(1L)), true);
	}

	@Test
	public void testNameDefault() {
		var named = new Capability.Name() {};
		assertFind(named.name(), "^%s\\.\\d+@", getClass().getSimpleName());
	}

	@Test
	public void testName() {
		assertEquals(Capability.Name.name("test", "abc"), "abc");
		assertFind(Capability.Name.name("test", null), "String@");
	}

	@Test
	public void testNameOf() {
		assertEquals(Capability.Name.nameOf(null), "null");
		assertEquals(Capability.Name.nameOf(name(null)), null);
		assertEquals(Capability.Name.nameOf(name("test")), "test");
	}

	@Test
	public void testNamePredicate() {
		var predicate = Capability.Name.by(s -> s.length() > 1);
		assertEquals(predicate.test(name("")), false);
		assertEquals(predicate.test(name("a")), false);
		assertEquals(predicate.test(name("test")), true);
		assertEquals(predicate.test(new Capability.Name() {}), true);
	}

	@Test
	public void testInit() {
		int[] i = { 0 };
		Capability.Init.init(() -> i[0]++, null, () -> i[0] += 5);
		assertEquals(i[0], 6);
	}

	@Test
	public void testIsEmptyPredicate() {
		assertEquals(Capability.IsEmpty.NOT_BY.test(null), false);
		assertEquals(Capability.IsEmpty.NOT_BY.test(IS_EMPTY_FALSE), false);
		assertEquals(Capability.IsEmpty.NOT_BY.test(IS_EMPTY_TRUE), true);
	}

	@Test
	public void testEnabledPredicate() {
		assertEquals(Capability.Enabled.BY.test(null), false);
		assertEquals(Capability.Enabled.BY.test(ENABLED_FALSE), false);
		assertEquals(Capability.Enabled.BY.test(ENABLED_TRUE), true);
	}

	@Test
	public void testEnabledComparator() {
		assertEquals(Capability.Enabled.COMPARATOR.compare(null, null), 0);
		assertEquals(Capability.Enabled.COMPARATOR.compare(null, ENABLED_FALSE), 0);
		assertEquals(Capability.Enabled.COMPARATOR.compare(null, ENABLED_TRUE), -1);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, null), 0);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, ENABLED_FALSE), 0);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, ENABLED_TRUE), -1);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, null), 1);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, ENABLED_FALSE), 1);
		assertEquals(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, ENABLED_TRUE), 0);
	}

	@Test
	public void testEnabledConditional() {
		assertEquals(Capability.Enabled.conditional(null, 1, 2), 2);
		assertEquals(Capability.Enabled.conditional(() -> false, 1, 2), 2);
		assertEquals(Capability.Enabled.conditional(() -> true, 1, 2), 1);
	}

	private static Capability.IntId id(int id) {
		return new Capability.IntId() {
			@Override
			public int id() {
				return id;
			}
		};
	}

	private static Capability.LongId id(long id) {
		return new Capability.LongId() {
			@Override
			public long id() {
				return id;
			}
		};
	}

	private static Capability.Name name(String name) {
		return new Capability.Name() {
			@Override
			public String name() {
				return name;
			}
		};
	}
}
