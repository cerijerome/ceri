package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertRange;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
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
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.SystemIo;
import ceri.common.property.TypedProperties;
import ceri.common.text.StringUtil;
import ceri.common.util.Align;

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
	public void testExerciseRecord() {
		TestUtil.exerciseRecord(null);
		TestUtil.exerciseRecord(new Rec(-1, "test"));
	}

	@Test
	public void testExerciseEnum() {
		TestUtil.exerciseEnum(Align.H.class);
		assertThrown(() -> TestUtil.exerciseEnum(BadEnum.class));
	}

	@Test
	public void testRunRepeat() throws InterruptedException {
		var sync = BooleanCondition.of();
		try (var exec = TestUtil.runRepeat(sync::signal)) {
			sync.await();
			sync.await();
		}
	}

	@Test
	public void testRunRepeatWithIndex() throws InterruptedException {
		var sync = ValueCondition.<Integer>of();
		try (var exec = TestUtil.runRepeat(i -> sync.signal(i))) {
			sync.await(i -> i > 1);
		}
	}

	@Test
	public void testReadString() {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			assertEquals(TestUtil.readString(), "test");
			assertEquals(TestUtil.readString(), "");
		}
	}

	@Test
	public void testReadStringWithBadInputStream() throws IOException {
		try (SystemIo sys = SystemIo.of()) {
			try (InputStream badIn = new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException();
				}
			}) {
				sys.in(badIn);
				assertThrown(TestUtil::readString);
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
		BooleanCondition sync = BooleanCondition.of();
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
		try (SystemIo sys = SystemIo.of()) {
			StringBuilder b = new StringBuilder();
			sys.out(StringUtil.asPrintStream(b));
			TestUtil.exec(ExecTest.class);
			assertTrue(b.toString().contains("Exec should do this"));
			assertTrue(b.toString().contains("Exec test that"));
		}
	}

	@Test
	public void testFirstSystemProperty() {
		assertNotNull(TestUtil.firstSystemProperty());
	}

	@Test
	public void testFirstEnvironmentVariable() {
		assertNotNull(TestUtil.firstEnvironmentVariable());
	}

	@Test
	public void testException() {
		Throwable t = thrown(() -> {
			throw new IOException("test");
		});
		assertThrowable(t, IOException.class, "test");
		assertNull(thrown(() -> {}));
	}

	@Test
	public void testInit() {
		boolean throwIt = false;
		assertEquals(init(() -> {
			if (throwIt) throw new IOException();
			return "test";
		}), "test");
		assertThrown(RuntimeException.class, () -> init(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testNullErr() {
		try (SystemIo sys = TestUtil.nullOutErr()) {
			System.err.print("This text should not appear");
		}
	}

	@Test
	public void testTestMap() {
		Map<Integer, String> map = testMap(1, "1", 2, "2", 3);
		assertEquals(map.size(), 3);
		assertEquals(map.get(1), "1");
		assertEquals(map.get(2), "2");
		assertNull(map.get(3));
	}

	@Test
	public void testResource() {
		assertEquals(resource("resource.txt"), "test");
		assertThrown(RuntimeException.class, () -> resource("not-found.txt"));
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
		assertThrown(IllegalArgumentException.class,
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
		assertCollection(TestUtil.pathsToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testToUnixFromPath() {
		List<String> paths =
			List.of(Path.of("a", "b", "c.txt").toString(), Path.of("a", "a.txt").toString());
		assertCollection(TestUtil.pathNamesToUnix(paths), "a/b/c.txt", "a/a.txt");
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
