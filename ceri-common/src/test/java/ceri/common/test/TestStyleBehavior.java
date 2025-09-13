package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestStyle.behavior;
import static ceri.common.test.TestStyle.none;
import static ceri.common.test.TestStyle.test;
import org.junit.Test;

public class TestStyleBehavior {

	@Test
	public void shouldGuessStyleFromTargetOrTestClass() {
		assertEquals(TestStyle.guessFrom((Class<?>) null), none);
		assertEquals(TestStyle.guessFrom(TestUtil.class), test);
		assertEquals(TestStyle.guessFrom(TestStyle.class), behavior);
		assertEquals(TestStyle.guessFrom(TestStyleBehavior.class), behavior);
		assertEquals(TestStyle.guessFrom((String) null), none);
		assertEquals(TestStyle.guessFrom(""), none);
		assertEquals(TestStyle.guessFrom("Util"), test);
		assertEquals(TestStyle.guessFrom("Helper"), behavior);
	}

	@Test
	public void shouldConvertToTest() {
		assertString(test.test(null), "Test");
		assertString(test.test(""), "Test");
		assertString(test.test("My\n"), "My\n");
		assertString(test.test("My"), "MyTest");
		assertString(test.test("ceri.common.My"), "ceri.common.MyTest");
		assertString(test.test("/My.class"), "/MyTest.class");
		assertString(behavior.test(""), "Behavior");
		assertString(behavior.test("My.java"), "MyBehavior.java");
		assertString(behavior.test("ceri/common/My.java"), "ceri/common/MyBehavior.java");
		assertString(none.test("My\n"), "My\n");
		assertString(none.test(""), "");
		assertString(none.test("My"), "My");
		assertString(none.test("ceri.common.My"), "ceri.common.My");
		assertString(none.test("My.java"), "My.java");
		assertString(none.test("/My.class"), "/My.class");
		assertString(none.test("ceri/common/My.java"), "ceri/common/My.java");
	}

	@Test
	public void shouldExtractTarget() {
		assertString(TestStyle.target(null), "");
		assertString(TestStyle.target(""), "");
		assertString(TestStyle.target("Test\n"), "Test\n");
		assertString(TestStyle.target("Test"), "");
		assertString(TestStyle.target("Behavior.class"), ".class");
		assertString(TestStyle.target("MyTest"), "My");
		assertString(TestStyle.target("MyClass"), "MyClass");
		assertString(TestStyle.target("MyBehavior.java"), "My.java");
		assertString(TestStyle.target("ceri.common.MyTest"), "ceri.common.My");
		assertString(TestStyle.target("ceri/common/MyBehavior.class"), "ceri/common/My.class");
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
		assertEquals(TestStyle.from(null), none);
		assertEquals(TestStyle.from("Name"), none);
		assertEquals(TestStyle.from("Test\n"), none);
		assertEquals(TestStyle.from("ceri.common.test.FullName"), none);
		assertEquals(TestStyle.from("ceri.common.test"), none);
		assertEquals(TestStyle.from("ceri.common.behavior"), none);
		assertEquals(TestStyle.from("Test"), test);
		assertEquals(TestStyle.from("ceri.common.test.Test"), test);
		assertEquals(TestStyle.from("ceri.common.test.MyTest"), test);
		assertEquals(TestStyle.from("ceri.common.test.BehaviorTest"), test);
		assertEquals(TestStyle.from("ceri.common.test.TestTest"), test);
		assertEquals(TestStyle.from("Behavior"), behavior);
		assertEquals(TestStyle.from("ceri.common.test.Behavior"), behavior);
		assertEquals(TestStyle.from("ceri.common.test.TestBehavior"), behavior);
		assertEquals(TestStyle.from("ceri.common.test.TestBehavior"), behavior);
		assertEquals(TestStyle.from("ceri.common.test.BehaviorBehavior"), behavior);
	}

	@Test
	public void shouldGetFromPath() {
		assertEquals(TestStyle.from("Name.java"), none);
		assertEquals(TestStyle.from("Name.class"), none);
		assertEquals(TestStyle.from("Test.jar"), none);
		assertEquals(TestStyle.from("Test.javax"), none);
		assertEquals(TestStyle.from("Test.java"), test);
		assertEquals(TestStyle.from("Behavior.class"), behavior);
		assertEquals(TestStyle.from("/Test.java"), test);
		assertEquals(TestStyle.from("ceri/common/test/Test.class"), test);
		assertEquals(TestStyle.from("ceri/common/test/MyBehavior.class"), behavior);
	}

	@Test
	public void shouldGetFromSuffix() {
		assertEquals(TestStyle.fromSuffix(null), none);
		assertEquals(TestStyle.fromSuffix(""), none);
		assertEquals(TestStyle.fromSuffix("Tester"), none);
		assertEquals(TestStyle.fromSuffix("test"), none);
		assertEquals(TestStyle.fromSuffix("behavior"), none);
		assertEquals(TestStyle.fromSuffix("Test"), test);
		assertEquals(TestStyle.fromSuffix("Behavior"), behavior);
	}
}
