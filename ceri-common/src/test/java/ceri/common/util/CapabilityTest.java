package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;

public class CapabilityTest {
	private static final Capability.IsEmpty IS_EMPTY_TRUE = () -> true;
	private static final Capability.IsEmpty IS_EMPTY_FALSE = () -> false;
	private static final Capability.Enabled ENABLED_TRUE = () -> true;
	private static final Capability.Enabled ENABLED_FALSE = () -> false;

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Capability.class);
	}

	@Test
	public void testIntIdDefault() {
		var id = new Capability.IntId() {};
		Assert.equal(Capability.IntId.id(null, null), 0);
		Assert.equal(Capability.IntId.id(null, 100), 100);
		Assert.equal(Capability.IntId.id(id, null), id.id());
		Assert.equal(Capability.IntId.id(id, 100), 100);
	}

	@Test
	public void testIntIdCompare() {
		Assert.equal(id(0).compareTo(id(-1)), 1);
		Assert.equal(id(0).compareTo(id(0)), 0);
		Assert.equal(id(0).compareTo(id(1)), -1);
	}

	@Test
	public void testIntIdPredicate() {
		var predicate = Capability.IntId.filter(i -> i > 0);
		Assert.equal(predicate.test(null), false);
		Assert.equal(predicate.test(id(0)), false);
		Assert.equal(predicate.test(id(1)), true);
	}

	@Test
	public void testLongIdDefault() {
		var id = new Capability.LongId() {};
		Assert.equal(Capability.LongId.id(null, null), 0L);
		Assert.equal(Capability.LongId.id(null, 100L), 100L);
		Assert.equal(Capability.LongId.id(id, null), id.id());
		Assert.equal(Capability.LongId.id(id, 100L), 100L);
	}

	@Test
	public void testLongIdCompare() {
		Assert.equal(id(0L).compareTo(id(-1L)), 1);
		Assert.equal(id(0L).compareTo(id(0L)), 0);
		Assert.equal(id(0L).compareTo(id(1L)), -1);
	}

	@Test
	public void testLongIdPredicate() {
		var predicate = Capability.LongId.filter(l -> l > 0L);
		Assert.equal(predicate.test(null), false);
		Assert.equal(predicate.test(id(0L)), false);
		Assert.equal(predicate.test(id(1L)), true);
	}

	@Test
	public void testNameDefault() {
		var named = new Capability.Name() {};
		Assert.find(named.name(), "^%s\\.\\d+@", getClass().getSimpleName());
	}

	@Test
	public void testName() {
		Assert.equal(Capability.Name.name("test", "abc"), "abc");
		Assert.find(Capability.Name.name("test", null), "String@");
	}

	@Test
	public void testNameOf() {
		Assert.equal(Capability.Name.nameOf(null), "null");
		Assert.equal(Capability.Name.nameOf(name(null)), null);
		Assert.equal(Capability.Name.nameOf(name("test")), "test");
	}

	@Test
	public void testNamePredicate() {
		var predicate = Capability.Name.filter(s -> s.length() > 1);
		Assert.equal(predicate.test(name("")), false);
		Assert.equal(predicate.test(name("a")), false);
		Assert.equal(predicate.test(name("test")), true);
		Assert.equal(predicate.test(new Capability.Name() {}), true);
	}

	@Test
	public void testInit() {
		int[] i = { 0 };
		Capability.Init.init(() -> i[0]++, null, () -> i[0] += 5);
		Assert.equal(i[0], 6);
	}

	@Test
	public void testIsEmptyPredicate() {
		Assert.equal(Capability.IsEmpty.NOT_BY.test(null), false);
		Assert.equal(Capability.IsEmpty.NOT_BY.test(IS_EMPTY_FALSE), false);
		Assert.equal(Capability.IsEmpty.NOT_BY.test(IS_EMPTY_TRUE), true);
	}

	@Test
	public void testEnabledPredicate() {
		Assert.equal(Capability.Enabled.BY.test(null), false);
		Assert.equal(Capability.Enabled.BY.test(ENABLED_FALSE), false);
		Assert.equal(Capability.Enabled.BY.test(ENABLED_TRUE), true);
	}

	@Test
	public void testEnabledComparator() {
		Assert.equal(Capability.Enabled.COMPARATOR.compare(null, null), 0);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(null, ENABLED_FALSE), -1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(null, ENABLED_TRUE), -1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, null), 1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, ENABLED_FALSE), 0);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_FALSE, ENABLED_TRUE), -1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, null), 1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, ENABLED_FALSE), 1);
		Assert.equal(Capability.Enabled.COMPARATOR.compare(ENABLED_TRUE, ENABLED_TRUE), 0);
	}

	@Test
	public void testEnabledConditional() {
		Assert.equal(Capability.Enabled.conditional(null, 1, 2), 2);
		Assert.equal(Capability.Enabled.conditional(() -> false, 1, 2), 2);
		Assert.equal(Capability.Enabled.conditional(() -> true, 1, 2), 1);
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
