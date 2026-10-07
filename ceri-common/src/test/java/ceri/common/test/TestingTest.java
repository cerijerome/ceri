package ceri.common.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.log.Level;
import ceri.common.process.Processes;
import ceri.common.text.StringBuilders;

public class TestingTest {

	static record Rec(int i, String s) {
		static int si = 3;
	}

	enum BadEnum {
		bad;

		BadEnum() {
			throw new RuntimeException();
		}
	}

	public static class ExecTest {
		@Test
		public void shouldDoThis() {}

		@Test
		public void testThat() {}
	}

	@Test
	public void testGuessStyleFromTargetOrTestClass() {
		Assert.equal(Testing.Style.guessFrom((Class<?>) null), Testing.Style.none);
		Assert.equal(Testing.Style.guessFrom(TestingTest.class), Testing.Style.test);
		Assert.equal(Testing.Style.guessFrom(Testing.Style.class), Testing.Style.behavior);
		Assert.equal(Testing.Style.guessFrom(ErrorGenBehavior.class), Testing.Style.behavior);
		Assert.equal(Testing.Style.guessFrom((String) null), Testing.Style.none);
		Assert.equal(Testing.Style.guessFrom(""), Testing.Style.none);
		Assert.equal(Testing.Style.guessFrom("Util"), Testing.Style.test);
		Assert.equal(Testing.Style.guessFrom("Helper"), Testing.Style.behavior);
	}

	@Test
	public void shouldConvertToTestStyle() {
		Assert.string(Testing.Style.test.test(null), "Test");
		Assert.string(Testing.Style.test.test(""), "Test");
		Assert.string(Testing.Style.test.test("My\n"), "My\n");
		Assert.string(Testing.Style.test.test("My"), "MyTest");
		Assert.string(Testing.Style.test.test("ceri.common.My"), "ceri.common.MyTest");
		Assert.string(Testing.Style.test.test("/My.class"), "/MyTest.class");
		Assert.string(Testing.Style.behavior.test(""), "Behavior");
		Assert.string(Testing.Style.behavior.test("My.java"), "MyBehavior.java");
		Assert.string(Testing.Style.behavior.test("ceri/common/My.java"),
			"ceri/common/MyBehavior.java");
		Assert.string(Testing.Style.none.test("My\n"), "My\n");
		Assert.string(Testing.Style.none.test(""), "");
		Assert.string(Testing.Style.none.test("My"), "My");
		Assert.string(Testing.Style.none.test("ceri.common.My"), "ceri.common.My");
		Assert.string(Testing.Style.none.test("My.java"), "My.java");
		Assert.string(Testing.Style.none.test("/My.class"), "/My.class");
		Assert.string(Testing.Style.none.test("ceri/common/My.java"), "ceri/common/My.java");
	}

	@Test
	public void shouldExtractStyleTarget() {
		Assert.string(Testing.Style.target(null), "");
		Assert.string(Testing.Style.target(""), "");
		Assert.string(Testing.Style.target("Test\n"), "Test\n");
		Assert.string(Testing.Style.target("Test"), "");
		Assert.string(Testing.Style.target("Behavior.class"), ".class");
		Assert.string(Testing.Style.target("MyTest"), "My");
		Assert.string(Testing.Style.target("MyClass"), "MyClass");
		Assert.string(Testing.Style.target("MyBehavior.java"), "My.java");
		Assert.string(Testing.Style.target("ceri.common.MyTest"), "ceri.common.My");
		Assert.string(Testing.Style.target("ceri/common/MyBehavior.class"), "ceri/common/My.class");
	}

	@Test
	public void shouldDetermineIfTestHasStyle() {
		Assert.no(Testing.Style.hasStyle(null));
		Assert.no(Testing.Style.hasStyle(""));
		Assert.no(Testing.Style.hasStyle("Name"));
		Assert.no(Testing.Style.hasStyle("ceri.common.Name"));
		Assert.no(Testing.Style.hasStyle("Name.java"));
		Assert.no(Testing.Style.hasStyle("ceri/common/Name.java"));
		Assert.no(Testing.Style.hasStyle(null));
		Assert.no(Testing.Style.hasStyle(null));
		Assert.yes(Testing.Style.hasStyle("Test"));
		Assert.yes(Testing.Style.hasStyle("ceri.common.Test"));
		Assert.yes(Testing.Style.hasStyle("Test.java"));
		Assert.yes(Testing.Style.hasStyle("MyBehavior"));
		Assert.yes(Testing.Style.hasStyle("MyBehavior.class"));
		Assert.yes(Testing.Style.hasStyle("ceri/common/MyBehavior.class"));
	}

	@Test
	public void shouldGetStyleFromName() {
		Assert.equal(Testing.Style.from(null), Testing.Style.none);
		Assert.equal(Testing.Style.from("Name"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Test\n"), Testing.Style.none);
		Assert.equal(Testing.Style.from("ceri.common.test.FullName"), Testing.Style.none);
		Assert.equal(Testing.Style.from("ceri.common.test"), Testing.Style.none);
		Assert.equal(Testing.Style.from("ceri.common.behavior"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Test"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri.common.test.Test"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri.common.test.MyTest"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri.common.test.BehaviorTest"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri.common.test.TestTest"), Testing.Style.test);
		Assert.equal(Testing.Style.from("Behavior"), Testing.Style.behavior);
		Assert.equal(Testing.Style.from("ceri.common.test.Behavior"), Testing.Style.behavior);
		Assert.equal(Testing.Style.from("ceri.common.test.TestBehavior"), Testing.Style.behavior);
		Assert.equal(Testing.Style.from("ceri.common.test.TestBehavior"), Testing.Style.behavior);
		Assert.equal(Testing.Style.from("ceri.common.test.BehaviorBehavior"),
			Testing.Style.behavior);
	}

	@Test
	public void shouldGetStyleFromPath() {
		Assert.equal(Testing.Style.from("Name.java"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Name.class"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Test.jar"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Test.javax"), Testing.Style.none);
		Assert.equal(Testing.Style.from("Test.java"), Testing.Style.test);
		Assert.equal(Testing.Style.from("Behavior.class"), Testing.Style.behavior);
		Assert.equal(Testing.Style.from("/Test.java"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri/common/test/Test.class"), Testing.Style.test);
		Assert.equal(Testing.Style.from("ceri/common/test/MyBehavior.class"),
			Testing.Style.behavior);
	}

	@Test
	public void shouldGetStyleFromSuffix() {
		Assert.equal(Testing.Style.fromSuffix(null), Testing.Style.none);
		Assert.equal(Testing.Style.fromSuffix(""), Testing.Style.none);
		Assert.equal(Testing.Style.fromSuffix("Tester"), Testing.Style.none);
		Assert.equal(Testing.Style.fromSuffix("test"), Testing.Style.none);
		Assert.equal(Testing.Style.fromSuffix("behavior"), Testing.Style.none);
		Assert.equal(Testing.Style.fromSuffix("Test"), Testing.Style.test);
		Assert.equal(Testing.Style.fromSuffix("Behavior"), Testing.Style.behavior);
	}

	@Test
	public void testIsTest() {
		Assert.yes(Testing.isTest());
	}

	@SuppressWarnings("resource")
	@Test
	public void testExec() {
		try (var sys = SystemIo.of()) {
			var b = new StringBuilder();
			sys.out(StringBuilders.printStream(b));
			Testing.exec(ExecTest.class);
			Assert.yes(b.toString().contains("Exec should do this"));
			Assert.yes(b.toString().contains("Exec test that"));
		}
	}

	@Test
	public void testGc() {
		Testing.gc();
	}

	@Test
	public void testFindTest() {
		var thread = Thread.currentThread();
		try (var t = TestConcurrent.threadRun(() -> {
			var te = Testing.findTest();
			Assert.equal(te.thread(), thread);
			Assert.equal(te.element().getClassName(), getClass().getName());
			Assert.equal(te.element().getMethodName(), "testFindTest");
		})) {
			t.get();
		}
	}

	@Test
	public void testExerciseRecord() {
		Testing.exerciseRecord(null);
		Testing.exerciseRecord(new Rec(-1, "test"));
	}

	@Test
	public void testExerciseEnum() {
		Testing.exerciseEnum(Level.class);
		Assert.thrown(() -> Testing.exerciseEnum(BadEnum.class));
	}

	@Test
	public void testExerciseSwitch() {
		Testing.exerciseSwitch(s -> {
			switch (s) {
				case "" -> Assert.equal(s, "");
				case "x" -> Assert.equal(s, "x");
				case null -> Assert.equal(s, null);
				default -> Assert.yes(s.startsWith("\0"));
			}
		}, null, "", "x");
	}

	@Test
	public void testInit() {
		boolean throwIt = false;
		Assert.equal(Testing.init(() -> {
			if (throwIt) throw new IOException();
			return "test";
		}), "test");
		Assert.runtime(() -> Testing.init(() -> Assert.throwIo()));
	}

	@SuppressWarnings("resource")
	@Test
	public void testClose() {
		Assert.equal(Testing.close((AutoCloseable) null), null);
		Assert.equal(Testing.close(TestConcurrent.futureOf(1)), null);
		Assert.equal(Testing.close(Processes.NULL), null);
		Assert.assertion(() -> Testing.close("test"));
	}

	@Test
	public void testThrown() {
		var t = Testing.thrown(() -> Assert.throwIo());
		Assert.throwable(t, IOException.class, "throwIo");
		Assert.isNull(Testing.thrown(() -> {}));
	}

	@Test
	public void testResource() {
		Assert.equal(Testing.resource("resource.txt"), "test");
		Assert.runtime(() -> Testing.resource("not-found.txt"));
	}

	@Test
	public void testTypedProperties() {
		var properties = Testing.properties("test", "a");
		Assert.equal(properties.parse("b").get(), "123");
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		Assert.equal(Testing.readableString(bytes), "?a.??~!?");
		Assert.equal(Testing.readableString(new byte[0], 0, 0, null, '.'), "");
		Assert.equal(Testing.readableString(new byte[0], 0, 0, StandardCharsets.US_ASCII, '.'), "");
	}

	@Test
	public void testByteReader() {
		Assert.array(Testing.byteReader(1, 2, 3).readBytes(), 1, 2, 3);
		Assert.array(Testing.byteReader("abc").readBytes(), 'a', 'b', 'c');
	}

	@Test
	public void testByteRange() {
		Assert.array(Testing.byteRange(2, 2), 2);
		Assert.array(Testing.byteRange(0, 3), 0, 1, 2, 3);
		Assert.array(Testing.byteRange(-3, -6), -3, -4, -5, -6);
	}

	@Test
	public void testRandomBools() {
		Testing.randomBool();
		Assert.equal(Testing.randomBools(5).length, 5);
	}

	@Test
	public void testRandomChars() {
		Assert.equal(Testing.randomChars(2).length, 2);
		var a = Testing.randomChars(3, 'a', 'c');
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, 'a', 'c');
	}

	@Test
	public void testRandomBytes() {
		Assert.equal(Testing.randomBytes(2).length, 2);
		var a = Testing.randomBytes(3, -3, 3);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, -3, 3);
	}

	@Test
	public void testRandomShorts() {
		Assert.equal(Testing.randomShorts(2).length, 2);
		var a = Testing.randomShorts(3, -3, 3);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, -3, 3);
	}

	@Test
	public void testRandomInts() {
		Assert.equal(Testing.randomInts(2).length, 2);
		Assert.equal(Testing.randomInts(3, Integer.MIN_VALUE, Integer.MAX_VALUE).length, 3);
		var a = Testing.randomInts(3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
		a = Testing.randomInts(3, Integer.MIN_VALUE, Integer.MIN_VALUE + 2);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, Integer.MIN_VALUE, Integer.MIN_VALUE + 2);
	}

	@Test
	public void testRandomLongs() {
		Assert.equal(Testing.randomLongs(2).length, 2);
		Assert.equal(Testing.randomLongs(3, Long.MIN_VALUE, Long.MAX_VALUE).length, 3);
		var a = Testing.randomLongs(3, Long.MAX_VALUE - 2, Long.MAX_VALUE);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, Long.MAX_VALUE - 2, Long.MAX_VALUE);
		a = Testing.randomLongs(3, Long.MIN_VALUE, Long.MIN_VALUE + 2);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, Long.MIN_VALUE, Long.MIN_VALUE + 2);
	}

	@Test
	public void testRandomFloats() {
		Assert.equal(Testing.randomFloats(2).length, 2);
		var a = Testing.randomFloats(3, -3, 3);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, -3, 3);
	}

	@Test
	public void testRandomDoubles() {
		Assert.equal(Testing.randomDoubles(2).length, 2);
		var a = Testing.randomDoubles(3, -3, 3);
		Assert.equal(a.length, 3);
		for (var r : a)
			Assert.range(r, -3, 3);
	}

	@Test
	public void testRandomString() {
		var r = Testing.randomString(100);
		Assert.equal(r.length(), 100);
		for (int i = 0; i < r.length(); i++)
			Assert.range(r.charAt(i), ' ', '~');
	}
}
