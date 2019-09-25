package ceri.common.collection;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertList;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.lang.Double.parseDouble;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.color.Colorx;
import ceri.common.function.ObjIntFunction;
import org.junit.Test;

public class IndexedBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Indexed<String> i0 = Indexed.of(7, "7");
		Indexed<String> i1 = Indexed.of(7, "7");
		Indexed<String> n0 = Indexed.of(8, "7");
		Indexed<String> n1 = Indexed.of(7, "");
		Indexed<String> n2 = Indexed.of(7, "8");
		exerciseEquals(i0, i1);
		assertAllNotEqual(i0, n0, n1, n2);
	}

	@Test
	public void shouldBeConsumed() {
		Indexed<String> indexed = Indexed.of(100, "abc");
		StringBuilder b = new StringBuilder();
		indexed.consume((s, i) -> b.append(i).append(":").append(s));
		assertThat(b.toString(), is("100:abc"));
	}

	@Test
	public void shouldBeApplied() {
		Indexed<String> indexed = Indexed.of(100, "abc");
		assertThat(indexed.apply((s, i) -> i + ":" + s), is("100:abc"));
	}

}
