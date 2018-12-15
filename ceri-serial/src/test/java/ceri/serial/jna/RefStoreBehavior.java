package ceri.serial.jna;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class RefStoreBehavior {

	@Test
	public void shouldAllowNullValue() {
		try (RefStore<String> rs = RefStore.of(0)) {
			assertThat(rs.reference(null), is(0));
			assertThat(rs.reference(null, 1000), is(0));
			assertNull(rs.referenceByToken(0));
			assertNull(rs.referenceByToken(1));
			assertThat(rs.unreference(null), is(0));
			assertThat(rs.unreference("Test"), is(0));
			assertNull(rs.unreferenceByToken(0));
			assertNull(rs.unreferenceByToken(1));
			assertNull(rs.peek(0));
			assertNull(rs.peek(1));
			assertNull(rs.get(0));
			assertNull(rs.get(1));
			assertThat(rs.remove(null), is(0));
			assertThat(rs.remove("Test"), is(0));
			assertNull(rs.removeByToken(0));
			assertNull(rs.removeByToken(1));
		}
	}

	@Test
	public void shouldRetrieveSameObject() {
		try (RefStore<String> rs = RefStore.of()) {
			String s0 = new String("Test");
			String s1 = new String("Test");
			int token0 = rs.reference(s0);
			int token1 = rs.reference(s1);
			assertNotEquals(token0, token1);
			assertTrue(rs.peek(token0) == s0);
			assertTrue(rs.get(token1) == s1);
		}
	}

	@Test
	public void shouldRemoveUnreferencedObjects() {
		try (RefStore<String> rs = RefStore.of()) {
			String s0 = new String("Test0");
			String s1 = new String("Test1");
			int token0 = rs.reference(s0);
			rs.referenceByToken(token0);
			rs.reference(s0);
			int token1 = rs.reference(s1);
			assertThat(rs.get(token0), is(s0));
			assertThat(rs.unreference(s0), is(token0));
			assertThat(rs.unreference(s0), is(token0));
			assertThat(rs.get(token0), is(s0));
			assertThat(rs.unreferenceByToken(token0), is(s0));
			assertNull(rs.peek(token0));
			assertThat(rs.unreference(s0), is(0));
			assertThat(rs.peek(token1), is(s1));
		}
	}

	@Test
	public void shouldExpireOldObjects() {
		try (RefStore<String> rs = RefStore.of()) {
			String s0 = new String("Test0");
			String s1 = new String("Test1");
			String s2 = new String("Test2");
			int token0 = rs.reference(s0, 1);
			int token1 = rs.reference(s1, 100000);
			int token2 = rs.reference(s2, 0);
			assertThat(rs.peek(token0), is(s0));
			assertThat(rs.peek(token1), is(s1));
			assertThat(rs.peek(token2), is(s2));
			BasicUtil.delay(1);
			rs.expireRefs();
			assertNull(rs.peek(token0));
			assertThat(rs.peek(token1), is(s1));
			assertThat(rs.get(token2), is(s2));
			rs.expireRefs();
			assertThat(rs.peek(token1), is(s1));
			assertThat(rs.peek(token2), is(s2));
		}
	}

}
