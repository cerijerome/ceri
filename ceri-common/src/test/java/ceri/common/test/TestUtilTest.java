package ceri.common.test;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertRange;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.common.test.Assert.runtime;
import static ceri.common.test.Assert.throwable;
import static ceri.common.test.TestUtil.init;
import static ceri.common.test.TestUtil.resource;
import static ceri.common.test.TestUtil.testMap;
import static ceri.common.test.TestUtil.thrown;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.SystemIo;
import ceri.common.log.Level;
import ceri.common.property.TypedProperties;
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
		assertTrue(TestUtil.isTest);
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
			assertEquals(te.thread(), thread);
			assertEquals(te.element().getClassName(), getClass().getName());
			assertEquals(te.element().getMethodName(), "testFindTest");
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
			assertEquals(TestUtil.readString(), "test");
			assertEquals(TestUtil.readString(), "");
		}
	}

	@Test
	public void testReadStringWithBadInputStream() throws IOException {
		try (var sys = SystemIo.of()) {
			try (InputStream badIn = new InputStream() {
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
		ValueCondition<String> sync = ValueCondition.of();
		try (var exec = TestUtil.threadCall(sync::await)) {
			sync.signal("test");
			assertEquals(exec.get(), "test");
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
			StringBuilder b = new StringBuilder();
			sys.out(StringBuilders.printStream(b));
			TestUtil.exec(ExecTest.class);
			assertTrue(b.toString().contains("Exec should do this"));
			assertTrue(b.toString().contains("Exec test that"));
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
		Throwable t = thrown(() -> {
			throw new IOException("test");
		});
		throwable(t, IOException.class, "test");
		Assert.isNull(thrown(() -> {}));
	}

	@Test
	public void testInit() {
		boolean throwIt = false;
		assertEquals(init(() -> {
			if (throwIt) throw new IOException();
			return "test";
		}), "test");
		runtime(() -> init(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testNullErr() {
		try (var _ = TestUtil.nullOutErr()) {
			System.err.print("This text should not appear");
		}
	}

	@Test
	public void testTestMap() {
		Map<Integer, String> map = testMap(1, "1", 2, "2", 3);
		assertEquals(map.size(), 3);
		assertEquals(map.get(1), "1");
		assertEquals(map.get(2), "2");
		Assert.isNull(map.get(3));
	}

	@Test
	public void testResource() {
		assertEquals(resource("resource.txt"), "test");
		runtime(() -> resource("not-found.txt"));
	}

	@Test
	public void testTypedProperties() {
		TypedProperties properties = TestUtil.typedProperties("test", "a");
		assertEquals(properties.parse("b").get(), "123");
	}

	@Test
	public void testProperties() {
		Properties properties = TestUtil.properties("test");
		assertEquals(properties.getProperty("a.b"), "123");
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		assertEquals(TestUtil.readableString(bytes), "?a.??~!?");
		Assert.thrown(IllegalArgumentException.class,
			() -> TestUtil.readableString(bytes, 3, 2, "test", '?'));
		assertEquals(TestUtil.readableString(new byte[0], 0, 0, null, '.'), "");
		assertEquals(TestUtil.readableString(new byte[0], 0, 0, "", '.'), "");
	}

	@Test
	public void testRandomString() {
		String r = TestUtil.randomString(100);
		assertEquals(r.length(), 100);
		for (int i = 0; i < r.length(); i++)
			assertRange(r.charAt(i), ' ', '~');
	}

	@Test
	public void testPathsToUnix() {
		List<Path> paths = List.of(Path.of("a", "b", "c.txt"), Path.of("a", "a.txt"));
		assertUnordered(TestUtil.pathsToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testToUnixFromPath() {
		List<String> paths =
			List.of(Path.of("a", "b", "c.txt").toString(), Path.of("a", "a.txt").toString());
		assertUnordered(TestUtil.pathNamesToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testByteRange() {
		assertArray(TestUtil.byteRange(2, 2), 2);
		assertArray(TestUtil.byteRange(0, 3), 0, 1, 2, 3);
		assertArray(TestUtil.byteRange(-3, -6), -3, -4, -5, -6);
	}

	@Test
	public void testRandomBool() {
		TestUtil.randomBool();
	}

	@Test
	public void testRandomBytes() {
		assertEquals(TestUtil.randomBytes(0).length, 0);
		assertEquals(TestUtil.randomBytes(100).length, 100);
	}

	@Test
	public void testReader() {
		assertArray(TestUtil.reader(1, 2, 3).readBytes(), 1, 2, 3);
		assertArray(TestUtil.reader("abc").readBytes(), 'a', 'b', 'c');
	}

}
