package ceri.common.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.SystemIo;
import ceri.common.log.Level;
import ceri.common.text.StringBuilders;

public class TestUtilTest {

	static class Uncreatable {
		private Uncreatable() {
			throw new RuntimeException();
		}
	}

	static record Rec(int i, String s) {
		static int si = 3;
	}

	enum BadEnum {
		bad;

		BadEnum() {
			throw new RuntimeException();
		}
	}

	@Test
	public void testIsTest() {
		Assert.yes(TestUtil.isTest);
	}

	@Test
	public void testGc() {
		TestUtil.gc();
	}

	@Test
	public void testFindTest() {
		var thread = Thread.currentThread();
		try (var t = TestUtil.threadRun(() -> {
			var te = TestUtil.findTest();
			Assert.equal(te.thread(), thread);
			Assert.equal(te.element().getClassName(), getClass().getName());
			Assert.equal(te.element().getMethodName(), "testFindTest");
		})) {
			t.get();
		}
	}

	@Test
	public void testExerciseRecord() {
		TestUtil.exerciseRecord(null);
		TestUtil.exerciseRecord(new Rec(-1, "test"));
	}

	@Test
	public void testExerciseEnum() {
		TestUtil.exerciseEnum(Level.class);
		Assert.thrown(() -> TestUtil.exerciseEnum(BadEnum.class));
	}

	@Test
	public void testRunRepeat() throws InterruptedException {
		var sync = BoolCondition.of();
		try (var _ = TestUtil.runRepeat(sync::signal)) {
			sync.await();
			sync.await();
		}
	}

	@Test
	public void testRunRepeatWithIndex() throws InterruptedException {
		var sync = ValueCondition.<Integer>of();
		try (var _ = TestUtil.runRepeat(i -> sync.signal(i))) {
			sync.await(i -> i > 1);
		}
	}

	@Test
	public void testReadString() {
		try (var sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			Assert.equal(TestUtil.readString(), "test");
			Assert.equal(TestUtil.readString(), "");
		}
	}

	@Test
	public void testReadStringWithBadInputStream() throws IOException {
		try (var sys = SystemIo.of()) {
			try (var badIn = new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException();
				}
			}) {
				sys.in(badIn);
				Assert.thrown(TestUtil::readString);
			}
		}
	}

	@Test
	public void testThreadCall() {
		var sync = ValueCondition.<String>of();
		try (var exec = TestUtil.threadCall(sync::await)) {
			sync.signal("test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void testThreadRun() {
		var sync = BoolCondition.of();
		try (var exec = TestUtil.threadRun(sync::await)) {
			sync.signal();
			exec.get();
		}
	}

	public static class ExecTest {
		@Test
		public void shouldDoThis() {}

		@Test
		public void testThat() {}
	}

	@SuppressWarnings("resource")
	@Test
	public void testExec() {
		try (var sys = SystemIo.of()) {
			var b = new StringBuilder();
			sys.out(StringBuilders.printStream(b));
			TestUtil.exec(ExecTest.class);
			Assert.yes(b.toString().contains("Exec should do this"));
			Assert.yes(b.toString().contains("Exec test that"));
		}
	}

	@Test
	public void testFirstSystemProperty() {
		Assert.notNull(TestUtil.firstSystemProperty());
	}

	@Test
	public void testFirstEnvironmentVariable() {
		Assert.notNull(TestUtil.firstEnvironmentVariable());
	}

	@Test
	public void testException() {
		var t = TestUtil.thrown(() -> Assert.throwIo());
		Assert.throwable(t, IOException.class, "throwIo");
		Assert.isNull(TestUtil.thrown(() -> {}));
	}

	@Test
	public void testInit() {
		boolean throwIt = false;
		Assert.equal(TestUtil.init(() -> {
			if (throwIt) throw new IOException();
			return "test";
		}), "test");
		Assert.runtime(() -> TestUtil.init(() -> Assert.throwIo()));
	}

	@Test
	public void testNullErr() {
		try (var _ = TestUtil.nullOutErr()) {
			System.err.print("This text should not appear");
		}
	}

	@Test
	public void testTestMap() {
		var map = TestUtil.testMap(1, "1", 2, "2", 3);
		Assert.equal(map.size(), 3);
		Assert.equal(map.get(1), "1");
		Assert.equal(map.get(2), "2");
		Assert.isNull(map.get(3));
	}

	@Test
	public void testResource() {
		Assert.equal(TestUtil.resource("resource.txt"), "test");
		Assert.runtime(() -> TestUtil.resource("not-found.txt"));
	}

	@Test
	public void testTypedProperties() {
		var properties = TestUtil.typedProperties("test", "a");
		Assert.equal(properties.parse("b").get(), "123");
	}

	@Test
	public void testProperties() {
		var properties = TestUtil.properties("test");
		Assert.equal(properties.getProperty("a.b"), "123");
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		Assert.equal(TestUtil.readableString(bytes), "?a.??~!?");
		Assert.thrown(IllegalArgumentException.class,
			() -> TestUtil.readableString(bytes, 3, 2, "test", '?'));
		Assert.equal(TestUtil.readableString(new byte[0], 0, 0, null, '.'), "");
		Assert.equal(TestUtil.readableString(new byte[0], 0, 0, "", '.'), "");
	}

	@Test
	public void testRandomString() {
		var r = TestUtil.randomString(100);
		Assert.equal(r.length(), 100);
		for (int i = 0; i < r.length(); i++)
			Assert.range(r.charAt(i), ' ', '~');
	}

	@Test
	public void testPathsToUnix() {
		var paths = List.of(Path.of("a", "b", "c.txt"), Path.of("a", "a.txt"));
		Assert.unordered(TestUtil.pathsToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testToUnixFromPath() {
		var paths =
			List.of(Path.of("a", "b", "c.txt").toString(), Path.of("a", "a.txt").toString());
		Assert.unordered(TestUtil.pathNamesToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testByteRange() {
		Assert.array(TestUtil.byteRange(2, 2), 2);
		Assert.array(TestUtil.byteRange(0, 3), 0, 1, 2, 3);
		Assert.array(TestUtil.byteRange(-3, -6), -3, -4, -5, -6);
	}

	@Test
	public void testRandomBool() {
		TestUtil.randomBool();
	}

	@Test
	public void testRandomBytes() {
		Assert.equal(TestUtil.randomBytes(0).length, 0);
		Assert.equal(TestUtil.randomBytes(100).length, 100);
	}

	@Test
	public void testReader() {
		Assert.array(TestUtil.reader(1, 2, 3).readBytes(), 1, 2, 3);
		Assert.array(TestUtil.reader("abc").readBytes(), 'a', 'b', 'c');
	}
}
