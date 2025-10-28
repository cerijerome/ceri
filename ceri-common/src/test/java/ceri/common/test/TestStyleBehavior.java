package ceri.common.test;

import static ceri.common.test.TestStyle.behavior;
import static ceri.common.test.TestStyle.none;
import static ceri.common.test.TestStyle.test;
import org.junit.Test;

public class TestStyleBehavior {

	@Test
	public void shouldGuessStyleFromTargetOrTestClass() {
		Assert.equal(TestStyle.guessFrom((Class<?>) null), none);
		Assert.equal(TestStyle.guessFrom(TestingTest.class), test);
		Assert.equal(TestStyle.guessFrom(TestStyle.class), behavior);
		Assert.equal(TestStyle.guessFrom(TestStyleBehavior.class), behavior);
		Assert.equal(TestStyle.guessFrom((String) null), none);
		Assert.equal(TestStyle.guessFrom(""), none);
		Assert.equal(TestStyle.guessFrom("Util"), test);
		Assert.equal(TestStyle.guessFrom("Helper"), behavior);
	}

	@Test
	public void shouldConvertToTest() {
		Assert.string(test.test(null), "Test");
		Assert.string(test.test(""), "Test");
		Assert.string(test.test("My\n"), "My\n");
		Assert.string(test.test("My"), "MyTest");
		Assert.string(test.test("ceri.common.My"), "ceri.common.MyTest");
		Assert.string(test.test("/My.class"), "/MyTest.class");
		Assert.string(behavior.test(""), "Behavior");
		Assert.string(behavior.test("My.java"), "MyBehavior.java");
		Assert.string(behavior.test("ceri/common/My.java"), "ceri/common/MyBehavior.java");
		Assert.string(none.test("My\n"), "My\n");
		Assert.string(none.test(""), "");
		Assert.string(none.test("My"), "My");
		Assert.string(none.test("ceri.common.My"), "ceri.common.My");
		Assert.string(none.test("My.java"), "My.java");
		Assert.string(none.test("/My.class"), "/My.class");
		Assert.string(none.test("ceri/common/My.java"), "ceri/common/My.java");
	}

	@Test
	public void shouldExtractTarget() {
		Assert.string(TestStyle.target(null), "");
		Assert.string(TestStyle.target(""), "");
		Assert.string(TestStyle.target("Test\n"), "Test\n");
		Assert.string(TestStyle.target("Test"), "");
		Assert.string(TestStyle.target("Behavior.class"), ".class");
		Assert.string(TestStyle.target("MyTest"), "My");
		Assert.string(TestStyle.target("MyClass"), "MyClass");
		Assert.string(TestStyle.target("MyBehavior.java"), "My.java");
		Assert.string(TestStyle.target("ceri.common.MyTest"), "ceri.common.My");
		Assert.string(TestStyle.target("ceri/common/MyBehavior.class"), "ceri/common/My.class");
	}

	@Test
	public void shouldDetermineIfTestHasStyle() {
		Assert.no(TestStyle.hasStyle(null));
		Assert.no(TestStyle.hasStyle(""));
		Assert.no(TestStyle.hasStyle("Name"));
		Assert.no(TestStyle.hasStyle("ceri.common.Name"));
		Assert.no(TestStyle.hasStyle("Name.java"));
		Assert.no(TestStyle.hasStyle("ceri/common/Name.java"));
		Assert.no(TestStyle.hasStyle(null));
		Assert.no(TestStyle.hasStyle(null));
		Assert.yes(TestStyle.hasStyle("Test"));
		Assert.yes(TestStyle.hasStyle("ceri.common.Test"));
		Assert.yes(TestStyle.hasStyle("Test.java"));
		Assert.yes(TestStyle.hasStyle("MyBehavior"));
		Assert.yes(TestStyle.hasStyle("MyBehavior.class"));
		Assert.yes(TestStyle.hasStyle("ceri/common/MyBehavior.class"));
	}

	@Test
	public void shouldGetFromName() {
		Assert.equal(TestStyle.from(null), none);
		Assert.equal(TestStyle.from("Name"), none);
		Assert.equal(TestStyle.from("Test\n"), none);
		Assert.equal(TestStyle.from("ceri.common.test.FullName"), none);
		Assert.equal(TestStyle.from("ceri.common.test"), none);
		Assert.equal(TestStyle.from("ceri.common.behavior"), none);
		Assert.equal(TestStyle.from("Test"), test);
		Assert.equal(TestStyle.from("ceri.common.test.Test"), test);
		Assert.equal(TestStyle.from("ceri.common.test.MyTest"), test);
		Assert.equal(TestStyle.from("ceri.common.test.BehaviorTest"), test);
		Assert.equal(TestStyle.from("ceri.common.test.TestTest"), test);
		Assert.equal(TestStyle.from("Behavior"), behavior);
		Assert.equal(TestStyle.from("ceri.common.test.Behavior"), behavior);
		Assert.equal(TestStyle.from("ceri.common.test.TestBehavior"), behavior);
		Assert.equal(TestStyle.from("ceri.common.test.TestBehavior"), behavior);
		Assert.equal(TestStyle.from("ceri.common.test.BehaviorBehavior"), behavior);
	}

	@Test
	public void shouldGetFromPath() {
		Assert.equal(TestStyle.from("Name.java"), none);
		Assert.equal(TestStyle.from("Name.class"), none);
		Assert.equal(TestStyle.from("Test.jar"), none);
		Assert.equal(TestStyle.from("Test.javax"), none);
		Assert.equal(TestStyle.from("Test.java"), test);
		Assert.equal(TestStyle.from("Behavior.class"), behavior);
		Assert.equal(TestStyle.from("/Test.java"), test);
		Assert.equal(TestStyle.from("ceri/common/test/Test.class"), test);
		Assert.equal(TestStyle.from("ceri/common/test/MyBehavior.class"), behavior);
	}

	@Test
	public void shouldGetFromSuffix() {
		Assert.equal(TestStyle.fromSuffix(null), none);
		Assert.equal(TestStyle.fromSuffix(""), none);
		Assert.equal(TestStyle.fromSuffix("Tester"), none);
		Assert.equal(TestStyle.fromSuffix("test"), none);
		Assert.equal(TestStyle.fromSuffix("behavior"), none);
		Assert.equal(TestStyle.fromSuffix("Test"), test);
		Assert.equal(TestStyle.fromSuffix("Behavior"), behavior);
	}
}
