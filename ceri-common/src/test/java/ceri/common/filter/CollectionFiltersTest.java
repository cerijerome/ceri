package ceri.common.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class CollectionFiltersTest {

	@Test
	public void testNotEmpty() {
		assertFalse(CollectionFilters.notEmpty().filter(Collections.emptySet()));
		assertTrue(CollectionFilters.<Integer>notEmpty().filter(Collections.singleton(1)));
		Filter<Collection<String>> filter = CollectionFilters.notEmpty();
		List<String> list = Arrays.asList("aaa", "bb", "c");
		assertTrue(filter.filter(list));
	}

	@Test
	public void testSize() {
		Filter<Collection<Integer>> filter = CollectionFilters.size(Filters.eq(1));
		assertFalse(filter.filter(Collections.<Integer>emptyList()));
		assertTrue(filter.filter(Collections.singleton(0)));
		assertFalse(filter.filter(Arrays.asList(-1, 1)));
		assertFalse(filter.filter(null));
	}
	
	@Test
	public void testAtIndex() {
		Filter<List<String>> filter = CollectionFilters.atIndex(1, Filters.max("B"));
		assertTrue(filter.filter(Arrays.asList("C", "A", "C")));
		assertFalse(filter.filter(Arrays.asList("A", "C", "A")));
	}

	@Test
	public void testAll() {
		Filter<Collection<Boolean>> filter = CollectionFilters.all(Filters.eq(true));
		assertTrue(filter.filter(Collections.<Boolean>emptyList()));
		assertFalse(filter.filter(Arrays.asList(false)));
		assertTrue(filter.filter(Arrays.asList(true)));
		assertFalse(filter.filter(Arrays.asList(false, false)));
		assertFalse(filter.filter(Arrays.asList(true, false)));
		assertFalse(filter.filter(Arrays.asList(false, true)));
		assertTrue(filter.filter(Arrays.asList(true, true)));
	}

	@Test
	public void testAny() {
		Filter<Collection<Boolean>> filter = CollectionFilters.any(Filters.eq(true));
		assertFalse(filter.filter(Collections.<Boolean>emptyList()));
		assertFalse(filter.filter(Arrays.asList(false)));
		assertTrue(filter.filter(Arrays.asList(true)));
		assertFalse(filter.filter(Arrays.asList(false, false)));
		assertTrue(filter.filter(Arrays.asList(true, false)));
		assertTrue(filter.filter(Arrays.asList(false, true)));
		assertTrue(filter.filter(Arrays.asList(true, true)));
	}

}
