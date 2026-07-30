package ceri.common.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ValueCondition;
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
		try (var t = Testing.threadRun(() -> {
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
		Assert.equal(Testing.close(TestFuture.of(1)), null);
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
	public void testRunRepeat() throws InterruptedException {
		var sync = BoolCondition.of();
		try (var _ = Testing.runRepeat(sync::signal)) {
			sync.await();
			sync.await();
		}
	}

	@Test
	public void testRunRepeatWithIndex() throws InterruptedException {
		var sync = ValueCondition.<Integer>of();
		try (var _ = Testing.runRepeat(i -> sync.signal(i))) {
			sync.await(i -> i > 1);
		}
	}

	@Test
	public void testThreadCall() {
		var sync = ValueCondition.<String>of();
		try (var exec = Testing.threadCall(sync::await)) {
			sync.signal("test");
			Assert.equal(exec.get(), "test");
		}
	}

	@Test
	public void testThreadRun() {
		var sync = BoolCondition.of();
		try (var exec = Testing.threadRun(sync::await)) {
			sync.signal();
			exec.get();
		}
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
	public void testReadString() {
		try (var sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			Assert.equal(Testing.readString(), "test");
			Assert.equal(Testing.readString(), "");
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
				Assert.thrown(Testing::readString);
			}
		}
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		Assert.equal(Testing.readableString(bytes), "?a.??~!?");
		Assert.thrown(IllegalArgumentException.class,
			() -> Testing.readableString(bytes, 3, 2, "test", '?'));
		Assert.equal(Testing.readableString(new byte[0], 0, 0, null, '.'), "");
		Assert.equal(Testing.readableString(new byte[0], 0, 0, "", '.'), "");
	}

	@Test
	public void testReader() {
		Assert.array(Testing.reader(1, 2, 3).readBytes(), 1, 2, 3);
		Assert.array(Testing.reader("abc").readBytes(), 'a', 'b', 'c');
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
