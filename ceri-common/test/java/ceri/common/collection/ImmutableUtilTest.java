package ceri.common.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.test.TestImmutable;

public class ImmutableUtilTest {

	@Test
	public void testCopy() {
		final List<Integer> list = ImmutableUtil.copy(1, 2, 3, 4, 5);
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
		TestImmutable.assertImmutableList(list);
	}

	@Test
	public void testArrayCopyAsList() {
		final List<Integer> list = ImmutableUtil.copyAsList(new Integer[] { 1, 2, 3, 4, 5 });
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
		TestImmutable.assertImmutableList(list);
	}

	@Test
	public void testCopyAsList() {
		List<Integer> srcList = new ArrayList<>();
		Collections.addAll(srcList, 1, 2, 3, 4, 5);
		List<Integer> copy = new ArrayList<>(srcList);
		final List<Integer> list = ImmutableUtil.copyAsList(srcList);
		srcList.remove(0);
		assertThat(list, is(copy));
		TestImmutable.assertImmutableList(list);
	}

	@Test
	public void testCopyAsSet() {
		Set<Integer> srcSet = new HashSet<>();
		Collections.addAll(srcSet, 1, 2, 3, 4, 5);
		Set<Integer> copy = new HashSet<>(srcSet);
		final Set<Integer> set = ImmutableUtil.copyAsSet(srcSet);
		srcSet.remove(0);
		assertThat(set, is(copy));
		TestImmutable.assertImmutableCollection(set);
	}

	@Test
	public void testCopyAsMap() {
		Map<Integer, String> srcMap = new HashMap<>();
		srcMap.put(1, "1");
		srcMap.put(2, "2");
		srcMap.put(3, "3");
		srcMap.put(4, "4");
		srcMap.put(5, "5");
		Map<Integer, String> copy = new HashMap<>(srcMap);
		final Map<Integer, String> map = ImmutableUtil.copyAsMap(srcMap);
		srcMap.remove(1);
		assertThat(map, is(copy));
		TestImmutable.assertImmutableMap(map);
	}

}
