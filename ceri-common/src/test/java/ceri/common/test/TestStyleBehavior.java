package ceri.common.test;

import static ceri.common.test.TestStyle.behavior;
import static ceri.common.test.TestStyle.none;
import static ceri.common.test.TestStyle.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestStyleBehavior {

	@Test
	public void shouldGuessStyleFromTargetOrTestClass() {
		assertThat(TestStyle.guessFrom((Class<?>) null), is(none));
		assertThat(TestStyle.guessFrom(TestUtil.class), is(test));
		assertThat(TestStyle.guessFrom(TestStyle.class), is(behavior));
		assertThat(TestStyle.guessFrom(TestStyleBehavior.class), is(behavior));
		assertThat(TestStyle.guessFrom((String) null), is(none));
		assertThat(TestStyle.guessFrom(""), is(none));
		assertThat(TestStyle.guessFrom("Util"), is(test));
		assertThat(TestStyle.guessFrom("Helper"), is(behavior));
	}

	@Test
	public void shouldConvertToTest() {
		assertNull(test.test(null));
		assertThat(test.test(""), is("Test"));
		assertThat(test.test("My\n"), is("My\n"));
		assertThat(test.test("My"), is("MyTest"));
		assertThat(test.test("ceri.common.My"), is("ceri.common.MyTest"));
		assertThat(test.test("/My.class"), is("/MyTest.class"));
		assertThat(behavior.test(""), is("Behavior"));
		assertThat(behavior.test("My.java"), is("MyBehavior.java"));
		assertThat(behavior.test("ceri/common/My.java"), is("ceri/common/MyBehavior.java"));
		assertThat(none.test("My\n"), is("My\n"));
		assertThat(none.test(""), is(""));
		assertThat(none.test("My"), is("My"));
		assertThat(none.test("ceri.common.My"), is("ceri.common.My"));
		assertThat(none.test("My.java"), is("My.java"));
		assertThat(none.test("/My.class"), is("/My.class"));
		assertThat(none.test("ceri/common/My.java"), is("ceri/common/My.java"));
	}

	@Test
	public void shouldExtractTarget() {
		assertNull(TestStyle.target(null));
		assertThat(TestStyle.target(""), is(""));
		assertThat(TestStyle.target("Test\n"), is("Test\n"));
		assertThat(TestStyle.target("Test"), is(""));
		assertThat(TestStyle.target("Behavior.class"), is(".class"));
		assertThat(TestStyle.target("MyTest"), is("My"));
		assertThat(TestStyle.target("MyClass"), is("MyClass"));
		assertThat(TestStyle.target("MyBehavior.java"), is("My.java"));
		assertThat(TestStyle.target("ceri.common.MyTest"), is("ceri.common.My"));
		assertThat(TestStyle.target("ceri/common/MyBehavior.class"), is("ceri/common/My.class"));
	}

	@Test
	public void shouldDetermineIfTestHasStyle() {
		assertFalse(TestStyle.hasStyle(null));
		assertFalse(TestStyle.hasStyle(""));
		assertFalse(TestStyle.hasStyle("Name"));
		assertFalse(TestStyle.hasStyle("ceri.common.Name"));
		assertFalse(TestStyle.hasStyle("Name.java"));
		assertFalse(TestStyle.hasStyle("ceri/common/Name.java"));
		assertFalse(TestStyle.hasStyle(null));
		assertFalse(TestStyle.hasStyle(null));
		assertTrue(TestStyle.hasStyle("Test"));
		assertTrue(TestStyle.hasStyle("ceri.common.Test"));
		assertTrue(TestStyle.hasStyle("Test.java"));
		assertTrue(TestStyle.hasStyle("MyBehavior"));
		assertTrue(TestStyle.hasStyle("MyBehavior.class"));
		assertTrue(TestStyle.hasStyle("ceri/common/MyBehavior.class"));
	}

	@Test
	public void shouldGetFromName() {
		assertThat(TestStyle.from(null), is(none));
		assertThat(TestStyle.from("Name"), is(none));
		assertThat(TestStyle.from("Test\n"), is(none));
		assertThat(TestStyle.from("ceri.common.test.FullName"), is(none));
		assertThat(TestStyle.from("ceri.common.test"), is(none));
		assertThat(TestStyle.from("ceri.common.behavior"), is(none));
		assertThat(TestStyle.from("Test"), is(test));
		assertThat(TestStyle.from("ceri.common.test.Test"), is(test));
		assertThat(TestStyle.from("ceri.common.test.MyTest"), is(test));
		assertThat(TestStyle.from("ceri.common.test.BehaviorTest"), is(test));
		assertThat(TestStyle.from("ceri.common.test.TestTest"), is(test));
		assertThat(TestStyle.from("Behavior"), is(behavior));
		assertThat(TestStyle.from("ceri.common.test.Behavior"), is(behavior));
		assertThat(TestStyle.from("ceri.common.test.TestBehavior"), is(behavior));
		assertThat(TestStyle.from("ceri.common.test.TestBehavior"), is(behavior));
		assertThat(TestStyle.from("ceri.common.test.BehaviorBehavior"), is(behavior));
	}

	@Test
	public void shouldGetFromPath() {
		assertThat(TestStyle.from("Name.java"), is(none));
		assertThat(TestStyle.from("Name.class"), is(none));
		assertThat(TestStyle.from("Test.jar"), is(none));
		assertThat(TestStyle.from("Test.javax"), is(none));
		assertThat(TestStyle.from("Test.java"), is(test));
		assertThat(TestStyle.from("Behavior.class"), is(behavior));
		assertThat(TestStyle.from("/Test.java"), is(test));
		assertThat(TestStyle.from("ceri/common/test/Test.class"), is(test));
		assertThat(TestStyle.from("ceri/common/test/MyBehavior.class"), is(behavior));
	}

	@Test
	public void shouldGetFromSuffix() {
		assertThat(TestStyle.fromSuffix(null), is(none));
		assertThat(TestStyle.fromSuffix(""), is(none));
		assertThat(TestStyle.fromSuffix("Tester"), is(none));
		assertThat(TestStyle.fromSuffix("test"), is(none));
		assertThat(TestStyle.fromSuffix("behavior"), is(none));
		assertThat(TestStyle.fromSuffix("Test"), is(test));
		assertThat(TestStyle.fromSuffix("Behavior"), is(behavior));
	}

}
