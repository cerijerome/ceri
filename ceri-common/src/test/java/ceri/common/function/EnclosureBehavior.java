package ceri.common.function;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;

public class EnclosureBehavior {

	@Test
	public void shouldProvideRepeatableClosure() {
		var sync = CallSync.runnable(true);
		try (var rep = Enclosure.Repeater.of(() -> enclosure("test", sync))) {
			Assert.equal(rep.ref(), null);
			Assert.equal(rep.get(), "test");
			sync.assertNoCall();
			Assert.equal(rep.init(), "test");
			Assert.equal(rep.get(), "test");
			sync.assertCalls(1);
			Assert.equal(rep.ref(), "test");
		}
	}

	@Test
	public void shouldProvideUnsafeRepeatableClosure() {
		var sync = CallSync.runnable(true);
		try (var rep = Enclosure.Repeater.unsafe(() -> enclosure("test", sync))) {
			Assert.equal(rep.get(), "test");
			sync.assertNoCall();
			Assert.equal(rep.init(), "test");
			Assert.equal(rep.get(), "test");
			sync.assertCalls(1);
		}
	}

	@Test
	public void shouldWrapCloseable() throws IOException {
		var sync = CallSync.runnable(true);
		try (Excepts.Closeable<IOException> ec = () -> sync.run()) {
			try (var _ = Enclosure.of(ec)) {}
			sync.assertCalls(1);
		}
	}

	@Test
	public void shouldTransformCloseable() {
		var sync = CallSync.runnable(true);
		try (var _ = Enclosure.from("test", () -> sync.run())) {}
		sync.assertCalls(1);
	}

	@Test
	public void shouldCreateCloseable() {
		var sync = CallSync.runnable(true);
		try (var c = Enclosure.from(() -> sync, CallSync.Runnable::run)) {
			Assert.equal(c.ref, sync);
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAdaptCloseableType() {
		var sync = CallSync.runnable(true);
		Functions.Closeable rc = sync::run;
		try (Enclosure<String> c = Enclosure.adapt(rc, _ -> "test")) {
			Assert.equal(c.ref, "test");
		}
		sync.assertCalls(1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseOnAdaptFailure() {
		var sync = CallSync.runnable(true);
		Functions.Closeable rc = sync::run;
		Assert.thrown(() -> Enclosure.adapt(rc, _ -> Assert.throwRuntime()));
		sync.assertCalls(1);
	}

	@Test
	public void shouldExecuteOnClose() {
		String[] ss = { "a" };
		try (var _ = Enclosure.of(ss, s -> s[0] = null)) {}
		Assert.isNull(ss[0]);
	}

	@Test
	public void shouldNotExecuteForNullSubject() {
		String[] ss = null;
		try (var _ = Enclosure.of(ss, _ -> {
			throw new RuntimeException();
		})) {}
	}

	@Test
	public void shouldAllowNullCloser() throws Exception {
		String[] ss = null;
		try (var _ = Enclosure.of(ss, null)) {}
	}

	@Test
	public void shouldCloseCloseablesInReverseOrder() {
		var captor = Captor.ofInt();
		try (var _ = Enclosure.ofAll(() -> captor.accept(1), () -> captor.accept(2))) {}
		captor.verify(2, 1);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfSubjectExists() {
		Assert.yes(Enclosure.empty().isEmpty());
		Assert.yes(Enclosure.noOp(null).isEmpty());
		Assert.no(Enclosure.noOp(new Object()).isEmpty());
		Assert.no(Enclosure.of(new Object(), null).isEmpty());
		Assert.yes(Enclosure.of(null, _ -> {}).isNoOp());
		Assert.no(Enclosure.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfCloseOperationExists() {
		Assert.yes(Enclosure.empty().isNoOp());
		Assert.yes(Enclosure.noOp(null).isNoOp());
		Assert.yes(Enclosure.noOp(new Object()).isNoOp());
		Assert.yes(Enclosure.of(new Object(), null).isNoOp());
		Assert.yes(Enclosure.of(null, _ -> {}).isNoOp());
		Assert.no(Enclosure.of(new Object(), _ -> {}).isNoOp());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(Enclosure.of("test", _ -> {}), "[test]");
	}

	private static <T> Enclosure<T> enclosure(T t, CallSync.Runnable closer) {
		return Enclosure.from(t, closer::run);
	}
}
