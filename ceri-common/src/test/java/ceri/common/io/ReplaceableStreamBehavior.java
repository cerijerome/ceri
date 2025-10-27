package ceri.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestConnector;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;
import ceri.common.test.TestUtil;

public class ReplaceableStreamBehavior {
	private ValueCondition<Exception> sync;
	private ReplaceableStream.In rin;
	private TestInputStream tin;
	private InputStream in;
	private InputStream in2;
	private ReplaceableStream.Out rout;
	private TestOutputStream tout;
	private ByteArrayOutputStream bout;
	private ByteArrayOutputStream bout2;
	private ReplaceableStream.Con.Fixable<Connector.Fixable> fcon;
	private TestConnector tcon;

	@After
	public void after() {
		in2 = TestUtil.close(in2);
		in = TestUtil.close(in);
		tin = TestUtil.close(tin);
		rin = TestUtil.close(rin);
		bout2 = TestUtil.close(bout2);
		bout = TestUtil.close(bout);
		tout = TestUtil.close(tout);
		rout = TestUtil.close(rout);
		tcon = TestUtil.close(tcon);
		fcon = TestUtil.close(fcon);
		sync = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(ReplaceableStream.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceIn() throws IOException {
		initIn();
		rin = ReplaceableStream.in();
		rin.replace(tin);
		rin.replace(tin); // does nothing
		rin.replace(TestInputStream.of());
		tin.close.assertCalls(1);
	}

	@Test
	public void shouldNotifyInListenerOfMarkException() throws InterruptedException {
		initIn();
		tin.mark.error.setFrom(ErrorGen.RTX);
		rin = ReplaceableStream.in();
		rin.errors().listen(sync::signal);
		rin.set(tin);
		Assert.thrown(() -> rin.mark(0));
		Assert.throwable(sync.await(), RuntimeException.class);
	}

	@Test
	public void shouldNotifyInListenerOfMarkSupportedException() throws InterruptedException {
		initIn();
		tin.markSupported.error.setFrom(ErrorGen.RTX);
		rin = ReplaceableStream.in();
		rin.errors().listen(sync::signal);
		Assert.no(rin.markSupported());
		rin.set(tin);
		Assert.thrown(rin::markSupported);
		Assert.throwable(sync.await(), RuntimeException.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyInListenersOfErrors() throws InterruptedException {
		initIn();
		tin.read.error.setFrom(ErrorGen.IOX);
		tin.to.writeBytes(0, 0);
		rin = ReplaceableStream.in();
		rin.set(tin);
		rin.errors().listen(sync::signal);
		Assert.thrown(rin::read);
		Assert.throwable(sync.await(), IOException.class);
		Assert.thrown(rin::read);
		Assert.throwable(sync.await(), IOException.class);
	}

	@Test
	public void shouldFailWithAnInvalidIn() {
		rin = ReplaceableStream.in();
		Assert.thrown(rin::read);
		Assert.thrown(rin::available);
		Assert.thrown(() -> rin.skip(0));
		rin.mark(4);
		Assert.thrown(rin::reset);
		byte[] buffer = new byte[100];
		Assert.thrown(() -> rin.read(buffer));
		Assert.thrown(() -> rin.read(buffer, 1, 99));
	}

	@Test
	public void shouldPassThroughInMarkAndReset() throws IOException {
		rin = ReplaceableStream.in();
		in = bis("test");
		Assert.no(rin.markSupported());
		rin.set(in);
		Assert.yes(rin.markSupported());
		Assert.equal(rin.available(), 4);
		byte[] buffer = new byte[6];
		rin.read(buffer, 0, 2);
		rin.mark(2);
		rin.read(buffer, 2, 2);
		rin.reset();
		rin.skip(1);
		rin.read(buffer, 4, 2);
		Assert.array(buffer, "testt\0".getBytes());
	}

	@Test
	public void shouldAllowInToBeReplaced() throws IOException {
		rin = ReplaceableStream.in();
		in = bis("test");
		in2 = bis("again");
		rin.set(in);
		byte[] buffer = new byte[9];
		rin.read(buffer);
		rin.set(in2);
		rin.read(buffer, 4, 5);
		Assert.array(buffer, "testagain".getBytes());
	}

	@Test
	public void shouldReadFromIn() throws IOException {
		rin = ReplaceableStream.in();
		in = bis("test");
		rin.set(in);
		byte[] buffer = new byte[2];
		Assert.equal(rin.read(), (int) 't');
		rin.read(buffer);
		Assert.array(buffer, 'e', 's');
		rin.read(buffer, 1, 1);
		Assert.array(buffer, 'e', 't');
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceOut() throws IOException {
		initOut();
		rout = ReplaceableStream.out();
		rout.replace(tout);
		rout.replace(tout); // does nothing
		rout.replace(TestOutputStream.of());
		tout.close.assertCalls(1);
	}

	@Test
	public void shouldNotifyOutListenersOfErrors() throws InterruptedException {
		initOut();
		tout.write.error.setFrom(ErrorGen.IOX);
		rout = ReplaceableStream.out();
		rout.set(tout);
		rout.errors().listen(sync::signal);
		Assert.thrown(() -> rout.write(0));
		Assert.throwable(sync.await(), IOException.class);
		Assert.thrown(() -> rout.write(0xff));
		Assert.throwable(sync.await(), IOException.class);
	}

	@Test
	public void shouldFailWithAnInvalidOut() {
		rout = ReplaceableStream.out();
		Assert.thrown(() -> rout.write(0));
		byte[] buffer = new byte[100];
		Assert.thrown(() -> rout.write(buffer));
		Assert.thrown(() -> rout.write(buffer, 1, 98));
		Assert.thrown(rout::flush);
	}

	@Test
	public void shouldAllowOutToBeReplaced() throws IOException {
		rout = ReplaceableStream.out();
		bout = new ByteArrayOutputStream();
		bout2 = new ByteArrayOutputStream();
		rout.set(bout);
		byte[] buffer = "test".getBytes();
		rout.write(buffer);
		rout.set(bout2);
		rout.write(buffer, 1, 3);
		assertBout(bout, "test");
		assertBout(bout2, "est");
	}

	@Test
	public void shouldWriteToOut() throws IOException {
		rout = ReplaceableStream.out();
		bout = new ByteArrayOutputStream();
		rout.set(bout);
		byte[] buffer = "test".getBytes();
		rout.write(buffer);
		rout.write('t');
		rout.write(buffer, 1, 3);
		rout.flush();
		assertBout(bout, "testtest");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceConnector() throws IOException {
		initCon(null);
		Assert.thrown(fcon::open);
		fcon.replace(tcon);
		fcon.open();
		Assert.equal(fcon.in().available(), 0);
		fcon.out().write(0);
	}

	@Test
	public void shouldListenForErrors() throws IOException {
		initCon("test");
		var captor = Captor.of();
		try (var _ = fcon.listeners().enclose(captor::accept)) {
			fcon.replace(tcon);
			fcon.broken();
			captor.verify(StateChange.broken);
		}
	}

	private void initIn() {
		sync = ValueCondition.of();
		tin = TestInputStream.of();
	}

	private void initOut() {
		sync = ValueCondition.of();
		tout = TestOutputStream.of();
	}

	private void initCon(String name) {
		fcon = ReplaceableStream.con(name);
		tcon = TestConnector.of();
	}

	private static void assertBout(ByteArrayOutputStream bout, String s) {
		Assert.array(bout.toByteArray(), s.getBytes());
	}

	private static ByteArrayInputStream bis(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}
}
